package cc.bodyplus.sdk.ble.parse;

import java.util.List;

/**
 * Created by Shihoo.Wang 2019/6/6
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class OfflineResultData {

    public OfflineResultData(int timeStamp, List<Integer> hrData, List<Integer> brData, List<HrBrErrBean> hrBrErrData) {
        this.timeStamp = timeStamp;
        this.hrData = hrData;
        this.brData = brData;
        this.hrBrErrData = hrBrErrData;
    }

    public int timeStamp;
    public List<Integer> hrData;
    public List<Integer> brData;
    public List<HrBrErrBean> hrBrErrData;
}
