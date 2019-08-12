package cc.bodyplus.sdk.ble.manger;

/**
 * Created by Shihoo.Wang on 2019/3/19.
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 * 用来监听BLE状态的 简化版的 BleConnectionInterface
 */
public interface BleConnectStateInterface {


    /**
     * 电量读取的返回、电量改变的回调。每次连接上，sdk 会主动去读取点亮
     * @param data 电量值
     */
    void blePowerLevel(byte data);

    /**
     * Core断开连接的回调
     */
    void bleDeviceDisconnect(int status);

    /**
     * Core解绑的回调
     */
    void bleDeviceUnBond();
}
