package cc.bodyplus.sdk.ble.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shihu.wang on 2017/3/17.
 * Email shihu.wang@bodyplus.cc
 */
public class BleCmdConfig {
    /**
     * BLE命令参数
     */
    //时间设置
    public static final short BLE_CMD_TIME = (short) 4;
    /**
     * The constant BLE_CMD_UPLOAD_DATA.
     */
    //数据上传总开关
    public static final short BLE_CMD_UPLOAD_DATA = (short) 12;
    /**
     * The constant BLE_CMD_UPLOAD_DATA_EMG.
     */
    //数据上传总开关
    public static final short BLE_CMD_UPLOAD_DATA_EMG = (short) 13;
    /**
     * The constant BLE_CMD_UPLOAD_DATA_ECG.
     */
    //ECG数据上传总开关
    public static final short BLE_CMD_UPLOAD_DATA_ECG = (short) 14;

    // 历史数据,离线存储的数据
    public static final short BLE_CMD_UPLOAD_OLDDATA = (short) 19;

    // 用户自定义名称 长度12 默认值BodyPlus  蓝牙自定义名称 取代S01版本 NAM
    public static final short BLE_CMD_USER_NAME = (short) 20;

    /**
     * The constant BLE_CMD_DFU_JUMP_LENGTH.
     */
    // 升级跳转  1 跳转到升级模式 取代DFU
    public static final short BLE_CMD_DFU_JUMP_LENGTH = (short) 33;

    /**
     * The constant BLE_CMD_USER_ID.
     */
    //用户标示码
    public static final short BLE_CMD_USER_ID = (short) 40;
    /**
     * The constant BLE_CMD_PASSWORD_CHECK.
     */
    //校验连接验证码
    public static final short BLE_CMD_PASSWORD_CHECK = (short) 48;
    /**
     * The constant BLE_CMD_POW_LEV.
     */
    //电池电量
    public static final short BLE_CMD_POW_LEV = (short) 57;
    /**
     * The constant BLE_CMD_HW_VN.
     */
    //硬件版本号
    public static final short BLE_CMD_HW_VN = (short) 64;
    /**
     * The constant BLE_CMD_SW_VN.
     */
    //固件版本号
    public static final short BLE_CMD_SW_VN = (short) 66;
    /**
     * The constant BLE_CMD_CORE_SN.
     */
    //SN码
    public static final short BLE_CMD_CORE_SN = (short) 68;
    /**
     * The constant BLE_CMD_CORE_MODE.
     */
    //模块位置状态
    public static final short BLE_CMD_CORE_MODE = (short) 60;

    //BootLoad版本
    public static final short BLE_CMD_DFU_VN = (short) 46;

    //APP写的BootLoad版本
    public static final short BLE_CMD_DFU_VN_APP = (short) 78;

    //core类型
    public static final short BLE_CMD_CORE_TYPE = (short) 45;

    /**
     * 心率数据
     */
    public static final int BLE_HEART_MESSAGE = 3;

    /**
     * 呼吸数据
     */
    public static final int BLE_BREATHING_MESSAGE = 4;

    /**
     * 心率数据异常
     */
    public static final int BLE_HEART_ERROR_MESSAGE = 5;

    /**
     * 呼吸数据异常
     */
    public static final int BLE_BREATHING_ERROR_MESSAGE = 6;

    /**
     * BLE升级参数
     */
//stm32APP跳转到bootload
    public static final short DFU_APP2BOOT = 1;
    /**
     * The constant DFU_INIT.
     */
//初始化升级状态
    public static final short DFU_INIT = 2;
    /**
     * The constant DFU_SET.
     */
    //设置长度，CRC，擦除Flash
    public static final short DFU_SET = 3;
    /**
     * The constant DFU_STATE.
     */
//查询DFU状态
    public static final short DFU_STATE = 5;


    private static Map<Short, Byte> mCommandPayloadLengthMap = new HashMap<>();

    static {
        mCommandPayloadLengthMap.put(BLE_CMD_TIME, (byte) 8);
        mCommandPayloadLengthMap.put(BLE_CMD_UPLOAD_DATA, (byte) 7);
        mCommandPayloadLengthMap.put(BLE_CMD_UPLOAD_DATA_ECG, (byte) 7);
        mCommandPayloadLengthMap.put(BLE_CMD_PASSWORD_CHECK, (byte) 8);
        mCommandPayloadLengthMap.put(BLE_CMD_POW_LEV, (byte) 1);
        mCommandPayloadLengthMap.put(BLE_CMD_HW_VN, (byte) 2);
        mCommandPayloadLengthMap.put(BLE_CMD_SW_VN, (byte) 2);
        mCommandPayloadLengthMap.put(BLE_CMD_CORE_SN, (byte) 8);
        mCommandPayloadLengthMap.put(DFU_APP2BOOT, (byte) 1);
        mCommandPayloadLengthMap.put(DFU_INIT, (byte) 1);
        mCommandPayloadLengthMap.put(DFU_SET, (byte) 8);
        mCommandPayloadLengthMap.put(DFU_STATE, (byte) 1);
        mCommandPayloadLengthMap.put(BLE_CMD_USER_ID, (byte) 4);
        mCommandPayloadLengthMap.put(BLE_CMD_CORE_MODE, (byte) 1);
        mCommandPayloadLengthMap.put(BLE_CMD_DFU_VN, (byte) 1);
        mCommandPayloadLengthMap.put(BLE_CMD_DFU_VN_APP, (byte) 1);
        mCommandPayloadLengthMap.put(BLE_CMD_CORE_TYPE, (byte) 1);

        mCommandPayloadLengthMap.put(BLE_CMD_USER_NAME, (byte) 12);
        mCommandPayloadLengthMap.put(BLE_CMD_DFU_JUMP_LENGTH, (byte) 1);
    }

    /**
     * Gets payload length by command.
     *
     * @param cmd the cmd
     * @return the payload length by command
     */
    public static byte getPayloadLengthByCommand(short cmd) {
        return mCommandPayloadLengthMap.get(cmd);
    }



    public class HistoryDataCmdVal{
        public final static short DATA_ASK = 0x01; //读命令，查询是否有数据，将返回有几段数据，1byte
        public final static short DATA_UPL = 0x02; //写命令，开始数据上传
        public final static short DATA_END = 0x03;  //写命令，完成数据接收
        public final static short DATA_ERS = 0x04;  //写命令，擦除所有历史数据
        public final static short DATA_INF = 0x08;  //读命令，查询当前要读的数据信息，返回时间和长度，8byte
        public final static short WORK_STATUS = 0x01;  //读命令，读取数据采集状态 0未采集，1数据上传 2 离线模式 1byte
    }

    public class HistoryDataLength{
        public final static int DATA_ASK_DATA_LENGHT = 6;
        public final static int DATA_INF_DATA_LENGHT = 13;
    }
}
