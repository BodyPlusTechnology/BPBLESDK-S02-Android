package cc.bodyplus.sdk.ble.manger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cc.bodyplus.sdk.ble.parse.BPDataParser;
import cc.bodyplus.sdk.ble.parse.EcgDataParser;
import cc.bodyplus.sdk.ble.parse.EcgDataParserListener;
import cc.bodyplus.sdk.ble.parse.HrBrErrBean;
import cc.bodyplus.sdk.ble.parse.OfflineDataCacheRef;
import cc.bodyplus.sdk.ble.parse.OfflineDataParser;
import cc.bodyplus.sdk.ble.parse.OfflineDataParserListener;
import cc.bodyplus.sdk.ble.parse.OfflineResultData;
import cc.bodyplus.sdk.ble.parse.WaveDataParser;
import cc.bodyplus.sdk.ble.parse.WaveDataParserListener;
import cc.bodyplus.sdk.ble.utils.BleCmdConfig;
import cc.bodyplus.sdk.ble.utils.BleUtils;
import cc.bodyplus.sdk.ble.utils.BleWriteData;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.TLUtil;
import cc.bodyplus.sdk.ble.utils.UUIDUtils;
import cc.bodyplus.sdk.ble.wave.EcgWaveFrameData;

import static cc.bodyplus.sdk.ble.manger.BleService.MSG_OFFLINE_DATA_ASK;
import static cc.bodyplus.sdk.ble.manger.BleService.MSG_OFFLINE_WORK_STATUS;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_BLE_WRITE_NAME_SUCCEED;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_BODY_DATA;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_BODY_ECG_WAVE_DATA;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_BODY_RATE_OFFLINE_DATA_END;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_BODY_RATE_OFFLINE_DATA_ERROR;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_BODY_RATE_OFFLINE_DATA_PROCESS;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_CORE_HEART_DATA_ERROR;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_HRV_RESULT;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_POW_LEVEL;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_READ_RSSI;
import static cc.bodyplus.sdk.ble.manger.BleService.RE_SWITCH_DATA_ACK;


/**
 * Created by shihu.wang on 2017/3/24.
 * Email shihu.wang@bodyplus.cc
 * <p>
 * 处理 S02 有氧版
 */
public class GattS02 extends GattBodyPlus{
    private DeviceInfo deviceInfo;
    private Context mContext;
    private BluetoothGatt mBluetoothGatt;
    private GattCallBack gattCallBack;
    private BluetoothGattService cmdService, dataService, batteryService;
    //cmdService
    private BluetoothGattCharacteristic cmdWriteCharacter, cmdRespondCharacter, logReadCharacter, batteryCharacter;
    //dataService
    private BluetoothGattCharacteristic dataWriteCharacter, dataRespondCharacter,dataWaveCharacter;
    private BluetoothGattCharacteristic offlineWriteCharacter, offlineReadCharacter;
    private Queue<BleWriteData> sWriteQueue = new ConcurrentLinkedQueue<>();

    private int setTimeCount ;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isStartConnect = false;
    private boolean isDeviceConnection = false;//设备连接是否成功
    private GattTimeOutHelper timeOutHelper;
    private Handler handler;
    private String mMacAddress;
    private boolean cancelBond = false;
    private boolean isReConnect;

    GattS02(Context mContext,DeviceInfo deviceInfo, GattCallBack gattCallBack) {
        this.deviceInfo = deviceInfo;
        this.mContext = mContext;
        this.gattCallBack = gattCallBack;

        GattTimeOutHelper.GattTimeOutListener timeOutListener = new GattTimeOutHelper.GattTimeOutListener() {
            @Override
            public void reSendData(BleWriteData latSendData) {
                write(latSendData);
            }

            @Override
            public void timeout() {
                sWriteQueue.clear();
                nextWrite();
            }
        };
        timeOutHelper = new GattTimeOutHelper(timeOutListener);
        handler = new Handler(mContext.getMainLooper());
    }

    private void connectRemoteDevice(){
        BluetoothManager bluetoothManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter adapter = bluetoothManager.getAdapter();
            if (!adapter.isEnabled()){
                return;
            }
            BluetoothDevice remoteDevice = adapter.getRemoteDevice(mMacAddress);
            mBluetoothGatt = remoteDevice.connectGatt(mContext, false, mBluetoothCallback);
            LogUtil.wshLog().d(" connectGatt :" + mMacAddress);
        }
    }

    private void reConnect(){
        if (mMacAddress != null) {
            connectRemoteDevice();
        }
    }

    @Override
    protected void autoReConnect(String mac) {
        if(!isStartConnect) {
            isReConnect = true;
            cancelBond = false;
            mMacAddress = mac;
            setTimeCount = 0;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    connectRemoteDevice();
                }
            });
        }
    }

    @Override
    protected void connect(String macAddress) {
        disconnect();
        cancelBond = false;
        mMacAddress = macAddress;
        isStartConnect = true;
        setTimeCount = 0;
        handler.post(new Runnable() {
            @Override
            public void run() {
                connectRemoteDevice();
            }
        });

//        LogUtil.wshLog().d(" connectGatt :" + mMacAddress);
    }

    @Override
    public void connect(final BluetoothDevice bluetoothDevice){

    }

    @Override
    public void disconnect(){
        try {
            if (mBluetoothGatt != null){
                if (isDeviceConnection) {
                    mBluetoothGatt.disconnect();
                }else {
                    boolean isrefresh = refreshDeviceCache();
                    LogUtil.wshLog().d(" 刷新缓存 isrefresh " + isrefresh);
                    mBluetoothGatt.close();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void cancelBond(){
        cancelBond = true;
        disconnect();
    }

    private boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                Method localMethod = mBluetoothGatt.getClass().getMethod(
                        "refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = (Boolean) localMethod.invoke(
                            mBluetoothGatt, new Object[0]);
                    return bool;
                }
            } catch (Exception localException) {

            }
        }
        return false;
    }

    @Override
    public void readRemoteRssi() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    @Override
    public void switchOfflineMode(boolean isOpen) {
        byte[] data = new byte[7];
        if (isOpen) {
            data[0] = 2;
            data[2] = 1;
        }
        cmdWriteCmd(BleCmdConfig.BLE_CMD_UPLOAD_DATA,data);
    }


    @Override
    public void offlineDataAsk() {
        cmdReadCmd(BleCmdConfig.BLE_CMD_UPLOAD_OLDDATA, (byte) BleCmdConfig.HistoryDataCmdVal.DATA_ASK);
    }

    @Override
    public void offlineDataPrepareUpload() { // 先去获取详情
        cmdReadCmd(BleCmdConfig.BLE_CMD_UPLOAD_OLDDATA, (byte) BleCmdConfig.HistoryDataCmdVal.DATA_INF);
    }

    @Override
    public void offlineDataEnd() {
        cmdWriteCmd(BleCmdConfig.BLE_CMD_UPLOAD_OLDDATA,new byte[]{BleCmdConfig.HistoryDataCmdVal.DATA_END});
    }

    @Override
    public void offlineDataRest() {
        cmdWriteCmd(BleCmdConfig.BLE_CMD_UPLOAD_OLDDATA,new byte[]{BleCmdConfig.HistoryDataCmdVal.DATA_ERS});
    }

    @Override
    public void offlineDataStartUpload() {
        cmdWriteCmd(BleCmdConfig.BLE_CMD_UPLOAD_OLDDATA,new byte[]{BleCmdConfig.HistoryDataCmdVal.DATA_UPL});
    }

    @Override
    public void offlineWorkStatus() {
        cmdReadCmd(BleCmdConfig.BLE_CMD_UPLOAD_DATA, (byte) BleCmdConfig.HistoryDataCmdVal.WORK_STATUS);
    }

    @Override
    public void fetchPowerLevel() {
        readPowerLevel();
    }

    @Override
    public void fetchCoreMode() {
        readCoreMode();
    }

    @Override
    public void switchEcgChannel(boolean isOpen) {
        byte[] data = new byte[7];
        if (isOpen) {
            data[0] = 1;
            data[2] = 1;
        }
        cmdWriteCmd(BleCmdConfig.BLE_CMD_UPLOAD_DATA,data);
    }

    @Override
    public void switchEcgWave(boolean isOpen) {
        byte[] data = new byte[7];
        if (isOpen) {
            data[0] = 3;
            data[2] = 1;
        }
        cmdWriteCmd(BleCmdConfig.BLE_CMD_UPLOAD_DATA,data);
        if (isOpen) {
            WaveDataParser.init(waveDataParserListener);
        }else {
            WaveDataParser.calcHrv();
        }
    }

    @Override
    public void deviceReName(String name) {
        cmdWriteCmd(BleCmdConfig.BLE_CMD_USER_NAME, name.getBytes());
    }

    @Override
    public void startDfu() {
        cmdWriteCmd(BleCmdConfig.BLE_CMD_DFU_JUMP_LENGTH, new byte[]{1});
    }

    @Override
    public void testWriteCmd(short cmd, byte[] data) {
        cmdWriteCmd(cmd,data);
    }

    @Override
    public void normalConnectStatus() {
        readPowerLevel();
//        readCoreMode();
        notificationDataCharacter();
        offlineWorkStatus();
    }

    private void reConnectSucceed(){
        isDeviceConnection = true;
        gattCallBack.reConnectSucceed(deviceInfo);
        // 如果需要升级 则先不读取电量和订阅通知
//        readPowerLevel();
//        readCoreMode();
//        notificationDataCharacter();
    }

    private void sendMessage(Message message){
        gattCallBack.handleMessage(message);
    }

    private synchronized void write(BleWriteData bData) {
        LogUtil.wshLog().d("write -----   isEmpty()："+sWriteQueue.isEmpty()+"    sIsWriting :"+ timeOutHelper.getIsWriting());
        if (timeOutHelper.getIsWriting()){
            sWriteQueue.add(bData);
        }else {
            timeOutHelper.setIsWriting(true);
            if (sWriteQueue.isEmpty()){
                doWrite(bData);
            }else {
                doWrite(sWriteQueue.poll());
                sWriteQueue.add(bData);
            }
        }
    }

    private synchronized void nextWrite() {
        LogUtil.wshLog().d(" nextWrite ------ isEmpty()："+ sWriteQueue.isEmpty()+"    sIsWriting :"+ timeOutHelper.getIsWriting());
        if (!sWriteQueue.isEmpty() && !timeOutHelper.getIsWriting()) {
            timeOutHelper.setIsWriting(true);
            doWrite(sWriteQueue.poll());
        }
    }

    private void doWrite(final BleWriteData bDatas) {
        if (mBluetoothGatt == null) {
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (bDatas.write_type == BleWriteData.CMD ){ // cmd write
                    if (cmdWriteCharacter != null){
                        cmdWriteCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        cmdWriteCharacter.setValue(bDatas.write_data);
                        mBluetoothGatt.writeCharacteristic(cmdWriteCharacter);
                        LogUtil.wshLog().d("CMD 写数据…… ： " + " cmdID: " + bDatas.write_data[1] + " " + bDatas.write_data[2]);
                        timeOutHelper.setLatSendData(bDatas);
                        timeOutHelper.init();
                    }
                }else if (bDatas.write_type == BleWriteData.OFFLINE ){ //  离线
                    if (offlineWriteCharacter != null) {
                        offlineWriteCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        offlineWriteCharacter.setValue(bDatas.write_data);
                        mBluetoothGatt.writeCharacteristic(offlineWriteCharacter);
                        LogUtil.wshLog().d("offline 写数据…… ： " + " cmdID: " + bDatas.write_data[1] + " " + bDatas.write_data[2]);
//                        timeOutHelper.setLatSendData(bDatas);
//                        timeOutHelper.init();
                    }
                }else if (bDatas.write_type == BleWriteData.DATA_FRAME ){ //  数据通道状态帧
                    if (dataWriteCharacter != null) {
                        dataWriteCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        dataWriteCharacter.setValue(bDatas.write_data);
                        mBluetoothGatt.writeCharacteristic(dataWriteCharacter);
                        LogUtil.wshLog().d("data_frame 写数据…… ： " + " cmdID: " + bDatas.write_data[1] + " " + bDatas.write_data[2]);
//                        timeOutHelper.setLatSendData(bDatas);
//                        timeOutHelper.init();
                    }
                }else if (bDatas.write_type == BleWriteData.DESCRIP_WRITE ){ //  BluetoothGattDescriptor
                    mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) bDatas.object);
                }else {
                    timeOutHelper.setIsWriting(false);
                    nextWrite();
                }
            }
        });
    }
    private void disConnectDevice(int status){
        LogUtil.wshLog().d("BLE s02 连接断开……");
        disconnect();
        if (mBluetoothGatt != null){
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        gattCallBack.reDisconnect(status);
        sWriteQueue.clear();
        timeOutHelper.closeThread();
        cmdService = null;
        dataService = null;
        batteryService = null;
        cmdWriteCharacter  = null;
        cmdRespondCharacter = null;
        logReadCharacter = null;
        batteryCharacter = null;
        dataWriteCharacter = null;
        dataRespondCharacter = null;
        offlineReadCharacter = null;
        offlineWriteCharacter = null;
    }

    private BluetoothGattCallback mBluetoothCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            /**
             *  Create by shihoo.wang@bodyplus 2019/03/16.
             * 1.过使用if(gatt==null)来判断gatt是否被创建过，如果创建过就使用gatt.connect();重新建立连接。
             *   但是在这种情况下测试的结果是重新连接需要花费很长的时间。
             * 解决办法是通过gatt = device.connectGatt(this, false, gattCallback);建立一个新的连接对象，很明显这样的速度要比上一种方法快很多
             * 然而，多次创建gatt连接对象的直接结果是创建过6个以上gatt后就会再也连接不上任何设备，原因应该是android中对BLE限制了同时连接的数量为6-8个
             * 解决办法是在每一次重新连接时都执行一次gatt.close();关闭上一个连接。
             * 2.为什么不在gatt.disconnect();后加一条gatt.close();呢，原因是如果立即执行gatt.close();会导致gattCallback无法收到STATE_DISCONNECTED的状态。
             * 当然，最好的办法是在gattCallback收到STATE_DISCONNECTED后再执行gatt.close();，这样逻辑上会更清析一些。
             */
            LogUtil.wshLog().d(" 连接-- s02 onConnectionStateChange……  newState: STATE_CONNECTED ："+
                    newState +" status :"+status);
            if (newState == BluetoothGatt.STATE_CONNECTED){
                isStartConnect = false;
                isReConnect = false;
                if (cancelBond){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (cancelBond) {
                                mBluetoothGatt.disconnect();
                            }
                        }
                    },1000);
                }else {
                    mBluetoothGatt.discoverServices();
                }
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                if (!cancelBond){
                    if (isStartConnect){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isStartConnect && !cancelBond) {
                                    mBluetoothGatt.connect();
                                    disconnect();
                                    reConnect();
                                }
                            }
                        },1000);
                        return;
                    }else if (isReConnect){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isReConnect && !cancelBond && !isStartConnect) {
                                    mBluetoothGatt.connect();
                                    disconnect();
                                    reConnect();
                                }
                            }
                        },5 * 1000);
                        return;
                    }
                }
                isDeviceConnection = false;
                disConnectDevice(status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                cmdService = mBluetoothGatt.getService(UUIDUtils.CMD_SERVICE);
                cmdRespondCharacter = cmdService.getCharacteristic(UUIDUtils.CMD_READ_CHARACTERISTIC); // 要订阅
                cmdWriteCharacter = cmdService.getCharacteristic(UUIDUtils.CMD_WRITE_CHARACTERISTIC);
                logReadCharacter = cmdService.getCharacteristic(UUIDUtils.CMD_LOG_READ_CHARACTERISTIC);

                dataService = mBluetoothGatt.getService(UUIDUtils.DATA_SERVICE);
                dataRespondCharacter = dataService.getCharacteristic(UUIDUtils.DATA_READ_CHARACTERISTIC); // 要订阅
                dataWriteCharacter = dataService.getCharacteristic(UUIDUtils.DATA_WRITE_CHARACTERISTIC);
                dataWaveCharacter = dataService.getCharacteristic(UUIDUtils.DATA_WAVE_CHARACTERISTIC);
                offlineWriteCharacter = dataService.getCharacteristic(UUIDUtils.OFFLINE_WRITE_CHARACTERISTIC);
                offlineReadCharacter = dataService.getCharacteristic(UUIDUtils.OFFLINE_READ_CHARACTERISTIC);

                batteryService = mBluetoothGatt.getService(UUIDUtils.BATTERY_SERVICE);
                batteryCharacter = batteryService.getCharacteristic(UUIDUtils.BATTERY_LEVEL_CHARACTERISTIC); // 要订阅

                //清空缓存
                sWriteQueue.clear();
                timeOutHelper.setIsWriting(false);

                //开启事件订阅
                enableNotification(true, mBluetoothGatt, cmdRespondCharacter);
                nextWrite();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            if (uuid.equals(UUIDUtils.OFFLINE_WRITE_CHARACTERISTIC) ||  // 离线数据上传
                    uuid.equals(UUIDUtils.DATA_WRITE_CHARACTERISTIC)) {  // 设备数据上传
                timeOutHelper.setIsWriting(false);
                nextWrite();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
//            LogUtil.wshLog().d("收到   byte[] data ： "+ BleUtils.dumpBytes(data));
            if (characteristic.getUuid().equals(UUIDUtils.OFFLINE_READ_CHARACTERISTIC)) { // 离线数据上传
                handleOfflineDataAvailable(data);
            } else if (characteristic.getUuid().equals(UUIDUtils.DATA_WAVE_CHARACTERISTIC)) { // 心率版ECG波形上传
                handleWaveDataAvailable(data);
            } else if (characteristic.getUuid().equals(UUIDUtils.BATTERY_LEVEL_CHARACTERISTIC)) { // 电池电量
                handleBattery(data);
            } else if (characteristic.getUuid().equals(UUIDUtils.DATA_READ_CHARACTERISTIC)) { // 设备数据上传
                handleEcgDataAvailable(data);
            } else if (characteristic.getUuid().equals(UUIDUtils.CMD_LOG_READ_CHARACTERISTIC)) { // 设备日志上传
                gattCallBack.handleLogData(data);
            } else {
                if (TLUtil.validateCRC8(data)) { // 通知返回码 包括命令码返回 STM码 以及数据通道
                    handleCharacteristicCmdData(data);
                } //CRC8码验证失败
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            timeOutHelper.setIsWriting(false);
            if (sWriteQueue.isEmpty()) {
                LogUtil.wshLog().d("BLE onDescriptorWrite…… 完成");
                if (isDeviceConnection){
                    return;
                }
                // 判断Core 的类型 如果是有氧版的 就不需要验证Core的APP状态
//                checkSecretKey();
                setDeviceTime();
            } else {
                LogUtil.wshLog().d("BLE onDescriptorWrite……写下一个");
                nextWrite();
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Message msg = Message.obtain(null, RE_READ_RSSI);
            msg.arg1 = rssi;
            msg.arg2 = status;
            sendMessage(msg);
        }

    };

    /**
     * 开启数据通道的通知
     */
    private void notificationDataCharacter(){
        enableNotification(true, mBluetoothGatt, dataRespondCharacter);
        enableNotification(true, mBluetoothGatt, batteryCharacter);
        enableNotification(true, mBluetoothGatt, offlineReadCharacter);
        enableNotification(true, mBluetoothGatt, dataWaveCharacter);
        nextWrite();
    }

    /**
     * 命令通道
     * 写命令（无参数）
     */
    private void cmdWriteCmd(short commandId) {
        write(BleUtils.generateWriteWriteData(commandId));
    }

    /**
     * 命令通道
     * 写命令（有参数）
     * @param commandId
     * @param value
     */
    private void cmdWriteCmd(short commandId, byte[] value) {
        write(BleUtils.generateWriteWriteData(commandId,value));
    }

    /**
     * 命令通道
     * 读命令（有参数 = 长度）
     */
    private void cmdReadCmd(short commandId, byte valueLength) {
        write(BleUtils.generateReadWriteData(commandId,valueLength));
    }

    private void enableNotification(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt != null && characteristic != null){
            sWriteQueue.add(BleUtils.generateNotifWriteData(enable,gatt,characteristic));
        }
    }

    private void sendOfflineFrameResponse(){
        write(BleUtils.generateOfflieWriteData((byte) 0xDE, (byte) 0xDE,(byte)0));
    }

    private void sendDataFrameResponse(byte d, byte d2, byte errFlag) {
        if (!isDeviceConnection){
            // 防止没有走完连接流程时，收到状态上报帧
            return;
        }
        write(BleUtils.generateDataFrameBleWriteData(d,d2,errFlag));
    }

    private void handleDataFrameHeader(byte[] data) {
        byte FRAME_FLAG_SUCCESS = 0;
        if (BPDataParser.isTimeStamp(data)) {
            sendDataFrameResponse((byte) 0xD0, (byte) 0xEF, FRAME_FLAG_SUCCESS);
        } else if (BPDataParser.isStateFrame(data)) {
            notifyCoreStateChange(data);
            sendDataFrameResponse((byte) 0xD0, (byte) 0xEE, FRAME_FLAG_SUCCESS);
        } else if (BPDataParser.isHeartBeatFrame(data)) {
            sendDataFrameResponse((byte) 0xD0, (byte) 0xEA, FRAME_FLAG_SUCCESS);
        }
    }

    private void notifyCoreStateChange(byte[] data){
        byte stateNum = data[2];
        byte num = data[3];
        byte [] stateValue = new byte[]{data[3]};
        if (stateNum == 1){ // core位置变化
            gattCallBack.reCoreModule(stateValue);
        } else if (stateNum==4 && num!=0){ // ECG脱落
            Message msg = Message.obtain(null, RE_CORE_HEART_DATA_ERROR);
            sendMessage(msg);
        }
    }

    private void handleWaveDataAvailable(byte[] data){
        WaveDataParser.handleWaveDataAvailable(data);
    }


    private void handleOfflineDataAvailable(byte[] data){
        OfflineDataParser.handleOfflineData(data,offlineDataParserListener);
    }

    private void handleEcgDataAvailable(byte[] data){
        EcgDataParser.handleEcgDataAvailable(data, ecgDataParserListener);
    }

    /**
     * 解析通知返回数据
     * @param data
     */
    private void handleCharacteristicCmdData(byte[] data) {
        if (BleUtils.isCMDReadResponse(data)) {
            handleCMDReadResponse(data);
        } else if (BleUtils.isCMDWriteResponse(data)) {
            handleCMDWriteResponse(data);
        } else {
            succeedReceiveCmd();
        }
    }

    private void handleBattery(byte[] data){
        Message msg = Message.obtain(null, RE_POW_LEVEL);
        msg.obj = data;
        sendMessage(msg);
    }

    private EcgDataParserListener ecgDataParserListener = new EcgDataParserListener() {
        @Override
        public void onHeartValue(int value) {
            Message msg = Message.obtain(null, RE_BODY_DATA);
            msg.arg1 = BleCmdConfig.BLE_HEART_MESSAGE;
            msg.arg2 = value;
            sendMessage(msg);
        }

        @Override
        public void onBreathValue(int value) {
            Message msg = Message.obtain(null, RE_BODY_DATA);
            msg.arg1 = BleCmdConfig.BLE_BREATHING_MESSAGE;
            msg.arg2 = value;
            sendMessage(msg);
        }

        @Override
        public void onHeartError(int value) {
            Message msg = Message.obtain(null, RE_BODY_DATA);
            msg.arg1 = BleCmdConfig.BLE_HEART_ERROR_MESSAGE;
            msg.arg2 = value;
            sendMessage(msg);
        }

        @Override
        public void onBreathError(int value) {
            Message msg = Message.obtain(null, RE_BODY_DATA);
            msg.arg1 = BleCmdConfig.BLE_BREATHING_ERROR_MESSAGE;
            msg.arg2 = value;
            sendMessage(msg);
        }

        @Override
        public void parseDataFrameHeader(byte[] data) {
            handleDataFrameHeader(data);
        }
    };

    private WaveDataParserListener waveDataParserListener = new WaveDataParserListener(){

        @Override
        public void waveData(EcgWaveFrameData mWaveData) {
            Message msg = Message.obtain(null, RE_BODY_ECG_WAVE_DATA);
            msg.obj = mWaveData;
            sendMessage(msg);
        }

        @Override
        public void hrvResult(int m2_hrv_process) {
            Message msg = Message.obtain(null, RE_HRV_RESULT);
            msg.obj = m2_hrv_process;
            sendMessage(msg);
        }
    };

    private OfflineDataParserListener offlineDataParserListener = new OfflineDataParserListener() {

        @Override
        public void offlineDateUploadProcess(int process) {
            Message msg = Message.obtain(null,RE_BODY_RATE_OFFLINE_DATA_PROCESS);
            msg.arg1 = process;
            sendMessage(msg);

        }

        @Override
        public void OfflineDataUploadError(int error) {
            Message msg = Message.obtain(null, RE_BODY_RATE_OFFLINE_DATA_ERROR);
            msg.arg1 = error;
            sendMessage(msg);
        }

        @Override
        public void offlineDataUploadFrameResponse() {
            sendOfflineFrameResponse();
        }

        @Override
        public void offlineDataUploadCompleted(int timeStamp, List<Integer> hrData,
                                               List<Integer> brData,
                                               ArrayList<HrBrErrBean> hrBrErrData) {
            LogUtil.wshLog().d("传输 完成： timeStamp ：" + timeStamp +"  数据长度1 ： "+ hrData.size() +"  数据长度2 ： " +brData.size() );
            OfflineResultData offlineResultData = new OfflineResultData(timeStamp,hrData,brData,hrBrErrData);
            OfflineDataCacheRef.getInstance().setOfflineResultData(offlineResultData);
            offlineDataEnd();
            Message msg = Message.obtain(null,RE_BODY_RATE_OFFLINE_DATA_END);
            sendMessage(msg);
        }

    };


    /**
     * 读命令返回的解析（ CMDRead ）
     * @param data
     */
    private void handleCMDReadResponse(byte[] data) {
       if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_HW_VN)) { // 读取 硬件版本号
            checkCmdReturn(BleCmdConfig.BLE_CMD_HW_VN);
            if (!BleUtils.isCMDReadError(data, BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_HW_VN))) {
                byte[] value = new byte[BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_HW_VN)];
                System.arraycopy(data, 4, value, 0, value.length);
                short s = value[1];
                short t = (short) (s << 8);
                short hw = (short) (t | value[0]);
                deviceInfo.hwVn = hw+"";
                readSwVersion();
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_HW_VN);
            }
        } else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_SW_VN)) { //  读取 固件版本号 ---> 绑定成功后
            checkCmdReturn(BleCmdConfig.BLE_CMD_SW_VN);
            if (!BleUtils.isCMDReadError(data, BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_SW_VN))) {
                byte[] value = new byte[BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_SW_VN)];
                System.arraycopy(data, 4, value, 0, value.length);
                short sw  = (short)(value[1]<<8 | value[0]&0xff);
                deviceInfo.swVn = sw+"";
                readBootVersion();
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_SW_VN);
            }
        } else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_DFU_VN)) { //  读取 BootLoad版本
            checkCmdReturn(BleCmdConfig.BLE_CMD_DFU_VN);
            if (!BleUtils.isCMDReadError(data, BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_DFU_VN))) {
                byte[] value = new byte[BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_DFU_VN)];
                System.arraycopy(data, 4, value, 0, value.length);
                short dfu  = (short)(value[0]&0xff);
                deviceInfo.dfu = dfu+"";
                readAppBootVersion();
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_DFU_VN);
            }
        } else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_DFU_VN_APP)) { //  读取 APP写的BootLoad版本
            checkCmdReturn(BleCmdConfig.BLE_CMD_DFU_VN_APP);
            if (!BleUtils.isCMDReadError(data, BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_DFU_VN_APP))) {
                byte[] value = new byte[BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_DFU_VN_APP)];
                System.arraycopy(data, 4, value, 0, value.length);
                short dfuAPP  = (short)(value[0]&0xff);
                deviceInfo.dfuAPP = dfuAPP+"";
                readCoreType();
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_DFU_VN_APP);
            }
        }else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_CORE_TYPE)) { //  读取 Core的类型
            checkCmdReturn(BleCmdConfig.BLE_CMD_CORE_TYPE);
            if (!BleUtils.isCMDReadError(data, BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_CORE_TYPE))) {
                byte[] value = new byte[BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_CORE_TYPE)];
                System.arraycopy(data, 4, value, 0, value.length);
                deviceInfo.coreType = (byte)(value[0]&0xff);
                reConnectSucceed();
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_DFU_VN_APP);
            }
        } else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_POW_LEV)) {  // 读取电量
            checkCmdReturn(BleCmdConfig.BLE_CMD_POW_LEV);
            if (!BleUtils.isCMDReadError(data, BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_POW_LEV))) {
                byte[] value = new byte[BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_POW_LEV)];
                System.arraycopy(data, 4, value, 0, value.length);
                Message msg = Message.obtain(null, RE_POW_LEVEL);
                msg.obj = value;
                sendMessage(msg);
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_POW_LEV);
            }
        }else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_CORE_MODE)) { //  读取 设备当前位置 上衣 裤子 充电座 ……
            checkCmdReturn(BleCmdConfig.BLE_CMD_CORE_MODE);
            if (!BleUtils.isCMDReadError(data, BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_CORE_MODE))) {
                byte[] value = new byte[BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_CORE_MODE)];
                System.arraycopy(data, 4, value, 0, value.length);
                gattCallBack.reCoreModule(value);
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_CORE_MODE);
            }
        }else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_UPLOAD_OLDDATA)) { // 历史数据上传使能
           checkCmdReturn(BleCmdConfig.BLE_CMD_UPLOAD_OLDDATA);
           if (data.length == BleCmdConfig.HistoryDataLength.DATA_INF_DATA_LENGHT){
               // 读取最新一份离线数据信息
               int timeStamp = (data[4] & 0xFF) | ((data[5] & 0xFF) << 8) | ((data[6] & 0xFF) << 16) | ((data[7] & 0xFF) << 24) ;
               int length = (data[8] & 0xFF) | ((data[9] & 0xFF) << 8) | ((data[10] & 0xFF) << 16) | ((data[11] & 0xFF) << 24) ;
               OfflineDataParser.initOffline(timeStamp,length);
               offlineDataStartUpload();
           }else if (data.length == BleCmdConfig.HistoryDataLength.DATA_ASK_DATA_LENGHT){
               // 获取Core中是否有离线数据
               Message msg = Message.obtain(null, MSG_OFFLINE_DATA_ASK);
               msg.obj = data[4];
               sendMessage(msg);
           }
       }else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_UPLOAD_DATA)) {  // 数据上传总开关
            checkCmdReturn(BleCmdConfig.BLE_CMD_UPLOAD_DATA);
            Message msg = Message.obtain(null, MSG_OFFLINE_WORK_STATUS);
            msg.obj = data[4];
            sendMessage(msg);
        }else{
            succeedReceiveCmd();
        }
    }

    /**
     * 写命令返回的解析（ CMDWrite ）
     * @param data
     */
    private void handleCMDWriteResponse(byte[] data) {
        if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_PASSWORD_CHECK)) { //  写 校验连接验证码
            checkCmdReturn(BleCmdConfig.BLE_CMD_PASSWORD_CHECK);
            if (!BleUtils.isCMDWriteError(data)) {
                setDeviceTime();
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_PASSWORD_CHECK);
            }
        }else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_TIME)) { // 写 时间设置返回值
            checkCmdReturn(BleCmdConfig.BLE_CMD_TIME);
            if (!BleUtils.isCMDWriteError(data)) {
                setTimeCount ++;
                if (setTimeCount < 2){
                    // 第一次设置时间
                    setDeviceTime();
                }else if (setTimeCount == 2) {
                    readHwVersion();
                }
            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_TIME);
            }

        }else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_DFU_JUMP_LENGTH)) { // 写 升级跳转
            checkCmdReturn(BleCmdConfig.BLE_CMD_DFU_JUMP_LENGTH);
        }else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_USER_NAME)) { // 写 修改名称
            checkCmdReturn(BleCmdConfig.BLE_CMD_USER_NAME);
            Message msg = Message.obtain(null, RE_BLE_WRITE_NAME_SUCCEED);
            msg.obj = data;
            sendMessage(msg);
        } else if (BleUtils.isCurrentCommandData(data, BleCmdConfig.BLE_CMD_UPLOAD_DATA)) {  // 写 数据上传总开关
            checkCmdReturn(BleCmdConfig.BLE_CMD_UPLOAD_DATA);

            String msweg = BleUtils.dumpBytes(data);
            Message msg = Message.obtain(null, RE_SWITCH_DATA_ACK);
            msg.obj = msweg;
            sendMessage(msg);
            if (!BleUtils.isCMDWriteError(data)) {

            } else {
                errorReceiveCmd(BleCmdConfig.BLE_CMD_UPLOAD_DATA);
            }
        }else{
            succeedReceiveCmd();
        }
    }

    private void checkCmdReturn(short receiveCmdId){
        if (TLUtil.isCurrentCmdBack(timeOutHelper.getLatSendData().write_data,receiveCmdId)){ //  收到的返回是当前发送的指令
            succeedReceiveCmd();
        }
    }

    private void succeedReceiveCmd(){
        timeOutHelper.setIsWriting(false);
        nextWrite();
    }

    private void errorReceiveCmd(short cmdID){
    }

    /**
     * 读取硬件版本
     */
    private void readHwVersion(){
        cmdReadCmd(BleCmdConfig.BLE_CMD_HW_VN,BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_HW_VN));
    }

    /**
     * 读取固件版本
     */
    private void readSwVersion(){
        cmdReadCmd(BleCmdConfig.BLE_CMD_SW_VN,BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_SW_VN));
    }

    /**
     * 读取BootLoad版本
     */
    private void readBootVersion(){
        cmdReadCmd(BleCmdConfig.BLE_CMD_DFU_VN,BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_DFU_VN));
    }

    /**
     * 读取APP写的BootLoad版本
     */
    private void readAppBootVersion(){
        cmdReadCmd(BleCmdConfig.BLE_CMD_DFU_VN_APP,BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_DFU_VN_APP));
    }

    /**
     * 读取	core类型
     */
    private void readCoreType(){
        cmdReadCmd(BleCmdConfig.BLE_CMD_CORE_TYPE,BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_CORE_TYPE));
    }

    /**
     * 读取电量
     */
    private void readPowerLevel(){
        if (!isDeviceConnection){
            // 防止没有走完连接流程时，收到状态上报帧
            return;
        }
        cmdReadCmd(BleCmdConfig.BLE_CMD_POW_LEV,BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_POW_LEV));
    }

    /**
     * 读取模块位置
     */
    private void readCoreMode(){
        if (!isDeviceConnection){
            // 防止没有走完连接流程时，收到状态上报帧
            return;
        }
        cmdReadCmd(BleCmdConfig.BLE_CMD_CORE_MODE,BleCmdConfig.getPayloadLengthByCommand(BleCmdConfig.BLE_CMD_CORE_MODE));
    }

    private void setDeviceTime() {
        cmdWriteCmd(BleCmdConfig.BLE_CMD_TIME, BleUtils.get_BleCmd_time());
    }

    private void checkSecretKey() {
        byte[] sn = deviceInfo.sn.getBytes();
        if (sn.length<10){
            return;
        }
        byte[] secretKey = BleUtils.generateSNPassWord(sn);
        cmdWriteCmd(BleCmdConfig.BLE_CMD_PASSWORD_CHECK, secretKey);
    }
}
