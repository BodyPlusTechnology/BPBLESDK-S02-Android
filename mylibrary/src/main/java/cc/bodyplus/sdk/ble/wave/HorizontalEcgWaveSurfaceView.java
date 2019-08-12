package cc.bodyplus.sdk.ble.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import cc.bodyplus.sdk.ble.manger.BleConnectionManger;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */

public class HorizontalEcgWaveSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mSurfaceHolder;
    private Context mContext;
    private float bigHeight, litHeight;
    private int DataLength; // 一共有多少个点，目前硬件每秒钟有250个点的数据
    private int mBigH = 0;
    private float baseY;
    private Paint wavePaint;
    private int XLineTotal = 75; // 水平线个数
    private int YLineTotal = 50; // 竖直线个数
    private float mWidth, mHeight;
    private float[] dataByte;
    private int YmaxValue = 2; 	// 2大格 1mv

    /**
     * 初始化View
     */
    private void initView() {
        mSurfaceHolder = getHolder();
        //注册回调方法
        mSurfaceHolder.addCallback(this);
        //设置一些参数方便后面绘图
        setFocusable(true);
        setKeepScreenOn(true);
        setFocusableInTouchMode(true);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
//        setZOrderMediaOverlay(true);

    }

    public HorizontalEcgWaveSurfaceView(Context context) {
        super(context);
        mContext = context;
        initView();
        initPaint();
    }

    public HorizontalEcgWaveSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint();
        initView();
    }

    private void initPaint() {
        wavePaint = new Paint();
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setAntiAlias(true);//去锯齿
        wavePaint.setColor(Color.parseColor("#ff7575"));//颜色
        wavePaint.setStrokeWidth(2);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        litHeight = (mWidth/XLineTotal); // 每一小格6dp
        bigHeight = 5*litHeight; // 每一大格
        mHeight = YLineTotal * litHeight;
        DataLength = XLineTotal * 10 ; // 走速25mm/s
        dataByte = new float[DataLength];
        baseY = (YLineTotal/2)*litHeight + mBigH*bigHeight;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    /**
     * 将心电图向上或向下移动一大格
     *
     * @param bigH >0向下  0< 向上
     */
    private void moveBaseLineDown(int bigH) {
        mBigH = bigH;
    }


    private void prepareDrawWave(){
        Canvas mCanvas = mSurfaceHolder.lockCanvas();
        synchronized (mSurfaceHolder) {
            if (mCanvas != null) {
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawWave(mCanvas);
            }
        }
        if (mCanvas != null) {
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    private void drawWave(Canvas canvas) {
        try {
            Path path = new Path();
            float startX = 0.0f, startY = 0.0f;
            float length = litHeight/10; // 每一个点距离（xy是相等的） 走速固定25mm/s
            float unitY = YmaxValue*bigHeight;
            for (int i=0;i<DataLength;i++){
                float data = dataByte[i];
                startY = baseY - data*unitY;
                if (startY < 0){
                    startY = 1.0f;
                }
                if (startY > mHeight ){
                    startY = mHeight;
                }
                startX = length*i;
                if (i == 0){
                    path.moveTo(startX, startY);
                }else {
                    path.lineTo(startX, startY);
                }
            }
            canvas.drawPath(path, wavePaint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int currentIndex = 0;
    private void setCurrentHeartData(float[] value){
        if (dataByte == null){
            return;
        }
        try {
            if (currentIndex < DataLength){

            }else {
                currentIndex = 0;
            }
            System.arraycopy(value,0,dataByte,currentIndex,value.length);
            currentIndex += value.length;
            prepareDrawWave();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private final int WAVE_OUT_LENGTH = 5;
    private final int WAVE_OUT_ECG_LENGTH = 250;
    private final float GAIN = 400f;
    private int intervalCount = 0;
    private float[] waveData = new float[250];
    private float[] waveDataDuplicate = new float[250];
    private final long INTERVAL = (1000L * WAVE_OUT_LENGTH )/ WAVE_OUT_ECG_LENGTH;
    private final long WAIT_INTERVAL = 8*1000;


    private void obtainEcgPrepareData(){
        EcgWaveFrameData frameData = EcgWaveOriginalDataUtils.getInstance().pollConversionWave(true);
        waveDataDuplicate = new float[frameData.ecgData.length];
        for (int i=0;i<frameData.ecgData.length;i++){
            waveDataDuplicate[i] = frameData.ecgData[i]/ GAIN;
        }
    }

    private void invalidateEcgWaveData(){
        intervalCount ++;
        final float[] des = new float[5];
        System.arraycopy(waveData,5*(intervalCount-1),des,0,5);
        if (intervalCount == 50){
            intervalCount = 0;
            System.arraycopy(waveDataDuplicate,0,waveData,0,waveDataDuplicate.length);
        }
        setCurrentHeartData(des);
    }

    private boolean isStartHrv = false;

    public void startHrvWave() {
        if (isStartHrv) {
            Toast.makeText(mContext, "小老弟，怎么能重复开启采集呢(^o^)", Toast.LENGTH_SHORT).show();
            return;
        }
        isStartHrv = true;
        EcgWaveOriginalDataUtils.getInstance().reset();
        BleConnectionManger.getInstance().switchEcgWave(true);
        Toast.makeText(mContext, "来了，老弟 ！请等待EcgWave数据上传~\n \n 你知道吗，低端phone需要等待8秒哦？", Toast.LENGTH_SHORT).show();
        startInvalidateTimer();
        startObtainDataTimer();
    }

    public void finishHRV() {
        BleConnectionManger.getInstance().switchEcgWave(false);
        finishEcgWave();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(100);
                prepareDrawWave();
            }
        }).start();
    }

    private void finishEcgWave(){
        isStartHrv = false;
        cancelDisposable(obtainDisposable);
        cancelDisposable(invalidateDisposable);
    }



    private Disposable obtainDisposable;
    private Disposable invalidateDisposable;

    private void startObtainDataTimer() {
        obtainDisposable = Observable.interval(WAIT_INTERVAL - 500,1000, TimeUnit.MILLISECONDS)
                . subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        waveDataDuplicate = new float[250];
                    }
                }).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (isStartHrv) {
                    obtainEcgPrepareData();
                }
            }
        });
    }

    private void startInvalidateTimer(){
        invalidateDisposable = Observable.interval(WAIT_INTERVAL,INTERVAL, TimeUnit.MILLISECONDS)
                . subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        intervalCount = 0;
                        currentIndex = 0;
                        dataByte = new float[DataLength];
                        waveData = new float[250];
                        prepareDrawWave();
                    }
                }).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (isStartHrv){
                            invalidateEcgWaveData();
                        }
                    }
                });
    }

    private void cancelDisposable(Disposable disposable){
        if (disposable!=null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

}
