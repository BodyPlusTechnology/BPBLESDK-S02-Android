package cc.bodyplus.sdk.ble.utils;

import java.util.UUID;

/**
 * Created by shihu.wang on 2017/3/17.
 * Email shihu.wang@bodyplus.cc
 */
public class UUIDUtils {
    /**
     * The constant CCC.
     */
//标准关闭或打开通知的UUID。
    public final static UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /**
     * The constant BATTERY_SERVICE_UUID.
     */
    public final static String BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb";
    /**
     * The constant BATTERY_CHAR_UUID.
     */
    public final static String BATTERY_CHAR_UUID = "00002a19-0000-1000-8000-00805f9b34fb";

    /**
     * The constant DEVICE_SERVICE_UUID.
     */
    public final static String DEVICE_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    /**
     * The constant DEVICE_CHAR_UUID.
     */
    public final static String DEVICE_CHAR_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";

    /**
     * The constant WRITE_SERVICE_UUID.
     */
    public final static String WRITE_SERVICE_UUID = "00001805-0000-1000-8000-00805f9b34fb";
    /**
     * The constant WRITE_CHAR_UUID_2A08.
     */
    public final static String WRITE_CHAR_UUID_2A08 = "00002a08-0000-1000-8000-00805f9b34fb";
    /**
     * The constant WRITE_CHAR_UUID_2A09.
     */
    public final static String WRITE_CHAR_UUID_2A09 = "00002a09-0000-1000-8000-00805f9b34fb";


    /**
     * The constant OAD_SERVICE_UUID.
     */
    public final static String OAD_SERVICE_UUID = "f000ffc0-0451-4000-b000-000000000000";

    /**
     * The constant NAME_SERVICE_UUID.
     */
    public final static String NAME_SERVICE_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    /**
     * The constant NAME_CHAR_UUID.
     */
    public final static String NAME_CHAR_UUID = "00002a28-0000-1000-8000-00805f9b34fb";

    /**
     * The constant KEY_SERVICE_UUID.
     */
    public final static String KEY_SERVICE_UUID = "0000c800-0000-1000-8000-00805f9b34fb";
    /**
     * The constant KEY_CHAR_UUID.
     */
    public final static String KEY_CHAR_UUID = "00002902-0000-1000-8000-00805f9b34fb";


    /**
     * The constant HR_SERVICE_UUID.
     */
// 心率GATT服务
    public static final UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");

    /**
     * The constant HR_SENSOR_LOCATION_CHARACTERISTIC_UUID.
     */
// 心率GATT传感器位置特征
    public static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID
            .fromString("00002A38-0000-1000-8000-00805f9b34fb");

    /**
     * The constant HR_CHARACTERISTIC_UUID.
     */
// 心率GATT特征
    public static final UUID HR_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");

    /**
     * The constant CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID.
     */
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /**
     * The constant UUID_KEY_DATA.
     */
    public final static UUID UUID_KEY_DATA =  UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");


    /**
     * The constant BATTERY_SERVICE.
     */
//电池服务
    public static final UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    /**
     * The constant BATTERY_LEVEL_CHARACTERISTIC.
     */
// 电池特征
    public static final UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    /**
     * The constant CMD_SERVICE.
     */
//CMD profile
    public static final UUID CMD_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * The constant CMD_WRITE_CHARACTERISTIC.
     */
    public static final UUID CMD_WRITE_CHARACTERISTIC = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * The constant CMD_READ_CHARACTERISTIC.
     */
    public static final UUID CMD_READ_CHARACTERISTIC = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * The constant CMD_BT_WRITE_CHARACTERISTIC.
     */
    public static final UUID CMD_BT_WRITE_CHARACTERISTIC = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * The constant CMD_BT_READ_CHARACTERISTIC.
     */
    public static final UUID CMD_BT_READ_CHARACTERISTIC = UUID.fromString("6e400012-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * The constant CMD_LOG_READ_CHARACTERISTIC.
     */
    public static final UUID CMD_LOG_READ_CHARACTERISTIC = UUID.fromString("6e400008-b5a3-f393-e0a9-e50e24dcca9e");

    /**
     * The constant DATA_SERVICE.
     */
//Data profile
    public static final UUID DATA_SERVICE = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID DATA_WRITE_CHARACTERISTIC = UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID DATA_READ_CHARACTERISTIC = UUID.fromString("6e400007-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID DATA_WAVE_CHARACTERISTIC = UUID.fromString("6e400013-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID OFFLINE_WRITE_CHARACTERISTIC = UUID.fromString("6e400009-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID OFFLINE_READ_CHARACTERISTIC = UUID.fromString("6e400010-b5a3-f393-e0a9-e50e24dcca9e");

    /**
     * The constant STM_SERVICE.
     */
//STM profile
    public static final UUID STM_SERVICE = UUID.fromString("6e400009-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * The constant STM_WRITE_CHARACTERISTIC.
     */
    public static final UUID STM_WRITE_CHARACTERISTIC = UUID.fromString("6e400010-b5a3-f393-e0a9-e50e24dcca9e");
    /**
     * The constant STM_READ_CHARACTERISTIC.
     */
    public static final UUID STM_READ_CHARACTERISTIC = UUID.fromString("6e400011-b5a3-f393-e0a9-e50e24dcca9e");
}
