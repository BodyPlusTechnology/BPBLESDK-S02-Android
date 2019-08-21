package cc.bodyplus.sdk.ble.manger;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.DrawableRes;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cc.bodyplus.sdk.ble.dfu.BootLoaderLocalConfig;
import cc.bodyplus.sdk.ble.dfu.DfuHelperS02;
import cc.bodyplus.sdk.ble.dfu.DfuListener;
import cc.bodyplus.sdk.ble.dfu.DfuLocalConfig;
import cc.bodyplus.sdk.ble.dfu.DfuNetConfig;
import cc.bodyplus.sdk.ble.dfu.DfuUpdateInfo;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.MyBleDevice;
import cc.bodyplus.sdk.ble.wave.EcgWaveFrameData;
import cc.bodyplus.sdk.ble.wave.EcgWaveOriginalDataUtils;

import static cc.bodyplus.sdk.ble.manger.BleService.MSG_OFFLINE_DATA_ASK;
import static cc.bodyplus.sdk.ble.manger.BleService.MSG_OFFLINE_WORK_STATUS;
import static cc.bodyplus.sdk.ble.manger.BleService.MSG_REGEISTER;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_CORE_CONNECT;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_DEVICE_DISCONNECT_STATE;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_POW_LEVEL;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_SCAN_DEVICE;


/**
 * Created by shihu.wang on 2017/3/15.
 * Email shihu.wang@bodyplus.cc
 * <p>
 * 用来 管理 Activity与BleService通信 的类
 * 数据透传
 */
public class BleConnectionManger implements ServiceConnection {
    private static BleConnectionManger mInstance = new BleConnectionManger();
    private Messenger mService = null;
    private static Messenger mMessenger;
    private BleConnectionInterface registedView;
    private ConcurrentHashMap<String, BleConnectionInterface> connectionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BleConnectStateInterface> stateMap = new ConcurrentHashMap<>();
    private Application application;
    private DfuHelperS02 dfuHelperS02;
    private DfuListener mBPDfuListener;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private BleConnectionManger() {
        mMessenger = new Messenger(new IncomingHandler(this));
    }

    public static BleConnectionManger getInstance() {
        return mInstance;
    }

    public void init(Application application, DfuListener dfuListener) {
        this.mBPDfuListener = dfuListener;
        this.application = application;
        startBondBleService();
    }

    /**
     * 如果蓝牙服务在Application中绑定失败，那就再来一次，
     * 解决不了Why，那就解决NoWhy
     */
    public boolean reInit() {
        if (mService == null){
            startBondBleService();
            return true;
        }
        return false;
    }

    private void startBondBleService() {
        Intent service = new Intent(application, BleService.class);
        application.startService(service);
        application.bindService(service, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = new Messenger(service);
        Message msg = Message.obtain(null, MSG_REGEISTER);
        if (msg != null) {
            msg.replyTo = mMessenger;
            sendMessage(msg);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (stopService) {
            stopService = false;
            application.stopService(new Intent(application, BleService.class));
        }else {
            startBondBleService();
        }
    }

    private boolean stopService;

    /**
     * 退出账号登录时关闭所有信息
     */
    public void clearAll(){
        disconnect();
        connectionMap.clear();
        registedView = null;
    }


    /**
     * 添加数据接收对象（可以是Activity , Fragment ,以及自定义的view）
     *
     * @param bleConnectionInterface 数据接收对象 默认接收控制指令数据
     * @param isRegisterBodyData     是否接收生理数据（肌肉 心率 呼吸 等）
     */
    public synchronized void addConnectionListener(BleConnectionInterface bleConnectionInterface,boolean isRegisterBodyData) {
        try {
            String clazzName = bleConnectionInterface.getClass().getName();
            //  覆盖相同key值的value值
            connectionMap.put(clazzName, bleConnectionInterface);
            if (isRegisterBodyData){
                registedView = bleConnectionInterface;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void addConnectStateListener(BleConnectStateInterface listener){
        try {
            String clazzName = listener.getClass().getName();
            //  覆盖相同key值的value值
            stateMap.put(clazzName, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除数据接收对象
     * 生命周期操作
     *
     * @param bleConnectionInterface the ble connection interface
     */
    public synchronized void removeConnectionListener(BleConnectionInterface bleConnectionInterface) {
        try {
            String clazzName = bleConnectionInterface.getClass().getName();
            if (connectionMap.containsKey(clazzName)) {
                connectionMap.remove(clazzName);
            }
            if(registedView!=null && registedView.getClass().getName().equals(clazzName)){
                registedView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void removeStatenListener(BleConnectStateInterface stateInterface) {
        try {
            String clazzName = stateInterface.getClass().getName();
            if (stateMap.containsKey(clazzName)){
                stateMap.remove(clazzName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendMessage(final Message msg) {
        if (msg != null && mService != null) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mService.send(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     *  ******************************** 页面请求数据的接口 *********************************
     */

    /**
     * 搜索设备
     */
    public void searchDevice(){
        Message message = Message.obtain(null,BleService.MSG_SEARCH_DEVICE);
        sendMessage(message);
    }

    /**
     * 连接指定的设备
     */
    public void connectDevice(MyBleDevice deviceInfo) {
        Message message = Message.obtain(null, BleService.MSG_CONNECT_DEVICE);
        message.obj = deviceInfo;
        sendMessage(message);
    }

    /**
     * 断开设备连接
     */
    public void disconnect() {
        Message message = Message.obtain(null,BleService.MSG_DISCONNECT);
        sendMessage(message);
    }

    /**
     * 取消绑定
     */
    public void cancelBond(){
        Message message = Message.obtain(null,BleService.MSG_CANCEL_BOND);
        sendMessage(message);
    }

    /**
     * 表示已经正常连接，不需要升级。
     */
    public void normalConnectStatus() {
        Message message = Message.obtain(null,BleService.MSG_NORMAL_CONNECT_STATUS);
        sendMessage(message);
    }


    /**
     * 读取Core电池电量
     */
    public void fetchPowerLevel() {
        Message message = Message.obtain(null,BleService.MSG_POWER_LEVEL);
        sendMessage(message);
    }

    /**
     * 读取Core的位置信息
     */
    public void fetchCoreMode() {
        Message message = Message.obtain(null,BleService.MSG_CORE_MODE);
        sendMessage(message);
    }

    /**
     * 销毁升级对象
     */
    public void destroyDfu(){
        if (dfuHelperS02 != null){
            dfuHelperS02.onDestroy();
            dfuHelperS02 = null;
        }
    }

    /**
     * 发送蓝牙升级跳转指令
     */
    public void startDfuCmd(){
        Message msg = Message.obtain(null, BleService.MSG_START_DFU);
        sendMessage(msg);
    }

    /**
     * 开启、关闭某一个硬件的数据ECG 心率呼吸 采集
     *
     * @param isSwitch the is switch
     */
    public void switchEcgChannel(boolean isSwitch) {
        Message message = Message.obtain(null,BleService.MSG_ECG_DATA_TYPE);
        message.obj = isSwitch;
        sendMessage(message);
    }

    /**
     * 开启、关闭某一个硬件的数据ECG 波形采集
     *
     * @param isSwitch the is switch
     */
    public void switchEcgWave(boolean isSwitch) {
        Message message = Message.obtain(null,BleService.MSG_ECG_WAVE_TYPE);
        message.obj = isSwitch;
        sendMessage(message);
    }


    /**
     * 更改设备名称
     *
     * @param name the name
     */
    public void changeBleName(String name){
        Message message = Message.obtain(null,BleService.MSG_BLE_NAME);
        message.obj = name;
        sendMessage(message);
    }

    /**
     * 写自定义控制命令码
     */
    public void writeCmd( short cmd, byte[] data){
        Message message = Message.obtain(null,BleService.MSG_TEST_WRITE_CMD);
        message.arg2 = cmd;
        message.obj = data;
        sendMessage(message);
    }

    /**
     * 写自定义控制命令码
     */
    public void readDeviceRssi( ){
        Message message = Message.obtain(null,BleService.MSG_READ_RSSI);
        sendMessage(message);
    }

    /**
     *  **********************************S02离线模式相关方法**********************************
     */

    /**
     * 开启、关闭某一个S02产品的离线模式
     */
    public void switchOfflineMode(boolean isSwitch) {
        Message message = Message.obtain(null,BleService.MSG_OFFLINE_MODE);
        message.obj = isSwitch;
        sendMessage(message);
    }

    /**
     * 读命令 读取数据采集状态 1byte
     */
    public void offlineWorkStatus(){
        Message message = Message.obtain(null, MSG_OFFLINE_WORK_STATUS);
        sendMessage(message);
    }

    /**
     * 读命令 查询是否有历史离线数据，将返回有几段数据，1byte
     */
    public void offlineDataAsk(){
        Message message = Message.obtain(null, MSG_OFFLINE_DATA_ASK);
        sendMessage(message);
    }

    /**
     * 写命令 开始数据上传
     */
    public void offlineDataUpload(){
        Message message = Message.obtain(null,BleService.MSG_OFFLINE_DATA_UPL);
        sendMessage(message);
    }


    /**
     * 写命令 擦除所有历史数据
     */
    public void offlineDataRest(){
        Message message = Message.obtain(null,BleService.MSG_OFFLINE_DATA_ERS);
        sendMessage(message);
    }


    /**
     * 自动重连Core
     *
     * @param sn Core的sn号
     */
    public void autoConnectBle(String sn){
        if (dfuHelperS02 == null){
            Message message = Message.obtain(null,BleService.MSG_AUTO_CONNECT_SN);
            message.obj = sn;
            sendMessage(message);
        }
    }


    public void autoConnectBle(DeviceInfo info){
        if (dfuHelperS02 == null){
            Message message = Message.obtain(null,BleService.MSG_AUTO_CONNECT_MAC);
            message.obj = info;
            sendMessage(message);
        }
    }


    /**
     * 停止自动重连Core
     *
     */
    public void stopAutoConnectBle(){
        Message message = Message.obtain(null,BleService.MSG_STOP_AUTO_CONNECT);
        sendMessage(message);
    }

    // 解除绑定
    public void unBondDevice(){
        msgDeviceDisconnect(-1);
    }


    // 后台运行
    public void enableBleSerBackground(@DrawableRes int icon,String title,boolean isEnable){
        Message message = Message.obtain(null,BleService.MSG_BACKGROUND);
        message.arg1 = isEnable ? 1 : 0;
        message.arg2 = icon;
        message.obj = title;
        sendMessage(message);
    }

    /**
     *  ************************* BLE数据的回调 ***************************
     */

    /**
     * 心率、呼吸数据
     *
     * @param code the code
     * @param dm   the dm
     */
    public void execCallBack(int code, int dm) {
        if (registedView != null){
            registedView.bleDataCallBack(code,dm);
        }
    }

    private static class IncomingHandler extends Handler {
        SoftReference<BleConnectionManger> softReference;
        IncomingHandler(BleConnectionManger bleConnectionManger) {
            softReference = new SoftReference<>(bleConnectionManger);
        }

        @Override
        public synchronized void handleMessage(Message msg) {
            try {
                BleConnectionManger manger;
                if (softReference == null) {
                    return;
                }else {
                    manger = softReference.get();
                    if (manger == null) {
                        return;
                    }
                }
                if (!manger.connectionMap.isEmpty() || !manger.stateMap.isEmpty()) {
                    switch (msg.what) {
                        case RE_SCAN_DEVICE:
                            ArrayList<MyBleDevice> myBleDevices = (ArrayList<MyBleDevice>)msg.obj;
                            manger.msgScanBleResult(myBleDevices);
                            break;
                        case RE_DEVICE_DISCONNECT_STATE:
                            manger.msgDeviceDisconnect(msg.arg1);
                            break;
                        case RE_CORE_CONNECT:
                            DeviceInfo device = (DeviceInfo) msg.obj;
                            manger.msgReConnectDevice(device);
                            break;
                        case RE_POW_LEVEL:
                            byte[] data = (byte[]) msg.obj;
                            manger.msgPowerLevel(data);
                            break;
                        case BleService.RE_CORE_HEART_DATA_ERROR:
                            // ECG脱落
                            manger.msgHeartDataError();
                            break;
                        case BleService.RE_BODY_ECG_WAVE_DATA:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                EcgWaveOriginalDataUtils.getInstance().addWave((EcgWaveFrameData) msg.obj);
                            }
                            break;
                        case BleService.RE_HRV_RESULT:
//                            msgBodyRateWaveData(BleService.DeviceType.values()[msg.arg1] ,(byte[]) msg.obj);
                            break;
                        case BleService.RE_CORE_MODLE:
                            manger.msgCoreModule((byte[]) msg.obj);
                            break;
                        case BleService.RE_MSG_GARMENT_DETAILS:
                            break;
                        case BleService.RE_MSG_S02_TEST:
                            break;
                        case BleService.RE_BLE_WRITE_NAME_SUCCEED:
                            manger.msgReNameSucceed();
                            break;
                        case BleService.RE_BODY_DATA:
                            manger.execCallBack(msg.arg1,msg.arg2);
                            break;
                        case BleService.RE_MSG_BLE_STATE_CHANGE:
                            manger.msgBleState(msg.arg1);
                            break;
                        case BleService.RE_MSG_CONNECT_DEVICE_DFU_STATE:
                            manger.msgProcessDfuState((String) msg.obj);
                            break;
                        case MSG_OFFLINE_WORK_STATUS:
                            manger.msgOfflineWorkStatus((byte) msg.obj);
                            break;
                        case BleService.MSG_OFFLINE_DATA_ASK:
                            if (manger.offlineDataAskLinster != null){
                                manger.offlineDataAskLinster.onStatusBack((Byte) msg.obj > 0);
                            }
                            break;
                        case BleService.RE_BODY_RATE_OFFLINE_DATA_ERROR:
                            manger.handleOfflineDataError(msg.arg1);
                            break;
                        case BleService.RE_BODY_RATE_OFFLINE_DATA_PROCESS:
                            manger.handleOfflineDataProcess(msg.arg1);
                            break;
                        case BleService.RE_BODY_RATE_OFFLINE_DATA_END:
                            manger.handleOfflineDataEnd();
                            break;
                        case BleService.RE_READ_RSSI:
                            manger.msgRssi(msg.arg1,msg.arg2);
                            break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void msgCoreModule(byte[] data){
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            connectionInterface.bleCoreModule(data[0]);
        }
    }

    /**
     * 心率检测脱落
     */
    private void msgHeartDataError() {
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            connectionInterface.bleHeartDataError();
        }
    }

    /**
     * 读取电量和电量改变的回调
     * @param data 电量
     */
    private void msgPowerLevel(byte[] data) {
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            connectionInterface.blePowerLevel(data[0]);
        }
        for (Object o : stateMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectStateInterface stateInterface = (BleConnectStateInterface) entry.getValue();
            stateInterface.blePowerLevel(data[0]);
        }
    }

    /**
     * 重新连接的回调
     * @param device 设备详情
     */
    private void msgReConnectDevice(DeviceInfo device) {
        if (null!=mBPDfuListener && checkDeviceVersion(device)) {
            // TODO 留个彩蛋
            msgReConnectDevicesDfu();
        }else {
            for (Object o : connectionMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
                connectionInterface.bleReConnectDevice(device);
            }
            normalConnectStatus();
        }
    }

    /**
     * 连接时要升级，返回空
     */
    private void msgReConnectDevicesDfu(){
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            // 这里需要升级时 返回对象为空  不是很合理
            connectionInterface.bleReConnectDevice(null);
        }
    }

    /**
     * 断开连接的回调
     * @param arg1
     */
    private void msgDeviceDisconnect(int arg1) {
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            connectionInterface.bleDeviceDisconnect(arg1);
        }
        for (Object o : stateMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectStateInterface stateInterface = (BleConnectStateInterface) entry.getValue();
            stateInterface.bleDeviceDisconnect(arg1);
        }
    }

    /**
     * 搜索返回的回调
     * @param lists 结果
     */
    private void msgScanBleResult(ArrayList<MyBleDevice> lists) {
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            connectionInterface.reDeviceList(lists);
        }
    }

    private void msgReNameSucceed(){
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            connectionInterface.reReNameSucceed();
        }
    }

    private void msgBleState(int arg1){
        if (arg1 == 1) { // 蓝牙打开
            for (Object o : connectionMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
                connectionInterface.reBleStateOn();
            }
        }else if (arg1 == 2){ // 蓝牙关闭
            if (null != mBPDfuListener) {
                mBPDfuListener.onBleOff();
            }
        }
    }


    private boolean checkDeviceVersion(DeviceInfo device){
        try {
            if (Integer.parseInt(device.swVn) > 276){
                // 当前要升级固件版本大于等于1.14 才升级 BootLoader
                if (BootLoaderLocalConfig.checkBootLoader(device)){
                    String newBtFileNameByHw = BootLoaderLocalConfig.getNewBtFileNameByHw(Integer.parseInt(device.hwVn));
                    if (newBtFileNameByHw!=null && newBtFileNameByHw.length()>0){
                        startAssetsDfu(DfuHelperS02.TYPE_BOOTLOADER,newBtFileNameByHw,device);
                        return true;
                    }
                }
            }
            // 先使用 SD卡的判断
            DfuUpdateInfo dfuUpdateInfo = DfuNetConfig.getDfuUpdateInfo(device);
            int localUpSwVn = DfuLocalConfig.getNewDfuVersionByHw(Integer.parseInt(device.hwVn));
            if (dfuUpdateInfo!=null && Integer.parseInt(dfuUpdateInfo.swVn)>localUpSwVn){
                if (Integer.parseInt(device.swVn) < Integer.parseInt(dfuUpdateInfo.swVn)) {
                    startSdCardDfu(DfuHelperS02.TYPE_DFU, dfuUpdateInfo.filePath, device);
                    return true;
                }
            }else {
                String newSwName = DfuLocalConfig.getNewDfuFileNameByHw(device);
                if (newSwName!=null && newSwName.length()>0){
                    // 需要升级 固件
                    startAssetsDfu(DfuHelperS02.TYPE_DFU,newSwName,device);
                    return true;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private void startAssetsDfu(int type, String assetsName, DeviceInfo device) {
        if (null != mBPDfuListener) {
            dfuHelperS02 = new DfuHelperS02(application, type, mBPDfuListener);
            int status = dfuHelperS02.setAssetsZipFile(assetsName, device);
            mBPDfuListener.onStart(status, device);
        }
    }

    private void startSdCardDfu(int type, String filePath, DeviceInfo device) {
        if (null != mBPDfuListener) {
            dfuHelperS02 = new DfuHelperS02(application, type, mBPDfuListener);
            int status = dfuHelperS02.setSdCardZipFile(filePath, device);
            mBPDfuListener.onStart(status, device);
        }
    }

    private void msgProcessDfuState(String sn){
        msgReConnectDevicesDfu();

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.sn = sn;
        deviceInfo.swVn = "0";
        String substring = sn.substring(2,4);
        try{
            int v = Integer.parseInt(substring);
            // 兼容早风模式的SN号 早风：ZF11025634
            if (v == 6){
                deviceInfo.hwVn = "6";   // 硬件版本0.04
            }else if (v == 4){
                deviceInfo.hwVn = "4";   // 硬件版本0.06
            }else if (v == 10){
                deviceInfo.hwVn = "256"; // 硬件版本1.00
            }else if (v == 11){
                deviceInfo.hwVn = "257"; // 硬件版本1.00
            }else if (v < 4){
                deviceInfo.hwVn = "2";
            }
        }catch (Exception e){
            deviceInfo.hwVn = "4";
        }

        // 先使用 SD卡的判断 使用网络和APP包内的升级包比较

        DfuUpdateInfo dfuUpdateInfo = DfuNetConfig.getDfuUpdateInfo(deviceInfo);
        int localUpSwVn = DfuLocalConfig.getNewDfuVersionByHw(Integer.parseInt(deviceInfo.hwVn));
        if (dfuUpdateInfo!=null && Integer.parseInt(dfuUpdateInfo.swVn)>localUpSwVn){
            startSdCardDfu(DfuHelperS02.TYPE_DFU,dfuUpdateInfo.filePath,deviceInfo);
        }else {
            String newSwName = DfuLocalConfig.getNewDfuFileNameByHw(deviceInfo);
            if (newSwName!=null && newSwName.length()>0){
                startAssetsDfu(DfuHelperS02.TYPE_DFU,newSwName,deviceInfo);
            }
        }
    }

    private void msgOfflineWorkStatus(byte b){
        boolean isOffline = b==0x02;
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            connectionInterface.reOfflineStatus(isOffline);
        }
    }

    private void msgRssi(int rssi, int status){
        for (Object o : connectionMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            BleConnectionInterface connectionInterface = (BleConnectionInterface) entry.getValue();
            connectionInterface.reRssi(rssi,status);
        }
    }


    private void handleOfflineDataError(int error){
//        public static final int OFFLINE_DATA_ERROR_TIMEOUT = 1;          // S02离线数据传输超时
//        public static final int OFFLINE_DATA_ERROR_CRC_ERROR = 2;          // S02离线数据传输CRC校验失败
//        public static final int OFFLINE_DATA_ERROR_OTHER = 3;          // S02离线数据失败 传输出错
//        public static final int OFFLINE_DATA_ERROR_PARSER = 4;          // S02离线数据失败 解析出错
//        Log.d("wsh","传输error： " + error );
        if (transLinster != null){
            transLinster.OfflineUploadError(error);
        }
    }

    private void handleOfflineDataProcess(int process){
//        Log.d("wsh","传输进度： " + process + " %");
        if (transLinster != null){
            transLinster.offlineUploadProcess(process);
        }
    }

    private void handleOfflineDataEnd(){
//        Log.d("wsh","传输进度： " + process + " %");
        if (transLinster != null){
            transLinster.offlineUploadEnd();
        }
    }

    public void setOfflineDataStatusLinster(OfflineDataAskLinster offlineDataAskLinster){
        this.offlineDataAskLinster = offlineDataAskLinster;
    }

    private OfflineDataAskLinster offlineDataAskLinster;

    public interface OfflineDataAskLinster{
        void onStatusBack(boolean hasOfflineData);
    }


    public void setOfflineDataUploadLinster(OfflineDataUploadListener transLinster){
        this.transLinster = transLinster;
    }

    private OfflineDataUploadListener transLinster;

    public interface OfflineDataUploadListener{
        void offlineUploadProcess(int process);
        void OfflineUploadError(int err);
        void offlineUploadEnd();
    }
}
