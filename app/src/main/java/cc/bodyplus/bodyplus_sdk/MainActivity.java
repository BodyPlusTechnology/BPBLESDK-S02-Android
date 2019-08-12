package cc.bodyplus.bodyplus_sdk;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cc.bodyplus.sdk.ble.manger.BleConnectionInterface;
import cc.bodyplus.sdk.ble.manger.BleConnectionManger;
import cc.bodyplus.sdk.ble.manger.BPAerobicDevicesCheckUtils;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.MyBleDevice;


public class MainActivity extends AppCompatActivity implements BleConnectionInterface,AppForeBackgroundListener.OnAppStatusListener{

    private BleDevicesListsAdapter adapter ;
    private ListView listView;
    private EditText editText;
    private MyBleDevice myBleDevice;
    private DeviceInfo deviceInfo;
    private TextView tv_jni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.search_result_list);
        editText = (EditText) findViewById(R.id.edit_sn);
        tv_jni = (TextView) findViewById(R.id.tv_jni);

        BleConnectionManger.getInstance().addConnectionListener(this,false); // 注册蓝牙监听

        ((App)getApplication()).setAppForeBackgroundStatusListener(this);
    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        short [] data = new short[75000];
////        int result = BPEcgWaveTools.m2_hrv_process(data,data.length);
////        tv_jni.setText("我是jni的反悔： ~ "+String.valueOf(result));
//    }

    @Override
    protected void onDestroy() {
        BleConnectionManger.getInstance().removeConnectionListener(this); // 移除监听
        super.onDestroy();
    }




    /**
     * 搜索附近蓝牙设备（S02产品）
     * 请求搜索是一个耗时的操作（耗时大于3秒），不宜频繁的执行，最好是在其回调的结果中执行下一次搜索刷新搜索列表
     * @param view
     */
    public void Search(View view){
        if (adapter != null) {
            adapter.setData(null);
        }
        BleConnectionManger.getInstance().searchDevice();
        Toast.makeText(this,"开始搜索",Toast.LENGTH_SHORT).show();
    }

    /**
     * 连接设备（连接搜索列表中已有的设备）
     * @param view
     */
    public void connectDevice(View view){
        if (adapter != null) {
            adapter.setData(null);
        }
        if (myBleDevice != null){
//            myBleDevice.setMacAddress("AA:BB:CC:DD:ED:CE");
            BleConnectionManger.getInstance().connectDevice(myBleDevice);
            Toast.makeText(MainActivity.this,"连接中！",Toast.LENGTH_SHORT).show();
            editText.setText(myBleDevice.getDeviceSn());
        }else {
            Toast.makeText(MainActivity.this,"请先从搜索列表中选中设备",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 解绑并断开现有连接
     */
    public void disConnect(View view) {
        BleConnectionManger.getInstance().disconnect();
        deviceInfo = null;
        myBleDevice = null;
    }

    /**
     * 使用已知SN号达到自动重连的效果
     * @param view
     */
    public void connectDeviceBySn(View view) {
        String sn = editText.getText().toString().trim();
        if (sn.length()==10 && BPAerobicDevicesCheckUtils.checkIsS02(sn)){
            BleConnectionManger.getInstance().autoConnectBle(sn);
        }else {
            Toast.makeText(this,"输入的设备SN号码有误！",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 详情页
     * @param view
     */
    public void showInfo(View view){
        if (deviceInfo!=null){
            Intent intent = new Intent(MainActivity.this,CoreInfoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("deviceInfo",deviceInfo);
            intent.putExtras(bundle);
            startActivity(intent);
        }else {
            Toast.makeText(MainActivity.this,"请先确定已连接设备！",Toast.LENGTH_SHORT).show();
        }
    }

    public void ecgWave(View view) {
        if (deviceInfo!=null){
            Intent intent = new Intent(MainActivity.this,CoreWaveActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("deviceInfo",deviceInfo);
            intent.putExtras(bundle);
            startActivity(intent);
        }else {
            Toast.makeText(MainActivity.this,"请先确定已连接设备！",Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeviceLists(final ArrayList<MyBleDevice> myBleDevices) {
        adapter = new BleDevicesListsAdapter(this);
        listView.setAdapter(adapter); //  要求加上 信号强度
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

                try {
                    BleDevicesListsAdapter.DialogViewHolder vHollder = (BleDevicesListsAdapter.DialogViewHolder) view.getTag();// 在每次获取点击的item时将对于的checkbox状态改变，同时修改map的值。
                    if (vHollder.device_check.isChecked()) {
                        myBleDevice = null;
                    } else {
                        myBleDevice = myBleDevices.get(position);
                    }
                    vHollder.device_check.toggle();// 反转当前视图的选中状态

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        adapter.setData(myBleDevices);
    }


    @Override
    public void reDeviceList(@Nullable ArrayList<MyBleDevice> lists) {
        showDeviceLists(lists);
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
        deviceInfo = device; // DeviceInfo对象，包含硬件版本信息
        Toast.makeText(MainActivity.this,"我是连接上的回调",Toast.LENGTH_SHORT).show();
        adapter.setData(null);
//        LogUtil.wshLog().d(device.toString());
    }

    @Override
    public void bleDeviceDisconnect(int status) {
        Toast.makeText(MainActivity.this,"我是断开的回调",Toast.LENGTH_SHORT).show();
//        BleConnectionManger.getInstance().autoConnectBle(deviceInfo.sn);
        adapter.setData(null);
    }

    @Override
    public void bleCoreModule(byte data) {

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(0);
            return true;
        }
        return false;
    }

    @Override
    public void onFront() {
        BleConnectionManger.getInstance().enableBleSerBackground(0,"", false);
    }

    @Override
    public void onBack() {
        BleConnectionManger.getInstance().enableBleSerBackground(R.mipmap.ic_launcher,getResources().getString(R.string.app_name), true);
    }


    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int PERMISSON_REQUESTCODE = 0;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    @Override
    protected void onResume() {
        try{
            super.onResume();
            if (Build.VERSION.SDK_INT >= 23) {
                if (isNeedCheck) {
                    checkPermissions(needPermissions);
                }
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * @param
     * @since 2.5.0
     */
    @TargetApi(23)
    private void checkPermissions(String... permissions) {
        try{
            if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (null != needRequestPermissonList
                        && needRequestPermissonList.size() > 0) {
                    try {
                        String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                        Method method = getClass().getMethod("requestPermissions", new Class[]{String[].class, int.class});
                        method.invoke(this, array, 0);
                    } catch (Throwable e) {

                    }
                }
            }

        }catch(Throwable e){
            e.printStackTrace();
        }
    }


    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    @TargetApi(23)
    private List<String> findDeniedPermissions(String[] permissions) {
        try{
            List<String> needRequestPermissonList = new ArrayList<String>();
            if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
                for (String perm : permissions) {
                    if (checkMySelfPermission(perm) != PackageManager.PERMISSION_GRANTED
                            || shouldShowMyRequestPermissionRationale(perm)) {
                        needRequestPermissonList.add(perm);
                    }
                }
            }
            return needRequestPermissonList;
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }


    private int checkMySelfPermission(String perm) {
        try {
            Method method = getClass().getMethod("checkSelfPermission", new Class[]{String.class});
            Integer permissionInt = (Integer) method.invoke(this, perm);
            return permissionInt;
        } catch (Throwable e) {
        }
        return -1;
    }

    private boolean shouldShowMyRequestPermissionRationale(String perm) {
        try {
            Method method = getClass().getMethod("shouldShowRequestPermissionRationale", new Class[]{String.class});
            Boolean permissionInt = (Boolean) method.invoke(this, perm);
            return permissionInt;
        } catch (Throwable e) {
        }
        return false;
    }


    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        try{
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
        return true;
    }
}
