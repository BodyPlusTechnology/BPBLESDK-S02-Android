package cc.bodyplus.sdk.ble.utils;

/**
 * Created by shihu.wang on 2017/3/24.
 * Email shihu.wang@bodyplus.cc
 */
public class BleWriteData {
    /**
     * The constant CMD.
     */
    public static final int CMD = 1;
    /**
     * The constant STM.
     */
//        public static final int CMD_WRITE = 2;
    public static final int STM = 3;
    /**
     * The constant DESCRIP_WRITE.
     */
//        public static final int STM_WRITE = 4;
    public static final int DESCRIP_WRITE = 5;

    /**
     * The constant BT.
     */
    @Deprecated
    public static final int BT = 7;


    public static final int OFFLINE = 9;

    public static final int DATA_FRAME = 11;

    /**
     * The Write type.
     */
    public int write_type; //对应的特性
    /**
     * The Write data.
     */
    public byte[] write_data; // 设置的数据
    /**
     * The Object.
     */
    public Object object;
}
