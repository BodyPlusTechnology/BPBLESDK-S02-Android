package cc.bodyplus.sdk.ble.parse;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

import cc.bodyplus.sdk.ble.utils.DataUtils;
import cc.bodyplus.sdk.ble.utils.TLUtil;

/**
 * Created by Shihoo.Wang 2019/3/22
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class EcgDataParser {
    private static int mPayloadLength = -1; // 接收生理数据时指示还有多少字节等待传输, 每次收到数据会减小, 为0时指示传输完成
    private static int mCurReadLength = 0; // 当前接收到数据在 mBodyData 的下标
    private static byte[] mBodyData; // 存储生理数据帧, 会由多次数据接收拼接组成

    public static void handleEcgDataAvailable(byte[] data, EcgDataParserListener listener) {
        //头部出现在加载生理数据的时候则重置数据
        if (BPDataParser.isNonBodyDataFrameLength(data) && data.length < mPayloadLength) {
            resetData();
        }
        if (mPayloadLength > 0) {
            int len = data.length;
            try {
                System.arraycopy(data, 0, mBodyData, mCurReadLength, len);
                mCurReadLength += len;
                mPayloadLength -= len;
            } catch (Exception e) {
                resetData();
                return;
            }
            if (mPayloadLength == 0) {
                boolean crcPass = TLUtil.validateCRC8(mBodyData);
                if (crcPass) {
                    onDataAvailable(mBodyData,listener);
                } else {
                    // 校验错误
                }
                resetData();
            } else if (mPayloadLength < 0) {
                resetData();
            }
        } else {
            boolean crcPass = TLUtil.validateCRC8(data);
            if (crcPass) {
                if (mBodyData != null) {
                    mBodyData = null;
                }
                mPayloadLength = DataUtils.parseBodyDataHeader(data);
                if (mPayloadLength < 0){
                    listener.parseDataFrameHeader(data);
                }
                if (mPayloadLength > 0) {
                    mBodyData = new byte[mPayloadLength];
                    mCurReadLength = 0;
                }
            } else {
                // 校验错误
            }
        }
    }


    private static void resetData() {
        mPayloadLength = -1;
        mCurReadLength = 0;
        mBodyData = null;
    }

    private static void onDataAvailable(byte[] data, EcgDataParserListener listener) {
        HashMap<Byte, BPDataMeta> metaList = parseBodyData(data);
        if (metaList == null){
            return;
        }
        for (Object o : metaList.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object key = entry.getKey();
            BPDataMeta val = (BPDataMeta) entry.getValue();
            switch ((byte) key) {
                case BPDataParser.CODE_HEART_RATE:
                    listener.onHeartValue(val.data[0] & 0xff);
                    break;
                case BPDataParser.CODE_BREATHING_RATE:
                    listener.onBreathValue(val.data[0] & 0xff);
                    break;
                case BPDataParser.CODE_HR_ERROR:
                    listener.onHeartError(val.data[0] & 0xff);
                    break;
                case BPDataParser.CODE_BR_ERROR:
                    listener.onBreathError(val.data[0] & 0xff);
                    break;
            }
        }
    }


    private static HashMap<Byte, BPDataMeta> parseBodyData(byte[] data) {  //0x02, 0xB6, 0x06, 0x2D, 0x1E
        if (data[0]==(byte)(0xF2) || data[0]==(byte)(0xF5)){
            int a = 1+1;
        }
        @SuppressLint("UseSparseArrays")
        HashMap<Byte, BPDataMeta> retList = new HashMap<>();
        int len, i = 0;
        BPDataMeta dm;
        // 需要减去最后一位的CRC8
        while (i < data.length - 1) {   //21
            byte code = data[i];   //07   02
            len = BPDataParser.getDataLengthByCode(code);  // 1  15
            if (len<1){
                return null;
            }
            dm = new BPDataMeta(data, i, len);   // data 0 15
            retList.put(code,dm);
            i += dm.getDataLength();
        }
        return retList;
    }

}
