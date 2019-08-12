package cc.bodyplus.bodyplus_sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Shihoo.Wang 2019/6/27
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 *
 * 用来监听程序前后台切换的监听
 */
public class AppForeBackgroundListener {

    private ApplicationCallbackListener activityLifecycleCallbacks;
    private OnAppStatusListener statusListener;

    public AppForeBackgroundListener(){
    }

    /**
     * 注册状态监听，仅在Application中使用
     */
    public void register(Application application){
        activityLifecycleCallbacks = new ApplicationCallbackListener();
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }


    /**
     * 取消 状态监听，仅在Application中使用
     */
    public void unRegister(Application application){
        if (activityLifecycleCallbacks != null){
            application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        }
    }

    public void setStatusListener(OnAppStatusListener listener){
        this.statusListener = listener;
    }

    private class ApplicationCallbackListener implements  Application.ActivityLifecycleCallbacks{

        //打开的Activity数量统计
        private int activityStartCount = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            activityStartCount++;
            //数值从0变到1说明是从后台切到前台
            if (activityStartCount == 1){
                //从后台切到前台
                if(statusListener != null){
                    statusListener.onFront();
                }
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityStartCount--;
            //数值从1到0说明是从前台切到后台
            if (activityStartCount == 0){
                //从前台切到后台
                if(statusListener != null){
                    statusListener.onBack();
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    public interface OnAppStatusListener{
        void onFront();
        void onBack();
    }
}
