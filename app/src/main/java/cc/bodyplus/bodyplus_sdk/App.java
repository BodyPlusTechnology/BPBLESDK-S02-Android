package cc.bodyplus.bodyplus_sdk;

import android.app.Application;

import cc.bodyplus.sdk.ble.manger.BleConnectionManger;

/**
 * Created by Shihu.Wang on 2017/7/5.
 * Email shihu.wang@bodyplus.cc
 */

public class App extends Application {

    private BPDfuListener mBPDfuListener;
    private AppForeBackgroundListener appForeBackgroundListener;
    @Override
    public void onCreate() {
        super.onCreate();
        mBPDfuListener = new BPDfuListener();
        BleConnectionManger.getInstance().init(this,mBPDfuListener);
        initAppForeBackListener();
    }

    private void initAppForeBackListener(){
        appForeBackgroundListener = new AppForeBackgroundListener();
        appForeBackgroundListener.register(this);
    }

    public void setAppForeBackgroundStatusListener(AppForeBackgroundListener.OnAppStatusListener helper){
        if (appForeBackgroundListener != null){
            appForeBackgroundListener.setStatusListener(helper);
        }
    }
}
