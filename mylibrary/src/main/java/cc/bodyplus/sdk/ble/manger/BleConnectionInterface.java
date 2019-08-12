package cc.bodyplus.sdk.ble.manger;

import java.util.ArrayList;

import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.MyBleDevice;


/**
 * Created by shihu.wang on 2017/3/17.
 * Email shihu.wang@bodyplus.cc
 */
public interface BleConnectionInterface {

    /**
     * 回调Body数据 肌电 心率 呼吸
     *
     * @param code 在BleCmdConfig中配置的有
     * @param dm   心率或呼吸值
     */
    void bleDataCallBack(int code, int dm);

    /**
     * 心率检测脱落
     */
    void bleHeartDataError();

    /**
     * 电量读取的返回、电量改变的回调
     *
     * @param data 电量值
     */
    void blePowerLevel(byte data);

    /**
     * Core重连的回调
     *
     * @param device the device
     */
    void bleReConnectDevice(DeviceInfo device);

    /**
     * Core断开连接的回调
     */
    void bleDeviceDisconnect(int status);

    /**
     * Core位置变化的回调（主动触发）
     * byte[] data  说明：
     * data[0] == 00  充电座
     * data[0] == 01  上衣
     * data[0] == 10  裤子
     * data[0] == 11  独立
     *
     * @param data the data
     */
    void bleCoreModule(byte data);

    /**
     * 搜索设备的返回
     * @param lists 设备列表
     */
    void reDeviceList(ArrayList<MyBleDevice> lists);

    /**
     * 修改设备名称的返回
     */
    void reReNameSucceed();

    /**
     * 蓝牙打开的监听
     */
    void reBleStateOn();

    void reOfflineStatus(boolean isOfflineStatus);

    void reRssi(int rssi, int status);
}