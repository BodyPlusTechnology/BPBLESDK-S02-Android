package cc.bodyplus.sdk.ble.wave;

import android.annotation.SuppressLint;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import cc.bodyplus.sdk.ble.parse.WaveDataParser;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class EcgWaveOriginalDataUtils {
    private Queue<EcgWaveFrameData> dataQueue = null;
    private int lastFrameCount;

    private static EcgWaveOriginalDataUtils mInstance = new EcgWaveOriginalDataUtils();

    private EcgWaveOriginalDataUtils(){

    }

    public static EcgWaveOriginalDataUtils getInstance(){
        return  mInstance;
    }


    @SuppressLint("NewApi")
    public void addWave(EcgWaveFrameData waveFrameData){
        if (dataQueue == null){
            dataQueue = new ConcurrentLinkedDeque<>();
        }
        dataQueue.add(waveFrameData);
//        LogUtil.wshLog().i("添加 --- 帧计数："+ waveFrameData.waveCnt +"  -- 余：" +dataQueue.size()+ " 条");
    }

    public EcgWaveFrameData pollConversionWave(boolean isRe){
        if (lastFrameCount == 65535){
            lastFrameCount = 0;
        }
        if (dataQueue != null){
            if (isRe) {
                lastFrameCount++;
            }
//            LogUtil.wshLog().e("读取 --- 帧计数："+ lastFrameCount +"  -- 余：" +dataQueue.size()+ " 条");
            EcgWaveFrameData peek = dataQueue.peek();
            if (peek != null){
                if (peek.waveCnt == lastFrameCount){
                    return dataQueue.poll();
                }else if (peek.waveCnt < lastFrameCount){
                    dataQueue.remove();
                    return pollConversionWave(false);
                }else /*if (peek.waveCnt > lastFrameCount)*/{
                    lastFrameCount = peek.waveCnt;
                    return dataQueue.poll();
                }
            }
        }
        return new EcgWaveFrameData(0,0,0,new byte[WaveDataParser.FRAME_DATA_LENGTH]);
    }

    public void reset(){
        if (dataQueue!=null) {
            dataQueue.clear();
        }
        dataQueue = null;
        lastFrameCount=0;
    }
}
