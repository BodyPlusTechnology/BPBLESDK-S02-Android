package cc.bodyplus.sdk.ble.parse;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

import cc.bodyplus.BPEcgWaveTools;
import cc.bodyplus.sdk.ble.utils.BleLogUtils;
import cc.bodyplus.sdk.ble.utils.TLUtil;
import cc.bodyplus.sdk.ble.wave.EcgWaveFrameData;
import cc.bodyplus.sdk.ble.wave.EcgWaveOriginalDataUtils;

/**
 * Created by Shihoo.Wang 2019/3/22
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class WaveDataParser {
    public static final int FRAME_DATA_LENGTH = 500;

    private static int mWaveDataLength = -1;
    private static int mWaveCurLength = 0;
    private static byte[] mWaveData;
    private static int mWaveCount;
    private static int unixtime;
    private static int loff;
    private static List<EcgWaveFrameData> tempWaveFrameData = new ArrayList<>();
    private static WaveDataParserListener listener;
    public static void init(WaveDataParserListener waveDataParserListener){
        mWaveDataLength = -1;
        mWaveCurLength = 0;
        mWaveData = null;
        mWaveCount = 0;
        unixtime = 0;
        loff = 0;
        tempWaveFrameData.clear();
        listener = waveDataParserListener;
        EcgWaveOriginalDataUtils.getInstance().reset();
    }

    public static void calcHrv(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                calcAndSaveWaveData();
            }
        }).start();
    }


    /**
     * 解析心率波形数据
     */
    public static void handleWaveDataAvailable(byte[] data){

        if (mWaveDataLength > 0){
            int len = data.length;
            try {
                System.arraycopy(data, 0, mWaveData, mWaveCurLength, len);
                mWaveCurLength += len;
                mWaveDataLength -= len;
            } catch (ArrayIndexOutOfBoundsException e) {
                mWaveDataLength = -1;
                mWaveCurLength = 0;
                mWaveData = null;
                return;
            } catch (Exception e) {
                mWaveDataLength = -1;
                mWaveCurLength = 0;
                mWaveData = null;
                return;
            }
            if (mWaveDataLength == 0) {
                EcgWaveFrameData frameData = new EcgWaveFrameData(loff,unixtime,mWaveCount,mWaveData);
//                if (listener != null) {
//                    listener.waveData(frameData);
//                }
                EcgWaveOriginalDataUtils.getInstance().addWave(frameData);
                tempWaveFrameData.add(frameData);
                mWaveDataLength = -1;
                mWaveCurLength = 0;
                mWaveData = null;
            } else if (mWaveDataLength < 0) {
                mWaveDataLength = -1;
                mWaveCurLength = 0;
                mWaveData = null;
            }
        }else{
            if (!BPDataParser.isEcgWaveFrame(data)) {
                mWaveDataLength = -1;
            }else {
                boolean crcPass = TLUtil.validateCRC8(data);
                if (crcPass) {
                    if (mWaveData != null) {
                        mWaveData = null;
                    }
                    parseWaveDataHeader(data);
                    if (mWaveDataLength > 0) {
                        mWaveData = new byte[mWaveDataLength];
                        mWaveCurLength = 0;
                    }
                }
            }
        }
    }

    private static void parseWaveDataHeader(byte[] data){
        loff = data[3] & 0xFF;
        unixtime = (data[4] & 0xFF) | ((data[5] & 0xFF) << 8) | ((data[6] & 0xFF) << 16) | ((data[7] & 0xFF) << 24);
        mWaveCount = (data[8] & 0xFF) | ((data[9] & 0xFF) << 8);
        mWaveDataLength = (data[10] & 0xFF) | ((data[11] & 0xFF) << 8); // 默认长度为 FRAME_DATA_LENGTH = 500
        if (mWaveDataLength != FRAME_DATA_LENGTH){
            mWaveDataLength = -1;
        }
//        LogUtil.wshLog().d("收到 心电波形数据 ---------- mWaveCount ： "+ mWaveCount);
    }

    private static void calcAndSaveWaveData(){
        if (!tempWaveFrameData.isEmpty()){
            short[] src = new short[tempWaveFrameData.size() * FRAME_DATA_LENGTH/2];
            int currentIndex = 0;
            for (EcgWaveFrameData frameData : tempWaveFrameData ) {
                System.arraycopy(frameData.ecgData,0,src,currentIndex,FRAME_DATA_LENGTH/2);
                currentIndex += FRAME_DATA_LENGTH/2;
            }
            int result = BPEcgWaveTools.m2_hrv_process(src,src.length);
            if (listener != null) {
                listener.hrvResult(result);
            }
            BleLogUtils.handleLogData(System.currentTimeMillis()/1000,tempWaveFrameData,String.valueOf(result));
//            LogUtil.wshLog().d("数据总长度 ： "+ src.length + " -- HRV分析 ： "+ result);
            init(null);
        }
    }
}
