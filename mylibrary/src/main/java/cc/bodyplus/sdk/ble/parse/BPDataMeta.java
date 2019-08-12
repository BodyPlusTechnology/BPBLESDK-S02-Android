package cc.bodyplus.sdk.ble.parse;

/**
 * Created by Shihu.Wang on 2017/7/5.
 * Email shihu.wang@bodyplus.cc
 */

public class BPDataMeta {
    byte item;
    public byte[] data;

    BPDataMeta(byte[] data, int start, int data_length) {
        item = data[start];
        this.data = new byte[data_length];
        System.arraycopy(data, start + 3 , this.data, 0, data_length);
    }

    int getDataLength() {
        return 3 + data.length;
    }

}
