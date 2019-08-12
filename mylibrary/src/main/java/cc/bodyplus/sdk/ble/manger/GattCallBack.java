package cc.bodyplus.sdk.ble.manger;

import android.os.Message;

import cc.bodyplus.sdk.ble.utils.DeviceInfo;


/**
 * Created by shihu.wang on 2017/3/25.
 * Email shihu.wang@bodyplus.cc
 */
public interface GattCallBack {

    /**
     * Handle message.
     *
     * @param message the message
     */
    void handleMessage(Message message);

    /**
     * Re bond succeed.
     *
     * @param deviceInfo the device info
     */

    /**
     * Re connect succeed.
     * @param deviceInfo the device info
     */
    void reConnectSucceed(DeviceInfo deviceInfo);


    /**
     * Re core modle.
     *
     * @param stateValue the state value
     */
    void reCoreModule(byte[] stateValue);

    /**
     * Re disconnect.
     * @param status
     */
    void reDisconnect(int status);

    /**
     * Handle log data.
     *
     * @param data the data
     */
    void handleLogData(byte[] data);
}