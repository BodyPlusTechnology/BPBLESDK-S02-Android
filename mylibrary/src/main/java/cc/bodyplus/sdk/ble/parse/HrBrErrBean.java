package cc.bodyplus.sdk.ble.parse;

import java.io.Serializable;

/**
 * Created by Shihoo.Wang 2019/7/13
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 *
 * 心率呼吸异常数据的记录
 */
public class HrBrErrBean implements Serializable {
    public HrBrErrBean(int relativeTime, byte value, byte item) {
        this.relativeTime = relativeTime;
        this.value = value;
        this.item = item;
    }

    public int relativeTime;
    public byte value;
    public byte item;
}
