package cc.bodyplus.bodyplus_sdk;

import cc.bodyplus.sdk.ble.dfu.DfuListener;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;

/**
 * Created by Shihoo.Wang 2019/3/21
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 *
 * 用来显示升级时的UI
 */
public class BPDfuListener implements DfuListener {

    @Override
    public void onSucceed() {
//        Log.d("wsh", "onSucceed : ");
    }

    @Override
    public void onError() {
//        Log.d("wsh", "onError : ");
    }

    @Override
    public void onBleOff() {

    }

    @Override
    public void onProgress(int percent) {
//        Log.d("wsh", "percent : "+percent);
    }

    @Override
    public void onStart(int status, DeviceInfo device) {

    }
}
