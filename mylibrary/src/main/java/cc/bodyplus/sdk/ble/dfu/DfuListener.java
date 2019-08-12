package cc.bodyplus.sdk.ble.dfu;

import cc.bodyplus.sdk.ble.utils.DeviceInfo;

/**
 * Created by Shihoo.Wang 2019/6/3
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */

public interface DfuListener {
    String ACTION_BLE_UPDATE_RESULT = "cc.bodyplus.sdk.ble.update.result";
    void onSucceed();
    void onError();
    void onBleOff();
    void onProgress(int percent);
    // 0 :准备升级 1：已经是最新版本，无需升级。2：安装包解析异常。3：当前固件信息有误。4：升级文件路径有误。
    void onStart(int status, DeviceInfo device);
}
