package cc.bodyplus.bodyplus_sdk.ecg;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */

public class HorizontalEcgSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {


    private SurfaceHolder mSurfaceHolder;
    private ArrayList<Float> ecgDatas = new ArrayList<>();

    //子线程标志位
    private boolean mIsDrawing;


    private float mHeight;
    private Context mContext;

    private float bigHeight, litHeight;

    private int DataLength; // 一共有多少个点，目前硬件每秒钟有250个点的数据
    private float lockWidth;//每次锁屏需要画的
    private static int ecgPerCount = 1;//每次画心电数据的个数

    private int mBigH = 0;


    private float baseY;


    private Paint wavePaint;


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

    public HorizontalEcgSurfaceView(Context context) {
        super(context);
        mContext = context;
        initView();
        initPaint();
    }

    public HorizontalEcgSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint();
        initView();
    }



    private void initPaint() {

        wavePaint = new Paint();
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setAntiAlias(true);//去锯齿
        wavePaint.setColor(Color.parseColor("#8c8c8c"));//颜色
        wavePaint.setStrokeWidth(2);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        litHeight = dipTopx(mContext, 6); // 每一小格6dp
        bigHeight = 5 * litHeight; // 每一大格
        // 竖直线个数
        int XLineTotal = (int) (mWidth / litHeight);
        // 水平线个数
        int YLineTotal = (int) (mHeight / litHeight);
        DataLength = XLineTotal * 10; // 走速25mm/s
        baseY = (YLineTotal / 2) * litHeight + mBigH * bigHeight;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;

    }


    public int getDataLength() {
        return DataLength;
    }


    private float dipTopx(Context context, float dpValue) {
        return dpValue * context.getResources().getDisplayMetrics().density;
    }

    /**
     * 将心电图向上或向下移动一大格
     *
     * @param bigH >0向下  0< 向上
     */
    public void moveBaseLineDown(int bigH) {
        mBigH = bigH;
    }


    public static int dip2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


    @Override
    public void run() {
        while (mIsDrawing) {
            long startTime = System.currentTimeMillis();

            //绘图的Canvas
            Canvas mCanvas = mSurfaceHolder.lockCanvas();
            synchronized (mSurfaceHolder) {
                if (mCanvas != null) {
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                    drawRect(mCanvas);
//                    drawText(mCanvas);
//                    drawSeconds(mCanvas);
                    drawWave(mCanvas);
                }
            }
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
            long endTime = System.currentTimeMillis();
            //每次锁屏的时间间距，单位:ms
            int sleepTime = 20;
            if (endTime - startTime < sleepTime) {
                SystemClock.sleep(sleepTime - (endTime - startTime));
            }

        }
    }


    private void drawWave(Canvas canvas) {
        try {
            Path path = new Path();
            float startX = 0.0f, startY = 0.0f;
            float length = litHeight / 10; // 每一个点距离（xy是相等的） 走速固定25mm/s
            // 2大格 1mv
            int ymaxValue = 2;
            float unitY = ymaxValue * bigHeight;
            List<Float> drawData = ecgDatas;
            if (ecgDatas.size() >= DataLength) {
                drawData = ecgDatas.subList(ecgDatas.size() - DataLength, ecgDatas.size());
            }

            final int size = drawData.size();
            for (int i = 0; i < size; i++) {
                float data = drawData.get(i);
                startY = baseY - data * unitY;
                if (startY < 0) {
                    startY = 1.0f;
                }
                if (startY > mHeight) {
                    startY = mHeight;
                }
                startX = length * i;
                if (i == 0) {
                    path.moveTo(startX, startY);
                } else {
                    path.lineTo(startX, startY);
                }
            }
            canvas.drawPath(path, wavePaint);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 设置当前心率值，该方法需要
     *
     * @param value
     */
    public void setCurrentHeartData(float[] value) {

        synchronized (mSurfaceHolder) {
            for (float aValue : value) {
                ecgDatas.add(aValue);
            }
        }


    }


    public void reSet() {
        mIsDrawing = false;
        ecgDatas.clear();
    }


    public void rePlay() {
        mIsDrawing = true;
        ecgDatas.clear();
    }

    public void setChangeData(float[] value) {
        synchronized (mSurfaceHolder) {
            for (float aValue : value) {
                ecgDatas.add(aValue);
            }
        }
    }



}
