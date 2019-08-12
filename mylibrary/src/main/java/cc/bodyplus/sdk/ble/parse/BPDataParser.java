package cc.bodyplus.sdk.ble.parse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shihu.wang on 2017/3/17.
 * Email shihu.wang@bodyplus.cc
 */
public class BPDataParser {
    /**
     * The constant CODE_ECG_MONITOR.
     */
    public static final byte CODE_ECG_MONITOR = 0x01; // 心电监护
    /**
     * The constant CODE_HEART_RATE.
     */
    public static final byte CODE_HEART_RATE = 0x02; // 心率
    /**
     * The constant CODE_RR_INTERVALS.
     */
    public static final byte CODE_RR_INTERVALS = 0x03; // RR间期
    /**
     * The constant CODE_HRV_RATIO.
     */
    public static final byte CODE_HRV_RATIO = 0x04; // HRV比率
    /**
     * The constant CODE_BREATHING_RATE.
     */
    public static final byte CODE_BREATHING_RATE = 0x05; // 呼吸率
    /**
     * The constant CODE_BREATHE_STRENGTH.
     */
    public static final byte CODE_BREATHE_STRENGTH = 6; // 呼吸强度
    /**
     * The constant CODE_HW_STATE.
     */
    public static final byte CODE_HW_STATE = (byte) 0xEE; // 状态, 硬件故障时

    public static final byte CODE_HR_ERROR = (byte) 0xF2; // 状态, 心率故障时

    public static final byte CODE_BR_ERROR = (byte) 0xF5; // 状态, 呼吸率故障时


	// <数据项代码, 数据长度>
	private static Map<Byte, Integer> mDataItemMap = new HashMap<Byte, Integer>();

	static {
		mDataItemMap.put(CODE_ECG_MONITOR, 50);
		mDataItemMap.put(CODE_HEART_RATE, 1);
		mDataItemMap.put(CODE_RR_INTERVALS, 1);
		mDataItemMap.put(CODE_HRV_RATIO, 1);
		mDataItemMap.put(CODE_BREATHING_RATE, 1);
		mDataItemMap.put(CODE_BREATHE_STRENGTH, 1);
		mDataItemMap.put(CODE_HW_STATE, 1);
		mDataItemMap.put(CODE_HR_ERROR, 1);
		mDataItemMap.put(CODE_BR_ERROR, 1);
	}

    public static int getDataLengthByCode(byte code) {
		int len = -1;
		if (mDataItemMap.containsKey(code)) {
			len = mDataItemMap.get(code);
		}
		return len;
	}



    public static int byteToInt(byte[] data, int index) {
		int ret = 0, t = 0;
		if ((null == data) || (data.length < (4 + index))) {
			return 0;
		}
		for (int i = 0; i < 4; ++i) {
			t = (data[i + index] & 0xFF) << (8 * i);
			ret += t;
		}
		return ret;
	}

    public static short byteToShort(byte[] data, int index) {
		short ret = 0, t = 0;
		if ((null == data) || (data.length < (2 + index))) {
			return 0;
		}
		for (int i = 0; i < 2; ++i) {
			t = (short) ((data[i + index] & 0xFF) << (8 * i));
			ret += t;
		}
		return ret;
	}

    public static boolean isNonBodyDataFrameLength(byte[] data) {
		if ((null != data) && (7 == data.length)) {
			return true;
		}
		return false;
	}

    public static boolean isStateFrameFrameLength(byte[] data) {
		if ((null != data) && (20 == data.length)){
			return true;
		}
		return false;
	}

    public static boolean isTimeStamp(byte[] data) {
		if (isNonBodyDataFrameLength(data)) {
			return ((byte) 0xD0 == data[0]) && ((byte) 0xEF == data[1]);
		}
		return false;
	}

    public static boolean isBodyDataHeader(byte[] data) {
		if (isNonBodyDataFrameLength(data)) {
			return ((byte) 0xD0 == data[0]) && ((byte) 0xDF == data[1]);
		}
		return false;
	}

    public static boolean isStateFrame(byte[] data) {
		if ((null != data) && ((7 == data.length) || (20 == data.length))) {
			return ((byte) 0xD0 == data[0]) && ((byte) 0xEE == data[1]);
		}
		return false;
	}

	public static boolean isHeartBeatFrame(byte[] data) {
		if ((null != data) && ((7 == data.length) || (20 == data.length))) {
			return ((byte) 0xD0 == data[0]) && ((byte) 0xEA == data[1]);
		}
		return false;
	}

	public static boolean isEcgWaveFrame(byte[] data) {
		if ((null != data) && ((13 == data.length))) {
			return ((byte) 0x57 == data[0]) && ((byte) 0x41 == data[1]) && ((byte) 0x56 == data[2]);
		}
		return false;
	}
}
