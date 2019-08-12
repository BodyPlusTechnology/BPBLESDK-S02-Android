package cc.bodyplus.sdk.ble.dfu;

import java.util.HashMap;

import cc.bodyplus.sdk.ble.utils.DeviceInfo;

/**
 * Created by Shihoo.Wang 2019/3/20
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class BootLoaderLocalConfig {

    public static int BOOTLOADER_VERSION_005 = 5; // 最新BootLoader升级包中的 版本号

    private static HashMap<Integer,Integer> SW_NEW_VERSION = new HashMap<>();

    private static HashMap<Integer,String> SW_NEW_FILE_NAME = new HashMap<>();

    //    private static int HW_101 = 257; // 硬件版本号 1.01版本的硬件不需要升级BootLoader

    static {
        SW_NEW_VERSION.put(2,   BOOTLOADER_VERSION_005);
        SW_NEW_VERSION.put(4,   BOOTLOADER_VERSION_005);
        SW_NEW_VERSION.put(6,   BOOTLOADER_VERSION_005);
        SW_NEW_VERSION.put(256, BOOTLOADER_VERSION_005);

        SW_NEW_FILE_NAME.put(2,     "bpdfu/s02_dfu_0002.zip");
        SW_NEW_FILE_NAME.put(4,     "bpdfu/s02_dfu_0004.zip");
        SW_NEW_FILE_NAME.put(6,     "bpdfu/s02_dfu_0006.zip");
        SW_NEW_FILE_NAME.put(256,   "bpdfu/s02_dfu_0100.zip");
    }



    private static int getNewBtVersionByHw(int hw){
        Integer integer = SW_NEW_VERSION.get(hw);
        if (integer==null){
            return 0;
        }else {
            return integer;
        }
    }

    public static String getNewBtFileNameByHw(int hw){
        String s = SW_NEW_FILE_NAME.get(hw);
        if (s==null){
            return "";
        }else {
            return s;
        }
    }


    public static boolean checkBootLoader(DeviceInfo device){
        if (device == null){
            return false;
        }
        if (device.hwVn==null || device.hwVn.length()<1){
            return false;
        }
        // 判断BootLoader版本
        if (device.dfu != null && device.dfu.length()>0) {
            int v = Integer.parseInt(device.dfu);
            // 需要升级
            if (v == 0 || v == 0XFF) {
                // 判断78地址值
                if (device.dfuAPP != null && device.dfuAPP.length()>0){
                    int v78 = Integer.parseInt(device.dfuAPP);
                    if (v78==0 || v==0XFF){ // 判断初始值
                        return true;
                    }else {
                        return v78 < BootLoaderLocalConfig.getNewBtVersionByHw(Integer.parseInt(device.hwVn));
                    }
                }
            } else {
                return v < BootLoaderLocalConfig.getNewBtVersionByHw(Integer.parseInt(device.hwVn));
            }
        }
        return false;
    }

}
