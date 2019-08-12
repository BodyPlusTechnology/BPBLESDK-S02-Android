package cc.bodyplus;

/**
 * Created by Shihoo.Wang 2019/3/25
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class BPEcgWaveTools {

    static {
        System.loadLibrary("BPEcgWave");
    }

    public static native int m2_hrv_process(short[] data, int dataLength);
}
