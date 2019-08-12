package cc.bodyplus.bodyplus_sdk;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;

import cc.bodyplus.sdk.ble.manger.BleConnectionInterface;
import cc.bodyplus.sdk.ble.manger.BleConnectionManger;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.MyBleDevice;
import cc.bodyplus.sdk.ble.wave.HorizontalEcgWaveSurfaceView;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */

public class CoreWaveActivity extends Activity implements BleConnectionInterface {


    private DeviceInfo deviceInfo;
    private HorizontalEcgWaveSurfaceView ecg_surface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave);
        ecg_surface = (HorizontalEcgWaveSurfaceView) findViewById(R.id.ecg_surface);

        BleConnectionManger.getInstance().addConnectionListener(this,true); // 注册蓝牙监听


        deviceInfo = (DeviceInfo) getIntent().getExtras().getSerializable("deviceInfo");
        if (deviceInfo == null){
            finish();
        }

    }


    /**
     * 开启ecg波形
     */
    public void startHRV(View view) {
        ecg_surface.startHrvWave();

    }

    /**
     * 关闭ecg波形
     */
    public void finishHRV(View view) {
        ecg_surface.finishHRV();

    }


    @Override
    protected void onDestroy() {
        BleConnectionManger.getInstance().removeConnectionListener(this); // 移除监听
        ecg_surface.finishHRV();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BleConnectionManger.getInstance().fetchCoreMode(); // 读取位置信息
        BleConnectionManger.getInstance().fetchPowerLevel(); // 读取电量信息
    }


    @Override
    public void reReNameSucceed() {
    }

    @Override
    public void reBleStateOn() {

    }

    @Override
    public void reOfflineStatus(boolean isOfflineStatus) {

    }

    @Override
    public void reRssi(int rssi, int status) {

    }

    @Override
    public void bleDataCallBack(int code, int dm) {
    }

    @Override
    public void bleHeartDataError() {
    }

    @Override
    public void blePowerLevel(byte data) {
    }

    @Override
    public void bleReConnectDevice(DeviceInfo device) {
        // 重连成功的回调
    }

    @Override
    public void bleDeviceDisconnect(int status) {
        // 连接断开的回调
        ecg_surface.finishHRV();
    }

    @Override
    public void bleCoreModule(byte data) {
    }

    @Override
    public void reDeviceList(@Nullable ArrayList<MyBleDevice> lists) {

    }
}
