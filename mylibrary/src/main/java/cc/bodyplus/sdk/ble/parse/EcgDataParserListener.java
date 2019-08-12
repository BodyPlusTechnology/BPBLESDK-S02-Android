package cc.bodyplus.sdk.ble.parse;

/**
 * Created by Shihoo.Wang 2019/3/22
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public interface EcgDataParserListener {

    void onHeartValue(int value);

    void onBreathValue(int value);

    void onHeartError(int value);

    void onBreathError(int value);

    void parseDataFrameHeader(byte[] data);

}
