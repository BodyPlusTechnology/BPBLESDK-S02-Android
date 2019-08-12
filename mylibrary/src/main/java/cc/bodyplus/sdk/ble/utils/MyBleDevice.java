package cc.bodyplus.sdk.ble.utils;

import java.io.Serializable;

/**
 * Created by shihu.wang on 2016/7/14.
 * Email shihu.wang@bodyplus.cc
 */
public class MyBleDevice implements Serializable {
    private String macAddress;
    private String deviceSn;
    private int rssi;
    private String deviceName;

    public boolean getDfuStatus() {
        return isDfuStatus;
    }

    public void setDfuStatus(boolean dfuStatus) {
        isDfuStatus = dfuStatus;
    }

    private boolean isDfuStatus;

    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Sets device name.
     *
     * @param deviceName the device name
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Gets mac address.
     *
     * @return the mac address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Sets mac address.
     *
     * @param macAddress the mac address
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Gets device sn.
     *
     * @return the device sn
     */
    public String getDeviceSn() {
        return deviceSn;
    }

    /**
     * Sets device sn.
     *
     * @param deviceSn the device sn
     */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /**
     * Gets rssi.
     *
     * @return the rssi
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * Sets rssi.
     *
     * @param rssi the rssi
     */
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}