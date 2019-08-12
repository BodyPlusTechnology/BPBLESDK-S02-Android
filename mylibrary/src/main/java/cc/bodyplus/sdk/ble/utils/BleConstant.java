package cc.bodyplus.sdk.ble.utils;


import android.os.Environment;

/**
 * Created by shihu.wang on 2017/3/17.
 * Email shihu.wang@bodyplus.cc
 */
public interface BleConstant {
    /**
     * The constant BODYPLUS_PATH.
     */
    String BODYPLUS_PATH = Environment.getExternalStorageDirectory().toString() + "/bodyplus_sdk";
    /**
     * The constant UPDATE_PATH.
     */
    //升级路径
    String UPDATE_PATH = BODYPLUS_PATH + "/update";

    //硬件路径
    String HARDWARE_PATH = BODYPLUS_PATH + "/hardware/";

    //心率日志路径
    String BLE_WAVE_PATH = HARDWARE_PATH + "/ble_wave/";
    /**
     * The constant UPDATE_STM32_PATH.
     */
    //DFU升级路径
    String UPDATE_STM32_PATH = UPDATE_PATH + "/stm32";
    //BootLoader升级路径
    String UPDATE_BOOTLOADER_PATH = BODYPLUS_PATH + "/bootloader_update";
}
