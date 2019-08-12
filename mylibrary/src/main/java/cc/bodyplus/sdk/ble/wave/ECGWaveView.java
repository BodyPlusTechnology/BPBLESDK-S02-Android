package cc.bodyplus.sdk.ble.wave;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cc.bodyplus.sdk.ble.manger.BleConnectionManger;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class ECGWaveView extends View {

    private float mWidth, mHeight;
    private Context mContext;

    private float bigHeight,litHeight;

    private int DataLength; // 一共有多少个点，目前硬件每秒钟有250个点的数据
    private float[] dataByte;
    private int XLineTotal = 75; // 水平线个数
    private int YLineTotal = 50; // 竖直线个数


    private int YmaxValue = 2; 	// 2大格 1mv
    private int mBigH = 0;

    public ECGWaveView(Context context) {
        super(context);
        mContext = context;
        initPaint();
    }

    public ECGWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
//			mHeight = MeasureSpec.getSize(heightMeasureSpec);
        litHeight = (mWidth/XLineTotal); // 每一小格6dp
        bigHeight = 5*litHeight; // 每一大格
        mHeight = YLineTotal * litHeight;
//			XLineTotal = (int)(mWidth/litHeight);
//			YLineTotal = (int)(mHeight/litHeight);
        DataLength = XLineTotal * 10 ; // 走速25mm/s
        dataByte = new float[DataLength];
        baseY = (YLineTotal/2)*litHeight + mBigH*bigHeight;
        secondsHeight = (int) dipTopx(mContext,20);
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawText(canvas);
        drawRect(canvas);
        drawWave(canvas);
        drawSeconds(canvas);
    }
    private int secondsHeight;
    private Paint wavePaint;
    private Paint textPaint;
    private Paint bigRectPaint;
    private Paint litRectPaint;
    private int textMarginTop = 12;

    private void initPaint(){

        wavePaint = new Paint();
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setAntiAlias(true);//去锯齿
        wavePaint.setColor(Color.parseColor("#ffffff"));//颜色
        wavePaint.setStrokeWidth(2);

        textPaint = new Paint();
        textPaint.setColor(Color.argb(80,255,255,255));
        textPaint.setTextSize(dip2px(mContext,15));
        textPaint.setStrokeWidth(dip2px(mContext,0.5f));
        textPaint.setAntiAlias(true);

        bigRectPaint = new Paint();
        bigRectPaint.setStyle(Paint.Style.STROKE);
        bigRectPaint.setColor(Color.parseColor("#80ffffff"));
        bigRectPaint.setStrokeWidth(1f);

        litRectPaint = new Paint();
        litRectPaint.setStyle(Paint.Style.STROKE);
        litRectPaint.setColor(Color.parseColor("#30ffffff"));
        litRectPaint.setStrokeWidth(1f);
    }
    private void drawText(Canvas canvas){
        String valueLift = "10mm/mv";
        String valueRight = "25mm/s";
        float sizeRight = textPaint.measureText(valueRight);
        Rect textBoundsRight = new Rect();
        textPaint.getTextBounds(valueRight, 0, valueRight.length(), textBoundsRight);
        int ysizeRight = textBoundsRight.height();
        canvas.drawText(valueLift, dip2px(mContext,16), dip2px(mContext,textMarginTop) + ysizeRight/2, textPaint);
        canvas.drawText(valueRight, mWidth - dip2px(mContext,16) - sizeRight, dip2px(mContext,textMarginTop) + ysizeRight/2, textPaint);
    }

    private void drawRect(Canvas canvas){
        //画水平线 从中间往上线画
//		int avg = YLineTotal/2;
//		canvas.drawLine(0,litHeight*avg,mWidth,litHeight*avg,bigRectPaint);
//		canvas.drawLine(0,YLineTotal*litHeight-1,mWidth,YLineTotal*litHeight-1,bigRectPaint);
//		for (int i=2;i<=avg+1;i++){
//			if ((i-1)%5 == 0){
//				// 画粗线
//				canvas.drawLine(0,litHeight*(avg+1-i),mWidth,litHeight*(avg+1-i),bigRectPaint);
//			}else {
//				//画细线
//				canvas.drawLine(0, litHeight * (avg + 1 - i), mWidth, litHeight * (avg + 1 - i), litRectPaint);
//
//			}
//		}
        for (int i=0;i<=YLineTotal;i++){
            if (i%5 == 0){
                // 画粗线
                canvas.drawLine(0,litHeight*i,mWidth,litHeight*i,bigRectPaint);
            }else {
                //画细线
                canvas.drawLine(0,litHeight*i,mWidth,litHeight*i,litRectPaint);
            }
        }
        canvas.drawLine(0,YLineTotal*litHeight-1,mWidth,YLineTotal*litHeight-1,bigRectPaint);

        //画竖直线
//		int avgX = XLineTotal/2;
//		canvas.drawLine(litHeight*(avgX),0,litHeight*(avgX),mHeight,bigRectPaint);
//		for (int i=2;i<=avgX+1;i++){
//			if ((i-1)%5 == 0){
//				// 画粗线
//				canvas.drawLine(litHeight*(avgX+1-i),0,litHeight*(avgX+1-i),mHeight,bigRectPaint);
//			}else {
//				//画细线
//				canvas.drawLine(litHeight*(avgX+1-i),0,litHeight*(avgX+1-i),mHeight,litRectPaint);
//			}
//		}

        for (int i=0;i<=XLineTotal;i++){
            if (i%5 == 0){
                // 画粗线
                canvas.drawLine(litHeight*i,0,litHeight*i,mHeight,bigRectPaint);
            }else {
                //画细线
                canvas.drawLine(litHeight*i,0,litHeight*i,mHeight,litRectPaint);
            }
        }
        canvas.drawLine(litHeight*XLineTotal-1,0,litHeight*XLineTotal-1,mHeight,bigRectPaint);
    }

    private boolean isNeedDrawSeconds = false;
    private float baseY;

    private void drawWave(Canvas canvas){
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


    private void drawSeconds(Canvas canvas){
        if (isNeedDrawSeconds){
            //画水平线 从中间往上线画
            int avg = XLineTotal/2;
            canvas.drawLine(litHeight*avg,baseY+bigHeight*2,litHeight*avg,baseY+bigHeight*2+secondsHeight,wavePaint);

            for (int i=0;i<avg;i++){
                if (i>0 && i%(5*5) == 0){
                    canvas.drawLine(litHeight*(avg-i),baseY+bigHeight*2,litHeight*(avg-i),baseY+bigHeight*2+secondsHeight,wavePaint);
                }
            }
            for (int i=avg;i<XLineTotal;i++){
                if (i>0 && (i-avg)%(5*5) == 0){
                    canvas.drawLine(litHeight*i,baseY+bigHeight*2,litHeight*i,baseY+bigHeight*2+secondsHeight,wavePaint);
                }
            }
        }
    }

    private int currentIndex = 0;

    /**
     * 设置当前心率值，该方法需要
     * @param value
     */
    public void setCurrentHeartData(float[] value){
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
            invalidate();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void reSet(){
        dataByte = new float[DataLength];
        invalidate();
    }

    public void setChangeData(float[] value){
        dataByte = value;
        invalidate();
    }

    public int getDataLength(){
        return DataLength;
    }


    private float dipTopx(Context context, float dpValue) {
        return dpValue * context.getResources().getDisplayMetrics().density;
    }

    /**
     * 将心电图向上或向下移动一大格
     * @param bigH >0向下  0< 向上
     */
    public void moveBaseLineDown(int bigH){
        mBigH = bigH;
    }

    public void needDrawSeconds(boolean isNeed){
        isNeedDrawSeconds = isNeed;
    }

    public void setTextMargin(int margin){
        textMarginTop = margin;
    }

    public static int dip2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    public float getViewHeight(){
        return mHeight;
    }


    private Timer timer;

    public void startHrvWave() {
        if (timer != null) {
            Toast.makeText(mContext, "小老弟，怎么能重复开启采集呢(^o^)", Toast.LENGTH_LONG).show();
            return;
        }
        BleConnectionManger.getInstance().switchEcgWave(true);
        createEcgDisposable();
        Toast.makeText(mContext, "请等待EcgWave数据上传！\n \n 你知道吗，低端phone需要等待8秒哦？", Toast.LENGTH_LONG).show();

    }

    public void finishHRV() {
        BleConnectionManger.getInstance().switchEcgWave(false);
        finishEcgWave();
    }

    private final int WAVE_OUT_LENGTH = 5;
    private final int WAVE_OUT_ECG_LENGTH = 250;
    private final float GAIN = 400f;
    private int count = 0;
    private float[] waveData = new float[250];

    private void createEcgDisposable(){
        waveData = null;
        count = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                count ++;
                if (count  == 1){
                    handleEcgTime();
                }
                handEcgData();
                if (count == 50){
                    count = 0;
                }
            }
        },8000,(1000L * WAVE_OUT_LENGTH )/ WAVE_OUT_ECG_LENGTH); // 延时8秒 开始定时
    }

    private void handleEcgTime(){
        EcgWaveFrameData frameData = EcgWaveOriginalDataUtils.getInstance().pollConversionWave(true);
        waveData = new float[frameData.ecgData.length];
        for (int i=0;i<frameData.ecgData.length;i++){
            waveData[i] = frameData.ecgData[i]/ GAIN;
        }
//        LogUtil.wshLog().d("开始取数据 --- 帧计数："+ frameData.waveCnt +"  -- 数据长度：" +waveData.length);
    }
    private void handEcgData(){
        final float[] des = new float[5];
        System.arraycopy(waveData,5*(count-1),des,0,5);

        post(new Runnable() {
            @Override
            public void run() {
                setCurrentHeartData(des);
            }
        });
//        LogUtil.wshLog().d("开始取数据 ---   次数：" + count +" 填充数据："+ Arrays.toString(des));
    }

    private void finishEcgWave(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
        reSet();
    }
}