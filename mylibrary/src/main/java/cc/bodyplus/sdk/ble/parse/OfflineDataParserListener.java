package cc.bodyplus.sdk.ble.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shihoo.Wang 2019/3/22
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public interface OfflineDataParserListener {

    void offlineDateUploadProcess(int process);

    void OfflineDataUploadError(int offlineDataErrorTimeout);

    void offlineDataUploadFrameResponse();

    void offlineDataUploadCompleted(int timeStamp, List<Integer> hrData, List<Integer> brData, ArrayList<HrBrErrBean> hrBrErrData);

}
