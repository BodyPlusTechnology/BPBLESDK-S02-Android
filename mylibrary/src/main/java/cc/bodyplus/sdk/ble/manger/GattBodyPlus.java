package cc.bodyplus.sdk.ble.manger;

import android.bluetooth.BluetoothDevice;

/**
 * Created by shihu.wang on 2017/3/27.
 * Email shihu.wang@bodyplus.cc
 * <p>
 * 产品父类
 * 提供一些公有方法
 */
public class GattBodyPlus {

    protected void autoReConnect(String mac){

    }

    /**
     * Connect.
     *
     * @param mBluetoothDevice the type
     */
    protected void connect(BluetoothDevice mBluetoothDevice) {

    }

    protected void connect(String macAddress) {

    }

    /**
     * Disconnect.
     */
    protected void disconnect() {

    }

    /**
     * Fetch power level.
     */
    protected void fetchPowerLevel() {

    }

    /**
     * Fetch core mode.
     */
    protected void fetchCoreMode() {

    }

    /**
     * Device re name.
     *
     * @param name the name
     */
    public void deviceReName(String name) {

    }

    /**
     * Start dfu.
     */
    public void startDfu() {

    }

    /**
     * Test write cmd.
     *
     * @param cmd  the cmd
     * @param data the data
     */
    public void testWriteCmd(short cmd, byte[] data) {

    }

    /**
     * Switch ecg channel.
     *
     * @param isOpen the is open
     */
    public void switchEcgChannel(boolean isOpen) {
    }


    public void offlineWorkStatus(){

    }


    public void switchOfflineMode(boolean isOpen) {

    }

    public void offlineDataAsk() {

    }

    public void offlineDataPrepareUpload() {

    }

    public void offlineDataRest() {

    }

    public void offlineDataStartUpload(){

    }

    public void offlineDataEnd(){

    }

    public void switchEcgWave(boolean isOpen) {

    }

    public void readRemoteRssi(){

    }

    public void cancelBond(){

    }

    public void normalConnectStatus() {

    }
}
