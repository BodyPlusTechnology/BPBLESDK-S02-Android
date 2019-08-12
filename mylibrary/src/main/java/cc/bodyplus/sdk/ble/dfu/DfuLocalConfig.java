package cc.bodyplus.sdk.ble.dfu;

import java.util.HashMap;

import cc.bodyplus.sdk.ble.utils.DeviceInfo;

/**
 * Created by Shihoo.Wang 2019/3/21
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class DfuLocalConfig {

    private static HashMap<Integer,Integer> SW_NEW_VERSION = new HashMap<>();
    private static HashMap<Integer,String> SW_NEW_FILE_NAME = new HashMap<>();

    static {
        SW_NEW_VERSION.put(2,   295);   // 0.02硬件版本号    最新固件版本号 1.27
        SW_NEW_VERSION.put(4,   295);   // 0.04硬件版本号    最新固件版本号 1.27
        SW_NEW_VERSION.put(6,   295);   // 0.06硬件版本号    最新固件版本号 1.27
        SW_NEW_VERSION.put(256, 295);   // 1.00硬件版本号    最新固件版本号 1.27
        SW_NEW_VERSION.put(257, 295);   // 1.01硬件版本号    最新固件版本号 1.27

        SW_NEW_FILE_NAME.put(2,     "bpdfu/s02_0002_0127.zip");     //最新固件地址
        SW_NEW_FILE_NAME.put(4,     "bpdfu/s02_0004_0127.zip");     //最新固件地址
        SW_NEW_FILE_NAME.put(6,     "bpdfu/s02_0006_0127.zip");     //最新固件地址
        SW_NEW_FILE_NAME.put(256,   "bpdfu/s02_0100_0127.zip");     //最新固件地址
        SW_NEW_FILE_NAME.put(257,   "bpdfu/s02_0101_0127.zip");     //最新固件地址
    }


    public static int getNewDfuVersionByHw(int hw){
        Integer integer = SW_NEW_VERSION.get(hw);
        if (integer==null){
            return 0;
        }else {
            return integer;
        }
    }

    public static String getNewDfuFileNameByHw(DeviceInfo device) {
        int hw = Integer.parseInt(device.hwVn);
        int oldSw = Integer.parseInt(device.swVn);
        int newSw = getNewDfuVersionByHw(hw);
        if (newSw > oldSw) {
            String s = SW_NEW_FILE_NAME.get(hw);
            if (s == null) {
                return "";
            } else {
                return s;
            }
        }else {
            return "";
        }
    }

}
