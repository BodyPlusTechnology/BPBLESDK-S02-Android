package cc.bodyplus.sdk.ble.parse;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

import cc.bodyplus.sdk.ble.utils.TLUtil;

/**
 * Created by Shihoo.Wang 2019/3/22
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class OfflineDataParser {


    public static final int OFFLINE_DATA_ERROR_TIMEOUT = 1;          // S02离线数据传输超时
    public static final int OFFLINE_DATA_ERROR_CRC_ERROR = 2;          // S02离线数据传输CRC校验失败
    public static final int OFFLINE_DATA_ERROR_OTHER = 3;          // S02离线数据失败 传输出错
    public static final int OFFLINE_DATA_ERROR_PARSER = 4;          // S02离线数据失败 解析出错
    public static final int OFFLINE_DATA_ERROR_NO_DATA = 5;                    // 无S02离线数据
    public static final int HEART_RATE = BPDataParser.CODE_HEART_RATE; // 心率
    public static final int BREATHING_RATE = BPDataParser.CODE_BREATHING_RATE; // 呼吸
    public static final int HEART_ERROR = BPDataParser.CODE_HR_ERROR; // 心率数据异常
    public static final int BREATH_ERROR = BPDataParser.CODE_BR_ERROR; // 呼吸数据异常
    public static final int DATA_LENGTH = 4; // 数据长度1

    private static int DIV_STAMP = 1504228278; // 2017/9/1 9:11:18 时间戳


    private static boolean isOfflineDataFinish;
    private static OfflineTimeOutThread mOfflineTimeOutThread;
    private static int mOfflineTimeOutNum;

    private static int mOfflineFrameDataLength = -1;
    private static int mOfflineCurLength = 0;
    private static byte[] mOfflineFrameData;
    private static byte mOfflineFrameCrc;
    private static byte[] mOfflineTotalData;
    private static int mOfflineTimeStamp;
    private static int mOfflineTotalLength;

    public static void initOffline(int stamp, int length){
        isOfflineDataFinish = false;
        mOfflineTotalLength = length;
        mOfflineTotalData = new byte[length];
        mOfflineFrameDataLength = -1;
        mOfflineCurLength = 0;
        mOfflineFrameData = null;
        mOfflineFrameCrc = 0;
        mOfflineTimeStamp = stamp;
    }


    public static void handleOfflineData(byte[] data, OfflineDataParserListener listener) {
        checkOfflineTimeOut(listener);
        if (mOfflineFrameDataLength > 0){
            int len = data.length;
            try {
                System.arraycopy(data, 0, mOfflineFrameData, mOfflineCurLength, len);
                mOfflineCurLength += len;
                mOfflineFrameDataLength -= len;
            } catch (ArrayIndexOutOfBoundsException e) {
                mOfflineFrameDataLength = -1;
                mOfflineCurLength = 0;
                mOfflineFrameData = null;
                onError(listener,OFFLINE_DATA_ERROR_OTHER);
                return;
            } catch (Exception e) {
                mOfflineFrameDataLength = -1;
                mOfflineCurLength = 0;
                mOfflineFrameData = null;
                onError(listener,OFFLINE_DATA_ERROR_OTHER);
                return;
            }
            if (mOfflineFrameDataLength == 0) {
                byte crc8 = TLUtil.CRC8_Tab(mOfflineFrameData);
                if (crc8 == mOfflineFrameCrc) {
                    try {
                        System.arraycopy(mOfflineFrameData,0,mOfflineTotalData,(mOfflineTotalData.length-mOfflineTotalLength), mOfflineFrameData.length);
                        mOfflineTotalLength -= mOfflineFrameData.length;
                        if (mOfflineTotalLength > 0){
                            int process = ((mOfflineTotalData.length-mOfflineTotalLength))*100/(mOfflineTotalData.length);
                            // 同时应答 Core接收到应答后才会发送下一帧数据
                            listener.offlineDataUploadFrameResponse();
                            listener.offlineDateUploadProcess(process);
                        }else {
                            // 所有数据接收完成
                            offlineDataAvailable(mOfflineTimeStamp,mOfflineTotalData,listener);
                            // 此处通知硬件接受完数据 有风险 应该等数据解析完成再通知
                            initOffline(0,0);
                            isOfflineDataFinish = true;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        onError(listener,OFFLINE_DATA_ERROR_OTHER);
                    }
                } else {
                    // CRC校验出错
                    onError(listener,OFFLINE_DATA_ERROR_CRC_ERROR);
                }
                mOfflineFrameDataLength = -1;
                mOfflineCurLength = 0;
                mOfflineFrameData = null;
            } else if (mOfflineFrameDataLength < 0) {
                mOfflineFrameDataLength = -1;
                mOfflineCurLength = 0;
                mOfflineFrameData = null;
            }
        }else{
            boolean crcPass = TLUtil.validateCRC8(data);
            if (crcPass) {
                if (mOfflineFrameData != null) {
                    mOfflineFrameData = null;
                }
                parseOfflineDataHeader(data);
                if (mOfflineFrameDataLength > 0) {
                    mOfflineFrameData = new byte[mOfflineFrameDataLength];
                    mOfflineCurLength = 0;
                }else {
                    onError(listener,OFFLINE_DATA_ERROR_OTHER);
                }
            }else {
                onError(listener,OFFLINE_DATA_ERROR_CRC_ERROR);
            }
        }
    }

    private static void parseOfflineDataHeader(byte[] data){
        if (!BPDataParser.isNonBodyDataFrameLength(data) && !BPDataParser.isStateFrameFrameLength(data)) {
            mOfflineFrameDataLength = -1;
        }
        if (isOfflineDataHeader(data)) { // 离线数据的处理
            mOfflineFrameCrc = data[4];
            mOfflineFrameDataLength = (data[2]&0xFF) | ((data[3]&0xFF) << 8);
        }
    }

    private static boolean isOfflineDataHeader(byte[] data) {
        return ((byte) 0xDE == data[0]) && ((byte) 0xDE == data[1]);
    }


    private static void checkOfflineTimeOut(OfflineDataParserListener listener){
        mOfflineTimeOutNum = 3;
        if (mOfflineTimeOutThread == null){
            mOfflineTimeOutThread = new OfflineTimeOutThread(listener);
            mOfflineTimeOutThread.start();
        }
    }

    private static void offlineDataAvailable(final int time, final byte[] data, OfflineDataParserListener listener){
        try {
            int timeStamp = time;
            if (time < DIV_STAMP){
                timeStamp = (int) (System.currentTimeMillis()/1000);
            }
            int dataLength = data.length;
            ArrayList<OfflineBean> HrData = new ArrayList<>();
            ArrayList<OfflineBean> BrData = new ArrayList<>();
            ArrayList<HrBrErrBean> HrBrErrData = new ArrayList<>();
            for (int i=0;i<dataLength;i=i+DATA_LENGTH){
                if (data[i] == HEART_RATE){ // 心率
                    int stamp = (timeStamp + ((data[i+1] & 0xFF) | ((data[i+2] & 0xFF) << 8)));
                    OfflineBean bean = new OfflineBean((data[i] & 0xFF),0,(data[i+3] & 0xFF),stamp);
                    HrData.add(bean);
                }else if (data[i] == BREATHING_RATE){ // 呼吸
                    int stamp = (timeStamp + ((data[i+1] & 0xFF) | ((data[i+2] & 0xFF) << 8)));
                    OfflineBean bean = new OfflineBean((data[i] & 0xFF),0,(data[i+3] & 0xFF),stamp);
                    BrData.add(bean);
                }else if (data[i] == HEART_ERROR){ // 心率异常
                    int relativeTime = (data[i+1] & 0xFF) | ((data[i+2] & 0xFF) << 8);
                    HrBrErrBean errBean = new HrBrErrBean(relativeTime,data[i+3],data[i]);
                    HrBrErrData.add(errBean);
                }else if (data[i] == BREATH_ERROR){ // 呼吸异常
                    int relativeTime = (data[i+1] & 0xFF) | ((data[i+2] & 0xFF) << 8);
                    HrBrErrBean errBean = new HrBrErrBean(relativeTime,data[i+3],data[i]);
                    HrBrErrData.add(errBean);
                }
            }
            if (HrData.isEmpty()){
                onError(listener,OFFLINE_DATA_ERROR_PARSER);
            }else {
                int endTimeStamp = HrData.get(HrData.size()-1).timeStamp;
                List<Integer> heart = generateOfflineData(timeStamp,endTimeStamp,HrData);
                List<Integer> breath = generateOfflineData(timeStamp,endTimeStamp,BrData);
                listener.offlineDataUploadCompleted(timeStamp,heart,breath,HrBrErrData);
            }

        }catch (Exception e){
            onError(listener,OFFLINE_DATA_ERROR_PARSER);
            e.printStackTrace();
        }
    }


    private static List<Integer> generateOfflineData(int startTimeStamp, int endTimeStamp, ArrayList<OfflineBean> offlineData){
        ArrayList<Integer> newData = new ArrayList<>();
        List<Integer> resultData = new ArrayList<>();
        for (int i=0;i<offlineData.size();i++){
            if (i == 0){
                // 从0开始判断有多少丢失的
                int num = offlineData.get(i).timeStamp - startTimeStamp;
                int value = offlineData.get(i).data;
                for(int j=0;j<=num;j++){
                    newData.add(value);
                }
            }else if ( i == offlineData.size()-1){
                // 判断最后有多少丢失的
                int num = endTimeStamp - offlineData.get(i).timeStamp;
                int value = offlineData.get(i).data;
                for(int j=0;j<=num;j++){
                    newData.add(value);
                }
                // 判断总数
                int length = endTimeStamp - startTimeStamp;
                int size = newData.size();
                if (size > length){
                    resultData = newData.subList(0,length);
                }else{
                    resultData.addAll(newData);
                    for(int k=0;k<(length-size);k++){
                        resultData.add(value);
                    }
                }
            }else {
                int num = offlineData.get(i).timeStamp - offlineData.get(i-1).timeStamp;
                int value = offlineData.get(i).data;
                for(int j=0;j<num;j++){
                    newData.add(value);
                }
            }
        }
        return resultData;
    }

    static class OfflineTimeOutThread extends Thread{
        private OfflineDataParserListener mListener;
        OfflineTimeOutThread(OfflineDataParserListener listener){
            mListener = listener;
        }

        @Override
        public void run() {
            while (mOfflineTimeOutNum > 0){
                SystemClock.sleep(1000);
                mOfflineTimeOutNum -- ;
            }
            if (!isOfflineDataFinish){
                onError(mListener,OFFLINE_DATA_ERROR_TIMEOUT);
            }
        }
    }

    private static void onError(OfflineDataParserListener listener, int errorType){
        initOffline(0,0);
        if (listener != null) {
            listener.OfflineDataUploadError(errorType);
        }
    }

}
