package cc.bodyplus.bodyplus_sdk;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cc.bodyplus.sdk.ble.manger.BleConnectionInterface;
import cc.bodyplus.sdk.ble.manger.BleConnectionManger;
import cc.bodyplus.sdk.ble.manger.LogUtil;
import cc.bodyplus.sdk.ble.utils.BleCmdConfig;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.MyBleDevice;

/**
 * Created by Shihu.Wang on 2017/7/5.
 * Email shihu.wang@bodyplus.cc
 */

public class CoreInfoActivity extends Activity implements BleConnectionInterface {

    private TextView tv_hw_version,tv_fw_version,tv_sn,tv_power,tv_model,tv_file_path,tv_heart_rate;
    private EditText edit_name,edit_file_path;
    private DeviceInfo deviceInfo;

    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        tv_hw_version = (TextView) findViewById(R.id.tv_hw_version);
        tv_fw_version = (TextView) findViewById(R.id.tv_fw_version);
        tv_sn = (TextView) findViewById(R.id.tv_sn);
        tv_power = (TextView) findViewById(R.id.tv_power);
        tv_model = (TextView) findViewById(R.id.tv_model);
        tv_file_path = (TextView) findViewById(R.id.tv_file_path);
        tv_heart_rate = (TextView) findViewById(R.id.tv_heart_rate);
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_file_path = (EditText) findViewById(R.id.edit_file_path);

        BleConnectionManger.getInstance().addConnectionListener(this,true); // 注册蓝牙监听


        deviceInfo = (DeviceInfo) getIntent().getExtras().getSerializable("deviceInfo");
        if (deviceInfo == null){
            finish();
        }

        tv_hw_version.setText("硬件版本："+deviceInfo.hwVn);
        tv_fw_version.setText("固件版本："+deviceInfo.swVn);
        tv_sn.setText("SN："+deviceInfo.sn);
        edit_name.setText(deviceInfo.bleName);

        mHandler = new Handler();

    }

    @Override
    protected void onDestroy() {
        BleConnectionManger.getInstance().removeConnectionListener(this); // 移除监听

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BleConnectionManger.getInstance().fetchCoreMode(); // 读取位置信息
        BleConnectionManger.getInstance().fetchPowerLevel(); // 读取电量信息
    }

    /**
     * 开始心率数据采集
     * @param view
     */
    public void startEcg(View view){
        BleConnectionManger.getInstance().switchEcgChannel(true);
    }

    /**
     * 关闭心率数据采集
     * @param view
     */
    public void closedEcg(View view){
        BleConnectionManger.getInstance().switchEcgChannel(false);
        tv_heart_rate.setText("心率：- -");
    }




    /**
     * 开始升级
     * @param view
     * 因为我们的zip包中包含有另外一个zip包，不能以文件选择的形式来获取，所以需要手动输入。
     */
    public void startUpdate(View view){
        edit_file_path.setText("/sdcard/bodyplus/update/s02_0100_0120.zip");
        String zipPath = edit_file_path.getText().toString().trim();
        if (zipPath.length()>0){
//            int result = BleConnectionManger.getInstance().startDfu(this, DfuHelperS02.TYPE_DFU, zipPath, deviceInfo);
//            int result = BleConnectionManger.getInstance().startDfu(this, DfuHelperS02.TYPE_BOOTLOADER, zipPath, deviceInfo, new DfuHelperS02.DfuListener() {
//                @Override
//                public void onStart() {
//
//                }
//
//                @Override
//                public void onSucceed() {
//                    Toast.makeText(CoreInfoActivity.this,"升级成功的回调！",Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onError() {
//                    Toast.makeText(CoreInfoActivity.this,"升级失败的回调！",Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onProgress(int percent) {
//                    if (percent%10 == 0) {
//                        Toast.makeText(CoreInfoActivity.this, "升级进度的回调！"+percent+"%", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
            // 0:准备升级 1：已经是最新版本，无需升级。2：安装包解析异常。3：当前固件信息有误。4：升级文件路径有误。
//            switch (result){
//                case 0:
//                    Toast.makeText(CoreInfoActivity.this,"准备升级!",Toast.LENGTH_SHORT).show();
//
//                    break;
//                case 1:
//                    Toast.makeText(CoreInfoActivity.this,"已经是最新版本，无需升级!",Toast.LENGTH_SHORT).show();
//                    break;
//                case 2:
//                    Toast.makeText(CoreInfoActivity.this,"安装包解析异常!",Toast.LENGTH_SHORT).show();
//                    break;
//                case 3:
//                    Toast.makeText(CoreInfoActivity.this,"当前固件信息有误!",Toast.LENGTH_SHORT).show();
//                    break;
//                case 4:
//                    Toast.makeText(CoreInfoActivity.this,"升级文件路径有误!",Toast.LENGTH_SHORT).show();
//                    break;
//            }
        }
    }

    /**
     * 自定义设备名称
     * @param view
     */
    public void reName(View view){
        String name = edit_name.getText().toString().trim();
        if (name.length()<=12){
            BleConnectionManger.getInstance().changeBleName(name);
        }else {
            Toast.makeText(CoreInfoActivity.this,"名称过长！",Toast.LENGTH_SHORT).show();
        }
    }





    @Override
    public void reReNameSucceed() {
        Toast.makeText(CoreInfoActivity.this,"我是修改名称的回调！",Toast.LENGTH_SHORT).show();
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
        // 心率数据的回调
        if (code == BleCmdConfig.BLE_HEART_MESSAGE){
            tv_heart_rate.setText("心率："+dm);
        }else if (code == BleCmdConfig.BLE_HEART_ERROR_MESSAGE){
            LogUtil.wshLog().d("心率异常 ： " + dm);
        }else if (code == BleCmdConfig.BLE_BREATHING_ERROR_MESSAGE){
            LogUtil.wshLog().d("呼吸率异常 ： " + dm);
        }
    }

    @Override
    public void bleHeartDataError() {
        // 心率检测脱落的回调
        tv_heart_rate.setText("心率：- -");
    }

    @Override
    public void blePowerLevel(byte data) {
        // 电量的回调 范围0-100
        tv_power.setText("电量："+data);
    }

    @Override
    public void bleReConnectDevice(DeviceInfo device) {
        // 重连成功的回调
    }

    @Override
    public void bleDeviceDisconnect(int status) {
        // 连接断开的回调
    }

    @Override
    public void bleCoreModule(byte data) {
        // 位置状态的回调
        switch (data) {
            case 0x00: // 充电座
                tv_model.setText("位置：充电");
                break;
            case 0x01: // 上衣
                tv_model.setText("位置：服装");
                break;
            case 0x11: // 独立
                tv_model.setText("位置：独立");
                break;
        }
    }

    @Override
    public void reDeviceList(@Nullable ArrayList<MyBleDevice> lists) {

    }


    public void startOffline(View view) {
        BleConnectionManger.getInstance().setOfflineDataStatusLinster(new BleConnectionManger.OfflineDataAskLinster() {
            @Override
            public void onStatusBack(boolean hasOfflneData) {
                if (hasOfflneData){
                    Toast.makeText(CoreInfoActivity.this,"开启离线 失败 ：删除 本地离线数据 ，请再次开启 ！",Toast.LENGTH_SHORT).show();
                    BleConnectionManger.getInstance().offlineDataRest();
                }else {
                    Toast.makeText(CoreInfoActivity.this,"开启离线 成功 ：本地没有离线数据 ，开始采集！ ！",Toast.LENGTH_SHORT).show();
                    BleConnectionManger.getInstance().switchOfflineMode(true);
                }
                BleConnectionManger.getInstance().setOfflineDataStatusLinster(null);
            }
        });
        BleConnectionManger.getInstance().offlineDataAsk();

    }

    public void finishOffline(View view) {
        BleConnectionManger.getInstance().switchOfflineMode(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleConnectionManger.getInstance().offlineDataAsk();
            }
        },1000);

        BleConnectionManger.getInstance().setOfflineDataStatusLinster(new BleConnectionManger.OfflineDataAskLinster() {
            @Override
            public void onStatusBack(boolean hasOfflneData) {
                if (hasOfflneData){
                    Toast.makeText(CoreInfoActivity.this,"同步离线数据 ：有数据 ，开始同步 ！ ！",Toast.LENGTH_SHORT).show();
                    BleConnectionManger.getInstance().offlineDataUpload();
                }else {
                    Toast.makeText(CoreInfoActivity.this,"同步离线数据 ：无数据 ，再见！ ！",Toast.LENGTH_SHORT).show();
                }
                BleConnectionManger.getInstance().setOfflineDataStatusLinster(null);
            }
        });
    }
}
