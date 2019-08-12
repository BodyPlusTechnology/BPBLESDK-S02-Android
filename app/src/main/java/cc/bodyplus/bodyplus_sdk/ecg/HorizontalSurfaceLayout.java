package cc.bodyplus.bodyplus_sdk.ecg;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import cc.bodyplus.bodyplus_sdk.R;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class HorizontalSurfaceLayout extends RelativeLayout {

    private Context mContext;
    private View mView;
    private ECGBgViewHorizontal ecg_backgroud;
    private HorizontalEcgSurfaceView ecg_surface;

    public HorizontalSurfaceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public HorizontalSurfaceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.layout_surface_horizontal, this);
        ecg_backgroud = (ECGBgViewHorizontal) findViewById(R.id.ecg_backgroud);
        ecg_surface = (HorizontalEcgSurfaceView) findViewById(R.id.ecg_surface);

        ecg_backgroud.needDrawSeconds(true);
        ecg_backgroud.setTextMargin(60);
        ecg_backgroud.moveBaseLineDown(1);
        ecg_surface.moveBaseLineDown(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public void setHrText(int i) {
        if (i > 0) {
            ecg_backgroud.setLeftText("心率：" + i + "bpm");
        }else{
            ecg_backgroud.setLeftText("平均心率：- -");
        }
    }

    public void setCurrentHr(float[] i) {
        ecg_surface.setCurrentHeartData(i);
    }

    public void setChangD(float[] data) {
        ecg_surface.setChangeData(data);
    }

    public int getdatalen(){
        return ecg_surface.getDataLength();
    }

    public void resetData() {
        ecg_surface.rePlay();
    }
}
