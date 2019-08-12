package cc.bodyplus.sdk.ble.manger;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cc.bodyplus.sdk.ble.utils.BleUtils;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.MyBleDevice;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by shihu.wang on 2017/3/15.
 * Email shihu.wang@bodyplus.cc
 */
public class BleService extends Service {
    private IncomingHandler mHandler;
    private Messenger mMessenger;
    private Messenger mClient ;
    private BluetoothAdapter mBlueToothAdapter;
    public static boolean isDeviceConnection;
    private List<MyBleDevice> mLeDevices = new ArrayList<>(); // 扫描到所有设备 的集合
    private List<String> mDeviceAddress = new ArrayList<>(); // 扫描到所有设备 的物理地址的集合 用来过滤掉重复的设备
    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<>(); // 扫描到所有设备 的物理地址的集合 用来过滤掉重复的设备
    private GattBodyPlus mGattBodyPlus ;

    public static final int MSG_REGEISTER = 10;                     // 创建通信桥梁
    public static final int MSG_SEARCH_DEVICE = 11;                 // 搜索设备
    public static final int MSG_CONNECT_DEVICE = 12;                // 连接设备
    public static final int MSG_DISCONNECT = 13;                    // 断开设备连接
    public static final int MSG_POWER_LEVEL = 14;                   // 电池电量
    public static final int MSG_CORE_MODE = 15;                     // Core的位置信息
    public static final int MSG_BLE_NAME = 16;                      // 更改设备名称
    public static final int MSG_START_DFU = 17;                     // 开始DFU
    public static final int MSG_UPDATE_BLE_RESULT = 18;          //
    public static final int MSG_TEST_WRITE_CMD = 19;          // 测试 cmd写数据
    public static final int MSG_ECG_DATA_TYPE = 20;   // 开启心率和呼吸
    public static final int MSG_AUTO_CONNECT_SN = 21;   // 自动重连控制
    public static final int MSG_STOP_AUTO_CONNECT = 22;   // 停止自动重连
    public static final int MSG_OFFLINE_WORK_STATUS = 32;              // 查询设备离线模式的状态
    public static final int MSG_OFFLINE_MODE = 33;              // S02产品离线模式
    public static final int MSG_OFFLINE_DATA_ASK = 34;          // S02产品离线模式 读命令 查询是否有数据
    public static final int MSG_OFFLINE_DATA_UPL = 35;          // S02产品离线模式 写命令 开始数据上传
//    public static final int MSG_OFFLINE_DATA_END = 36;          // S02产品离线模式 写命令 完成数据接收
    public static final int MSG_OFFLINE_DATA_ERS = 37;          // S02产品离线模式 写命令 擦除所有历史数据
//    public static final int MSG_OFFLINE_DATA_INF = 38;          // S02产品离线模式 读命令 查询当前要读的数据信息 返回时间和长度 8byte
    public static final int MSG_ECG_WAVE_TYPE = 39;   // 开启心率和呼吸
    public static final int MSG_READ_RSSI = 40;   // 信号强度
    public static final int MSG_CANCEL_BOND = 41;   // 取消绑定
    public static final int MSG_NORMAL_CONNECT_STATUS = 42;   // 连接正常，不需要升级
    public static final int MSG_AUTO_CONNECT_MAC = 43;   // 自动重连(使用MAC地址)
    public static final int MSG_BACKGROUND = 44;   // Android O 后台运行判断

    public static final int RE_SCAN_DEVICE = 110; //搜索所有设备
    public static final int RE_DEVICE_DISCONNECT_STATE = 113; //设备断开
    public static final int RE_CORE_MODLE = 114; //设备的位置状态
    public static final int RE_CORE_HEART_DATA_ERROR = 115; //ECG脱落
    public static final int RE_POW_LEVEL = 117; //电量
    public static final int RE_BLE_WRITE_NAME_SUCCEED = 118; // 改设备名的返回
    public static final int RE_BODY_ECG_WAVE_DATA = 123; // 心电波形数据
    public static final int RE_HRV_RESULT = 124; // HRV分析结果
    public static final int RE_CORE_CONNECT = 130; // 设备重连
    public static final int RE_BODY_DATA = 141; // 心率、呼吸数据
    public static final int RE_MSG_GARMENT_DETAILS = 143;          // 服装底座具体byte数组
    public static final int RE_MSG_S02_TEST = 144;              // S02测试命令
    public static final int RE_MSG_BLE_STATE_CHANGE = 145;              // 手机蓝牙打开/关闭
    public static final int RE_MSG_CONNECT_DEVICE_DFU_STATE = 146;              // 连接的设备为DFU状态
    public static final int RE_BODY_RATE_OFFLINE_DATA_ERROR = 147;          // S02离线数据传输失败
    public static final int RE_BODY_RATE_OFFLINE_DATA_PROCESS = 148;          // S02离线数据传输进度
    public static final int RE_BODY_RATE_OFFLINE_DATA_END = 149;          // S02离线数据传输完成
    public static final int RE_SWITCH_DATA_ACK = 150;          //
    public static final int RE_READ_RSSI = 151;           // 信号强度

    public BleService() {
        mHandler = new IncomingHandler(this);
        mMessenger = new Messenger(mHandler);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBlueToothAdapter = bluetoothManager.getAdapter();
        }
        registerReceiver(blueStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        LogUtil.wshLog().d("BleSer创建");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(blueStateBroadcastReceiver);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    private static class IncomingHandler extends Handler {

        SoftReference<BleService> softReference;
        IncomingHandler(BleService service) {
            softReference = new SoftReference<>(service);
        }

        @Override
        public synchronized void handleMessage(Message msg) {
            try {
                BleService service;
                if (softReference == null) {
                    return;
                }else {
                    service = softReference.get();
                    if (service == null) {
                        return;
                    }
                }
                switch (msg.what) {
                    case MSG_REGEISTER:  // 注册信使
                        service.mClient = msg.replyTo;
                        break;
                    case MSG_SEARCH_DEVICE: // 搜索设备
                        service.startScanBleDevice();
                        break;
                    case MSG_CONNECT_DEVICE:  // 连接设备
                        service.connectDeviceBySelect((MyBleDevice) msg.obj);
                        break;
                    case MSG_DISCONNECT: // 断开已有连接
                        service.disConnect();
                        break;
                    case MSG_POWER_LEVEL: // 电量
                        service.fetchPowerLevel();
                        break;
                    case MSG_CORE_MODE: // 位置信息
                        service.fetchCoreMode();
                        break;
                    case MSG_START_DFU: // BL升级开始
                        service.coreStartDfu();
                        break;
                    case MSG_UPDATE_BLE_RESULT: // BL升级的结果

                        break;
                    case MSG_ECG_DATA_TYPE: // 开启或关闭ECG校准 全部
                        boolean isOpend = (boolean) msg.obj;
                        service.switchEcgChannelType(isOpend);
                        break;
                    case MSG_BLE_NAME: // 更改设备名称
                        service.deviceReName(msg);
                        break;
                    case MSG_TEST_WRITE_CMD:
                        service.testWriteCmd(msg);
                        break;
                    case MSG_AUTO_CONNECT_SN:
                        String sn  = (String) msg.obj;
                        service.autoScanDevice(sn);
                        break;
                    case MSG_STOP_AUTO_CONNECT :
                        service.stopAutoScan();
                        break;
                    case MSG_OFFLINE_MODE: // 开启或关闭S02离线模式
                        boolean isOp = (boolean) msg.obj;
                        service.switchOfflineMode(isOp);
                        break;
                    case MSG_OFFLINE_WORK_STATUS:
                        service.offlineWorkStatus();
                        break;
                    case MSG_OFFLINE_DATA_ASK:
                        service.offlineDataAsk();
                        break;
                    case MSG_OFFLINE_DATA_UPL:
                        service.offlineDataUpload();
                        break;
//                    case MSG_OFFLINE_DATA_END:
//                        service.offlineDataUpload();
//                        break;
                    case MSG_OFFLINE_DATA_ERS:
                        service.offlineDataRest();
                        break;
                    case MSG_ECG_WAVE_TYPE:
                         service.switchEcgWave((boolean) msg.obj);
                        break;
                    case MSG_READ_RSSI:
                        service.readRemoteRssi();
                        break;
                    case MSG_CANCEL_BOND:
                        service.cancelBond();
                        break;
                    case MSG_NORMAL_CONNECT_STATUS:
                        service.normalConnectStatus();
                        break;
                    case MSG_AUTO_CONNECT_MAC:
                        DeviceInfo info  = (DeviceInfo) msg.obj;
                        service.autoScanDeviceMac(info);
                        break;
                    case MSG_BACKGROUND:
                        service.setBackground(msg);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void setBackground(Message msg){
        buildNotification(this,msg.arg1,msg.arg2, (String) msg.obj);
    }

    private void deviceReName(Message message){
        String name = (String) message.obj;
        if (mGattBodyPlus != null){
            mGattBodyPlus.deviceReName(name);
        }
    }

    /**
     * 开启和关闭ECG 心率呼吸 数据采集
     * @param isOpen 打开或关闭数据采集
     */
    private void switchEcgChannelType(boolean isOpen) {
        if (mGattBodyPlus != null){
            mGattBodyPlus.switchEcgChannel(isOpen);
        }
    }

    /**
     * 开启和关闭ECG 心率呼吸 数据采集
     * @param isOpen 打开或关闭数据采集
     */
    private void switchEcgWave(boolean isOpen) {
        if (mGattBodyPlus != null){
            mGattBodyPlus.switchEcgWave(isOpen);
        }
    }

    private void testWriteCmd(Message message){
        short cmd = (short) message.arg2;
        byte[] data = (byte[]) message.obj;
        if (mGattBodyPlus != null){
            mGattBodyPlus.testWriteCmd(cmd,data);
        }
    }

    private void fetchPowerLevel(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.fetchPowerLevel();
        }
    }

    private void fetchCoreMode(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.fetchCoreMode();
        }
    }

    /**
     * DFU 蓝牙升级
     */
    private void coreStartDfu(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.startDfu();
        }
    }

    private void offlineWorkStatus(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.offlineWorkStatus();
        }
    }

    private void switchOfflineMode(boolean isOpen){
        if (mGattBodyPlus != null){
            mGattBodyPlus.switchOfflineMode(isOpen);
        }
    }

    private void offlineDataAsk(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.offlineDataAsk();
        }
    }

    private void offlineDataUpload(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.offlineDataPrepareUpload();
        }
    }

    private void offlineDataRest(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.offlineDataRest();
        }
    }

    private void readRemoteRssi(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.readRemoteRssi();
        }
    }

    private void normalConnectStatus(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.normalConnectStatus();
        }
    }

    private void cancelBond(){
        if (mGattBodyPlus != null){
            mGattBodyPlus.cancelBond();
        }
    }
    /**
     * 向所有绑定服务的activity发送消息
     * @param msg 消息主体
     */
    private void sendMessage(Message msg) {
        try {
            mClient.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 主动断开连接
     */
    private void disConnect(){
        isDeviceConnection = false;
        mLeDevices.clear();
        mDeviceAddress.clear();
        mBluetoothDeviceList.clear();
        if (mGattBodyPlus != null){
            mGattBodyPlus.disconnect();
            mGattBodyPlus = null;
        }
    }

    /**
     * 搜索开始设备
     */
    private void startScanBleDevice(){
        mHandler.postDelayed( new Runnable() {
            @Override
            public void run() {
                mBlueToothAdapter.stopLeScan(mLeScanCallback); // 3秒后结束扫描
                // 通知界面扫描结束 并传递数据
                if (mLeDevices!=null && mLeDevices.size() > 0){
                    Message msg = Message.obtain(null, RE_SCAN_DEVICE);
                    msg.arg1 = 0 ;
                    msg.obj = mLeDevices;
                    sendMessage(msg);
                }else{
                    // 通知没有扫描到
                    Message msg = Message.obtain(null, RE_SCAN_DEVICE);
                    msg.arg1 = 2;
                    msg.obj = mLeDevices;
                    sendMessage(msg);
                }
            }
        }, 5*1000);
        mLeDevices.clear();
        mDeviceAddress.clear();
        mBluetoothDeviceList.clear();
        if (mBlueToothAdapter.isDiscovering()){
            mBlueToothAdapter.cancelDiscovery();
        }
        UUID[] CMD_SERVICE = {UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")};
        mBlueToothAdapter.startLeScan(mLeScanCallback);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null && device.getName() != null) {
                if (mDeviceAddress.size()>0 && mDeviceAddress.contains(device.getAddress())){
                    return;
                }
                byte[] b = BPAerobicDevicesCheckUtils.geSnBytesByScanRecord(scanRecord);
                if (b!=null && b.length>0){
                    String deviceSn = BleUtils.byteToChar(b);
                    if (!BPAerobicDevicesCheckUtils.checkIsS02(deviceSn)){
                        return;
                    }
                    if(deviceSn.length()>=10) {
                        String  sn = deviceSn.substring(0,10);
                        MyBleDevice myBleDevice = new MyBleDevice();
                        myBleDevice.setMacAddress(device.getAddress());
                        myBleDevice.setDeviceSn(sn);
                        myBleDevice.setRssi(rssi);
                        myBleDevice.setDeviceName(device.getName());
                        myBleDevice.setDfuStatus(BleUtils.isFilterDFUUUID(scanRecord));
                        mLeDevices.add(myBleDevice);
                        mDeviceAddress.add(device.getAddress());
                        mBluetoothDeviceList.add(device);
                    }
                }

            }
        }
    };

    // ---------------------------------------- 使用mac地址重連 ---------------------------
    private Disposable remoteDisposable;

    private void autoScanDeviceMac(final DeviceInfo info){
        if (info==null || info.macAddress.length()<1){
            return;
        }
        //搜索附件设备，防止重启蓝牙的后无法连接
        startRemoteDisposable();
//        autoScanDevice(info.sn);

        mHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        connectRemote(info);
                    }
                }
        ,3000);

    }

    private void connectRemote(DeviceInfo info){
        if (!mBlueToothAdapter.isEnabled()){
            return;
        }
        final DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.sn = info.sn;
        deviceInfo.bleName = info.bleName;
        deviceInfo.macAddress = info.macAddress;
        if (mGattBodyPlus != null) {
            mGattBodyPlus.disconnect();
            mGattBodyPlus.autoReConnect(info.macAddress);
        }else {
            mGattBodyPlus = new GattS02(BleService.this, deviceInfo, gattCallBack);
            mGattBodyPlus.autoReConnect(info.macAddress);
        }
    }

    // ----  ---------------
    private void stopRemoteDisposable(){
        if (remoteDisposable!=null && !remoteDisposable.isDisposed()){
            remoteDisposable.dispose();
            remoteDisposable = null;
        }
    }
    private void startRemoteDisposable(){
        if (remoteDisposable==null || remoteDisposable.isDisposed()) {
            remoteDisposable = Observable.interval(10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {

                        }
                    }).subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            intervalScan();
                        }

                    });
        }
        startScanRemoteDevice();
    }

    private void intervalScan(){
        stopScanRemoteDevice();
        SystemClock.sleep(5*1000);
        if (!isDeviceConnection) {
            startScanRemoteDevice();
        }else {
            stopRemoteDisposable();
        }
    }

    private void startScanRemoteDevice(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBlueToothAdapter.isEnabled()) {
                    mBlueToothAdapter.startLeScan(mReConnectRemoteLeScanCallback);
                }
            }
        });
    }

    private void stopScanRemoteDevice(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mReConnectRemoteLeScanCallback != null) {
                    mBlueToothAdapter.stopLeScan(mReConnectRemoteLeScanCallback);
                }
            }
        });
    }


    private BluetoothAdapter.LeScanCallback  mReConnectRemoteLeScanCallback = new BluetoothAdapter.LeScanCallback(){

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        }
    };

    // -------------------------- 自动连接的搜索 暂时和上面的绑定时搜索分开 ------------------------------------

    private String mReconnectSn;
    private boolean isReScaned;
//    private Timer timer ;
    private Disposable disposable;

    private BluetoothAdapter.LeScanCallback  mReConnectLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!isReScaned && device != null) {
                if (mReconnectSn==null || mReconnectSn.length()<1){
                    stopReLeScanHandler();
                    return;
                }
                byte[] b = BPAerobicDevicesCheckUtils.geSnBytesByScanRecord(scanRecord);
                if (b!=null && b.length>0){
                    String deviceSn = BleUtils.byteToChar(b);
                    if(deviceSn.length()>=10) {
                        String sn = deviceSn.substring(0,10);
                        if (sn.equals(mReconnectSn)) {
                            MyBleDevice myBleDevice = new MyBleDevice();
                            myBleDevice.setMacAddress(device.getAddress());
                            myBleDevice.setDeviceSn(sn);
                            myBleDevice.setRssi(rssi);
                            myBleDevice.setDeviceName(device.getName());
                            myBleDevice.setDfuStatus(BleUtils.isFilterDFUUUID(scanRecord));
                            reAutoScanDevice(myBleDevice);
                        }
                    }
                }
            }
        }
    };

    private void stopAutoScan(){
        isReScaned = true;
        mReconnectSn = null;
        stopReLeScanHandler();
        destroyBleStatusTimer();
        cancelBond();
    }

    private void autoScanDevice(String sn){
        if (sn==null || sn.length()<1){
            return;
        }
        Log.d("wsh", "reconnect sn: "+ sn);
        mReconnectSn = sn;
        isReScaned = false;
        startBleStatusTimer();
    }

    private synchronized void startBleStatusTimer(){
        if (disposable==null || disposable.isDisposed()) {
            disposable = Observable.interval(10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {

                        }
                    }).subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            checkBleStatus();
                        }

                    });
        }
        startReLeScanHandler();
    }


    private void checkBleStatus(){
        if (!isReScaned && !isDeviceConnection){
            stopReLeScanHandler();
            SystemClock.sleep(5*1000);
            if (!isReScaned && !isDeviceConnection) {
                startReLeScanHandler();
            }
        }else {
            stopReLeScanHandler();
            destroyBleStatusTimer();
        }
    }


    private void startReLeScanHandler(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBlueToothAdapter.isEnabled()) {
                    mBlueToothAdapter.startLeScan(mReConnectLeScanCallback);
                }
            }
        });
    }

    private void stopReLeScanHandler(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mReConnectLeScanCallback != null) {
                    mBlueToothAdapter.stopLeScan(mReConnectLeScanCallback);
                }
            }
        });
    }

    // 停止定时器
    private synchronized void destroyBleStatusTimer(){
        if(disposable!=null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

    private void reAutoScanDevice(MyBleDevice myBleDevice){
        Log.d("wsh", "reconnect  搜素到 sn: "+ mReconnectSn);
        isReScaned = true;
        stopReLeScanHandler();
        connectBySearchDevice(myBleDevice);
    }


    // 连接一个设备
    private void connectBySearchDevice(MyBleDevice device, BluetoothDevice bluetoothDevice){
        destroyBleStatusTimer();
        if (device.getDfuStatus()){
            //如果是DFU
            Message msg = Message.obtain(null, RE_MSG_CONNECT_DEVICE_DFU_STATE);
            msg.obj = device.getDeviceSn();
            sendMessage(msg);
        }else {
            stopReLeScanHandler();
            final DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.sn = device.getDeviceSn();
            deviceInfo.bleName = device.getDeviceName();
            deviceInfo.macAddress = device.getMacAddress();
            if (mGattBodyPlus != null) {
                mGattBodyPlus.disconnect();
                mGattBodyPlus.connect(bluetoothDevice);
            }else {
                mGattBodyPlus = new GattS02(BleService.this, deviceInfo, gattCallBack);
                mGattBodyPlus.connect(bluetoothDevice);
            }
        }
    }

    // 连接一个设备
    private void connectBySearchDevice(MyBleDevice device){
        destroyBleStatusTimer();
        if (device.getDfuStatus()){
            //如果是DFU
            Message msg = Message.obtain(null, RE_MSG_CONNECT_DEVICE_DFU_STATE);
            msg.obj = device.getDeviceSn();
            sendMessage(msg);
        }else {
            stopReLeScanHandler();
            final DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.sn = device.getDeviceSn();
            deviceInfo.bleName = device.getDeviceName();
            deviceInfo.macAddress = device.getMacAddress();
            if (mGattBodyPlus != null) {
                mGattBodyPlus.disconnect();
            }
            mGattBodyPlus = new GattS02(BleService.this, deviceInfo, gattCallBack);
            mGattBodyPlus.connect(device.getMacAddress());
        }
    }

    private void connectDeviceBySelect(MyBleDevice device){
//        if (device!=null && device.getDeviceSn()!=null && !mBluetoothDeviceList.isEmpty()) {
//            for (BluetoothDevice bluetoothDevice : mBluetoothDeviceList){
//                if (device.getMacAddress().equals(bluetoothDevice.getAddress())){
//                    connectBySearchDevice(device,bluetoothDevice);
//                    break;
//                }
//            }
//        }
        connectBySearchDevice(device);
    }

    private GattCallBack gattCallBack = new GattCallBack(){

        @Override
        public void handleMessage(Message message) {
            sendMessage(message);
        }

        @Override
        public void reDisconnect(int status) {
            isDeviceConnection = false;
            Message msg = Message.obtain(null,RE_DEVICE_DISCONNECT_STATE);
            msg.arg1 = status;
            sendMessage(msg);
        }

        @Override
        public void handleLogData(byte[] data) {
//            BleLogUtils.BleLogUtils(data);
        }


        @Override
        public void reConnectSucceed(DeviceInfo deviceInfo) {
            isDeviceConnection = true;
            Message msg = Message.obtain(null,RE_CORE_CONNECT);
            msg.obj = deviceInfo;
            sendMessage(msg);
        }

        @Override
        public void reCoreModule(byte[] stateValue) {
            if (isDeviceConnection) { // 如果是连接状态 才发送Core位置变化的消息  过滤掉运动页面重连时 首先发送数据采集的情况
                Message msg = Message.obtain(null, RE_CORE_MODLE);
                msg.obj = stateValue;
                sendMessage(msg);
            }
        }
    };

    @SuppressLint("NewApi")
    private void buildNotification(Context mContext, int enable,@DrawableRes int icon, String title) {
        String channelId = mContext.getPackageName();
        if (enable == 1){
            // 允许
            Notification.Builder builder = null;
            Notification notification = null;
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
                NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                @SuppressLint("WrongConstant")
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        title, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//设置提示灯
                notificationChannel.setLightColor(Color.GREEN);//设置提示灯颜色
                notificationChannel.setShowBadge(true);//显示logo
                notificationChannel.setDescription("");//设置描述
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); //设置锁屏可见 VISIBILITY_PUBLIC=可见
                notificationChannel.enableVibration(false);
                notificationChannel.setSound(null,null);
                notificationManager.createNotificationChannel(notificationChannel);

                builder = new Notification.Builder(mContext, channelId);
                builder.setChannelId(channelId);
            } else {
                builder = new Notification.Builder(mContext);
            }
            builder.setContentTitle(title)
                    .setSmallIcon(icon) // 这是设置小图标 防止用户点击
                    .setContentText("正在后台运行")
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                    .setWhen(System.currentTimeMillis());

            notification = builder.build();
            startForeground(1,notification);
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel mChannel = mNotificationManager.getNotificationChannel(channelId);
                if (null != mChannel) {
                    mNotificationManager.deleteNotificationChannel(channelId);
                }
            }else {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
            }
        }

    }


    protected BroadcastReceiver blueStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            switch (blueState) {
                case BluetoothAdapter.STATE_ON:
                    Message msgOn = Message.obtain(null, RE_MSG_BLE_STATE_CHANGE);
                    msgOn.arg1 = 1;
                    sendMessage(msgOn);
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Message msgOff = Message.obtain(null, RE_MSG_BLE_STATE_CHANGE);
                    msgOff.arg1 = 2;
                    sendMessage(msgOff);
                    stopAutoScan();
                    break;
            }
        }
    };
}
