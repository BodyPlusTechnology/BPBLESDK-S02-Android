package cc.bodyplus.sdk.ble.dfu;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import cc.bodyplus.sdk.ble.manger.BPAerobicDevicesCheckUtils;
import cc.bodyplus.sdk.ble.manger.BleConnectionManger;
import cc.bodyplus.sdk.ble.ota.OTAService;
import cc.bodyplus.sdk.ble.utils.BleCmdConfig;
import cc.bodyplus.sdk.ble.utils.BleConstant;
import cc.bodyplus.sdk.ble.utils.BleUtils;
import cc.bodyplus.sdk.ble.utils.DeviceInfo;
import cc.bodyplus.sdk.ble.utils.FileUtils;
import cc.bodyplus.sdk.ble.utils.UnzipFromAssets;
import cc.bodyplus.sdk.ble.utils.ZipUtils;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;


/**
 * 固件升级的帮助类，用于功能的模块化，对外提供一系列Core固件升级的接口
 * Core固件升级的时候蓝牙连接不需要断开，直接使用升级通道对固件升级
 * 所以这一块的功能也就放在原有的BLE服务类中
 *
 * 2019/3/21 新增BootLoader升级
 *
 * Created by shihu.wang on 2016/11/9.
 * Email shihu.wang@bodyplus.cc
 */
public class DfuHelperS02 {
    public static int TYPE_BOOTLOADER = 1;
    public static int TYPE_DFU = 0;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isFound = false;
    private String mZipPath;
    private String mSn;
    private int mType;
    private DfuListener dfuListener;

    /**
     * Instantiates a new Dfu helper s 02.
     *
     * @param context the context
     */
    public DfuHelperS02(Context context, int type , DfuListener listener){
        dfuListener = listener;
        mType = type;
        mContext = context;
        DfuServiceListenerHelper.registerProgressListener(mContext, mDfuProgressListener);
    }

    public void onDestroy(){
        DfuServiceListenerHelper.unregisterProgressListener(mContext, mDfuProgressListener);
        if (!isFound) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    /**
     * Sets zip file.
     *
     * @param assetsName             the file name
     * @param device                the DeviceInfo
     * @return 0 :准备升级 1：已经是最新版本，无需升级。2：安装包解析异常。3：当前固件信息有误。4：升级文件路径有误。 return mDfuProgressListener 升级的回调
     */
    public int setAssetsZipFile(String assetsName, DeviceInfo device) {
        try {
            mSn = device.sn;
            String firmwareName = "";
            String firmwareVersion = "";
            if (mType == TYPE_DFU){
                FileUtils.deleteFile(BleConstant.UPDATE_STM32_PATH);
                UnzipFromAssets.unZip(mContext, assetsName, BleConstant.UPDATE_STM32_PATH, true);
                String jsonPath = getJsonFileFromSD(BleConstant.UPDATE_STM32_PATH + "/bodydfu.json");
                JSONObject stm32Json = new JSONObject(jsonPath).getJSONObject("stm32dfu");
                firmwareName = stm32Json.getString("firmware_name");
                firmwareVersion = stm32Json.getString("firmware_version");
                mZipPath = BleConstant.UPDATE_STM32_PATH + "/" + firmwareName;
                if (Integer.parseInt(firmwareVersion) > Integer.parseInt(device.swVn)){
                    startUpdate();
                    return 0;
                }else {
                    return 1;
                }
            }else if (mType == TYPE_BOOTLOADER){
                FileUtils.deleteFile(BleConstant.UPDATE_BOOTLOADER_PATH);
                UnzipFromAssets.unZip(mContext,assetsName , BleConstant.UPDATE_BOOTLOADER_PATH, true);
                String jsonPath = getJsonFileFromSD(BleConstant.UPDATE_BOOTLOADER_PATH + "/bodydfu.json");
                JSONObject stm32Json = new JSONObject(jsonPath).getJSONObject("stm32dfu");
                firmwareName = stm32Json.getString("firmware_name");
                firmwareVersion = stm32Json.getString("firmware_version");
                mZipPath = BleConstant.UPDATE_BOOTLOADER_PATH + "/" + firmwareName;
                if (Integer.parseInt(firmwareVersion) > Integer.parseInt(device.dfu)){
                    startUpdate();
                    return 0;
                }else {
                    return 1;
                }
            }else {
                return 2;
            }
        } catch (Exception e) {
            return 2;
        }
    }

    /**
     * Sets zip file.
     *
     * @param filePath             the file name
     * @param device                the DeviceInfo
     * @return 0 :准备升级 1：已经是最新版本，无需升级。2：安装包解析异常。3：当前固件信息有误。4：升级文件路径有误。 return mDfuProgressListener 升级的回调
     */
    public int setSdCardZipFile(String filePath, DeviceInfo device) {
        try {
            mSn = device.sn;
            String firmwareName = "";
            String firmwareVersion = "";
            if (mType == TYPE_DFU){
                FileUtils.deleteFile(BleConstant.UPDATE_STM32_PATH);
                ZipUtils.upZipFile(new File(filePath), BleConstant.UPDATE_STM32_PATH);
                String jsonPath = getJsonFileFromSD(BleConstant.UPDATE_STM32_PATH + "/bodydfu.json");
                JSONObject stm32Json = new JSONObject(jsonPath).getJSONObject("stm32dfu");
                firmwareName = stm32Json.getString("firmware_name");
                firmwareVersion = stm32Json.getString("firmware_version");
                mZipPath = BleConstant.UPDATE_STM32_PATH + "/" + firmwareName;
                if (Integer.parseInt(firmwareVersion) > Integer.parseInt(device.swVn)){
                    startUpdate();
                    return 0;
                }else {
                    return 1;
                }
            }else {
                return 2;
            }
        } catch (Exception e) {
            return 2;
        }
    }

    private void startUpdate(){
        BleConnectionManger.getInstance().startDfuCmd();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                searchDFUDevice();
                BleConnectionManger.getInstance().disconnect();
            }
        }).start();

    }


    private void searchDFUDevice() {
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                // 提示不支持蓝牙设备
            } else {
                isFound = false;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
//                Log.d("wsh","searchDFUDevice");
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!isFound){
                byte[] b = BPAerobicDevicesCheckUtils.geSnBytesByScanRecord(scanRecord);
                if (b != null && b.length > 0) {
                    String deviceSn = BleUtils.byteToChar(b);
                    if(deviceSn.length()>=10) {
                        String sn = deviceSn.substring(0, 10);
                        if (sn.equals(mSn)) {
                            if (BleUtils.isFilterDFUUUID(scanRecord)) {
                                isFound = true;
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                startUp(device);
                            }
                        }
                    }
                }
            }
        }
    };

    private void startUp(final BluetoothDevice device){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final DfuServiceInitiator starter = new DfuServiceInitiator(device.getAddress())
                        .setDeviceName(device.getName())
//                        .setPacketsReceiptNotificationsEnabled(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
//                        .setDisableNotification(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        .setKeepBond(true);
                starter.setZip(mZipPath);
                starter.start(mContext, OTAService.class);
//                Log.d("wsh","startUp");
            }
        }).start();
    }


    private String getJsonFileFromSD(String path) {
        String result = "";
        try {
            FileInputStream fis = new FileInputStream(path);
            BufferedReader bfr = new BufferedReader(new InputStreamReader(fis));
            String line = "";
            while ((line = bfr.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDfuCompleted(final String deviceAddress) {
            BleConnectionManger.getInstance().destroyDfu();
            mContext.stopService(new Intent(mContext, OTAService.class));
            DfuServiceListenerHelper.unregisterProgressListener(mContext, mDfuProgressListener);
            if (dfuListener != null){
                dfuListener.onSucceed();
            }
            if (mType == TYPE_BOOTLOADER){
                // 升级完成后 写标识位
                 BleConnectionManger.getInstance().writeCmd(BleCmdConfig.BLE_CMD_DFU_VN_APP,new byte[]{(byte) BootLoaderLocalConfig.BOOTLOADER_VERSION_005});
            }
        }
        @Override
        public void onDfuAborted(final String deviceAddress) {
//            BleConnectionManger.getInstance().destroyDfu();
//            if (dfuListener != null){
//                dfuListener.onError();
//            }
        }
        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            if (dfuListener != null){
                dfuListener.onProgress(percent);
            }
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            BleConnectionManger.getInstance().destroyDfu();
            mContext.stopService(new Intent(mContext, OTAService.class));
            DfuServiceListenerHelper.unregisterProgressListener(mContext, mDfuProgressListener);
            if (dfuListener != null){
                dfuListener.onError();
            }
        }
    };
}
