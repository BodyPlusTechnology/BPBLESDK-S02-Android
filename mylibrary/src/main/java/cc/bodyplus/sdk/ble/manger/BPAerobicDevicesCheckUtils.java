package cc.bodyplus.sdk.ble.manger;

import android.text.TextUtils;
import android.util.SparseArray;

import cc.bodyplus.sdk.ble.utils.BleUtils;

/**
 * Created by Shihoo.Wang 2019/5/21
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class BPAerobicDevicesCheckUtils {

    /**
     * 适配早风科技项目、S03项目
     * @param deviceSn
     * @return
     */
    public static boolean checkIsS02(String deviceSn){
        if (TextUtils.isEmpty(deviceSn)){
            return false;
        }
        return deviceSn.startsWith("2") | deviceSn.startsWith("3") | deviceSn.startsWith("ZF");
    }

    /**
     *  适配厂商信息
     */
    public static byte[] geSnBytesByScanRecord(byte[] scanRecord){
        if (scanRecord != null) {
            // 获取广播里面的信息
            SparseArray<byte[]> recodeArray = BleUtils.parseFromBytes(scanRecord);
            //根据厂商ID获取对应的信息（0xffff 默认厂商ID  信息为SN码）
            byte[] b = recodeArray.get(0x06f8);
            if (b == null) {
                b = recodeArray.get(0xffff);
            }
            return b;
        }else {
            return null;
        }
    }
}
