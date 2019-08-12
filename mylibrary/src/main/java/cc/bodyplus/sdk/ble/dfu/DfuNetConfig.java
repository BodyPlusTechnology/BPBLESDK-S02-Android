package cc.bodyplus.sdk.ble.dfu;

import java.util.List;

import cc.bodyplus.sdk.ble.utils.DeviceInfo;

/**
 * Created by Shihoo.Wang 2019/3/21
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class DfuNetConfig {

    private static List<DfuUpdateInfo> updateInfoList;


    public static void setUpdateInfoList(List<DfuUpdateInfo> updateInfoList) {
        DfuNetConfig.updateInfoList = updateInfoList;
    }

    public static DfuUpdateInfo getDfuUpdateInfo(DeviceInfo device) {
        int hw = Integer.parseInt(device.hwVn);
        try {
            if (updateInfoList != null && !updateInfoList.isEmpty()) {
                for (DfuUpdateInfo updateInfo : updateInfoList) {
                    if (Integer.parseInt(updateInfo.hwVn) == hw){
                        return updateInfo;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
