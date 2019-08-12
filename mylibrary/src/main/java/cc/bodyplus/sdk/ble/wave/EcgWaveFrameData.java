package cc.bodyplus.sdk.ble.wave;

import java.io.Serializable;

import cc.bodyplus.sdk.ble.utils.BleUtils;

/**
 * Created by Shihoo.Wang 2019/3/26
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class EcgWaveFrameData implements Serializable {

    public EcgWaveFrameData(int loff1,int unixtime1,int aveCnt1,byte[] ecgData1){
        this.loff = loff1;
        this.unixtime = unixtime1;
        this.waveCnt = aveCnt1;
        this.ecgData = BleUtils.byteArrayToShortArray(ecgData1);
    }

    public int loff; // 电极脱落计数，0 - 250，表示信号质量
    public int unixtime; // 标准时间戳
    public int waveCnt; // 波形每秒数据自增计数
    public short[] ecgData; // ECG数据帧，16位精度，500byte, 250采样率， 转换后为 250 Short长度
}
