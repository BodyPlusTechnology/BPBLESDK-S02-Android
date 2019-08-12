package cc.bodyplus.sdk.ble.wave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class ECGBgViewHorizontal extends View {

    private float mWidth, mHeight;
    private Context mContext;

    private float bigHeight,litHeight;

    private int DataLength; // 一共有多少个点，目前硬件每秒钟有250个点的数据
    private float[] dataByte;
    private int XLineTotal = 75; // 水平线个数
    private int YLineTotal = 50; // 竖直线个数


    private int YmaxValue = 2; 	// 2大格 1mv
    private int mBigH = 0;

    public ECGBgViewHorizontal(Context context) {
        super(context);
        mContext = context;
        initPaint();
    }

    public ECGBgViewHorizontal(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint();
    }


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
        drawRect(canvas);
        drawSeconds(canvas);
        drawText(canvas);
    }
    private int secondsHeight;
    private Paint textPaint;
    private Paint bigRectPaint;
    private Paint litRectPaint;
    private int textMarginTop = 12;

    private void initPaint(){

        textPaint = new Paint();
        textPaint.setColor(Color.argb(155,80,80,80));
        textPaint.setTextSize(dip2px(mContext,15));
        textPaint.setStrokeWidth(dip2px(mContext,0.5f));
        textPaint.setAntiAlias(true);

        bigRectPaint = new Paint();
        bigRectPaint.setStyle(Paint.Style.STROKE);
        bigRectPaint.setColor(Color.parseColor("#e4e5e6"));
        bigRectPaint.setStrokeWidth(dip2px(mContext,0.5f));

        litRectPaint = new Paint();
        litRectPaint.setStyle(Paint.Style.STROKE);
        litRectPaint.setColor(Color.parseColor("#70e4e5e6"));
        litRectPaint.setStrokeWidth(dip2px(mContext,0.5f));
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
        int avg = YLineTotal/2;
        canvas.drawLine(0,litHeight*avg,mWidth,litHeight*avg,bigRectPaint);
        canvas.drawLine(0,YLineTotal*litHeight-1,mWidth,YLineTotal*litHeight-1,bigRectPaint);
        for (int i=2;i<=avg+1;i++){
            if ((i-1)%5 == 0){
                // 画粗线
                canvas.drawLine(0,litHeight*(avg+1-i),mWidth,litHeight*(avg+1-i),bigRectPaint);
            }else {
                //画细线
                canvas.drawLine(0, litHeight * (avg + 1 - i), mWidth, litHeight * (avg + 1 - i), litRectPaint);

            }
        }
        for (int i=avg+1;i<=YLineTotal;i++){
            if ((i-avg)%5 == 0){
                // 画粗线
                canvas.drawLine(0,litHeight*i,mWidth,litHeight*i,bigRectPaint);
            }else {
                //画细线
                canvas.drawLine(0,litHeight*i,mWidth,litHeight*i,litRectPaint);
            }
        }

        //画竖直线
        int avgX = XLineTotal/2;
        canvas.drawLine(litHeight*(avgX),0,litHeight*(avgX),mHeight,bigRectPaint);
        for (int i=2;i<=avgX+1;i++){
            if ((i-1)%5 == 0){
                // 画粗线
                canvas.drawLine(litHeight*(avgX+1-i),0,litHeight*(avgX+1-i),mHeight,bigRectPaint);
            }else {
                //画细线
                canvas.drawLine(litHeight*(avgX+1-i),0,litHeight*(avgX+1-i),mHeight,litRectPaint);
            }
        }

        for (int i=avgX+1;i<=XLineTotal;i++){
            if ((i-avgX)%5 == 0){
                // 画粗线
                canvas.drawLine(litHeight*i,0,litHeight*i,mHeight,bigRectPaint);
            }else {
                //画细线
                canvas.drawLine(litHeight*i,0,litHeight*i,mHeight,litRectPaint);
            }
        }
    }

    private boolean isNeedDrawSeconds = false;
    private float baseY;

    private void drawSeconds(Canvas canvas){
        if (isNeedDrawSeconds){
            //画水平线 从中间往上线画
            int avg = XLineTotal/2;
            canvas.drawLine(litHeight*avg,baseY+bigHeight*2,litHeight*avg,baseY+bigHeight*2+secondsHeight,bigRectPaint);

            for (int i=0;i<avg;i++){
                if (i>0 && i%(5*5) == 0){
                    canvas.drawLine(litHeight*(avg-i),baseY+bigHeight*2,litHeight*(avg-i),baseY+bigHeight*2+secondsHeight,bigRectPaint);
                }
            }
            for (int i=avg;i<XLineTotal;i++){
                if (i>0 && (i-avg)%(5*5) == 0){
                    canvas.drawLine(litHeight*i,baseY+bigHeight*2,litHeight*i,baseY+bigHeight*2+secondsHeight,bigRectPaint);
                }
            }
        }
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
}
