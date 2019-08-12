package cc.bodyplus.sdk.ble.parse;

import cc.bodyplus.sdk.ble.wave.EcgWaveFrameData;

/**
 * Created by Shihoo.Wang 2019/3/23
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public interface WaveDataParserListener {

    void waveData(EcgWaveFrameData mWaveData);

    void hrvResult(int m2_hrv_process);
}
