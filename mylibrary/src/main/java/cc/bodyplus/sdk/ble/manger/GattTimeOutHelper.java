package cc.bodyplus.sdk.ble.manger;

import android.os.SystemClock;

import java.util.Arrays;

import cc.bodyplus.sdk.ble.utils.BleWriteData;

/**
 * Created by Shihoo.Wang 2019/3/22
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class GattTimeOutHelper {

    private boolean timeOutThread_Start = false;
    private boolean sIsWriting = false;
    private int timeOutNum = 0;
    private int timeOutTime = 0;
    private BleWriteData reSendData ;
    private BleWriteData latSendData;
    private TimeOutThread timeOutThread;
    private GattTimeOutListener timeOutListener;

    GattTimeOutHelper(GattTimeOutListener listener){
        timeOutListener = listener;
    }

    void init(){
        timeOutThread_Start = true;
        if (timeOutThread == null){
            timeOutThread = new TimeOutThread();
            timeOutThread.start();
        }
    }

    boolean getIsWriting() {
        return sIsWriting;
    }

    void setIsWriting(boolean isWriting){
        sIsWriting = isWriting;
        if (!isWriting) {
            timeOutTime = 0;
        }
    }

    BleWriteData getLatSendData() {
        return latSendData;
    }

    void setLatSendData(BleWriteData bDatas) {
        latSendData = bDatas;
    }

    void closeThread() {
        setIsWriting(false);
        timeOutThread_Start = false;
    }


    class TimeOutThread extends Thread{
        @Override
        public void run() {
            while(timeOutThread_Start){
                if (sIsWriting){
                    if (timeOutTime == 6){
                        // 超时三秒啦 重发
                        if (timeOutNum == 3){
                            // 超时3次 TODO 放弃当前这条指令 发送下一条
                            sIsWriting = false;
                            timeOutNum = 0;
                            timeOutTime = 0;
                            if (timeOutListener != null){
                                timeOutListener.timeout();
                            }
                            continue;
                        }
                        if (reSendData !=null &&
                                latSendData !=null &&
                                reSendData.write_type == latSendData.write_type &&
                                Arrays.toString(reSendData.write_data).equals(Arrays.toString(latSendData.write_data))){
                            timeOutNum++ ;
                        }else {
                            timeOutNum = 1;
                        }
                        if (latSendData != null){
                            if (timeOutListener != null){
                                timeOutListener.reSendData(latSendData);
                            }
                            reSendData = latSendData;
                        }
                        timeOutTime = 0;
                    }else {
                        int num = 0;
                        while (sIsWriting){
                            SystemClock.sleep(10);
                            num ++;
                            if (num == 50){
                                break;
                            }
                        }
                        timeOutTime++;
                    }
                }else {
                    int num = 0;
                    while (!sIsWriting){
                        SystemClock.sleep(10);
                        num ++;
                        if (num == 1000){
                            break;
                        }
                    }
                }
            }
        }
    }


    interface GattTimeOutListener{
        void reSendData(BleWriteData latSendData);
        void timeout();
    }

}
