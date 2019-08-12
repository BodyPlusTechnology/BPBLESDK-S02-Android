package cc.bodyplus.sdk.ble.parse;

import java.io.Serializable;

/**
 * Created by Shihoo.Wang 2019/6/3
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class OfflineBean implements Serializable {
    public int item;
    public int time;
    public int data;
    public int timeStamp;

    public OfflineBean(int item, int time, int data, int tiemStamp) {
        this.item = item;
        this.time = time;
        this.data = data;
        this.timeStamp = tiemStamp;
    }
}
