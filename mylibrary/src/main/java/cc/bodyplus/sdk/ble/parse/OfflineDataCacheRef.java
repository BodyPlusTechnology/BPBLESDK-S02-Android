package cc.bodyplus.sdk.ble.parse;

import java.lang.ref.SoftReference;

/**
 * Created by Shihoo.Wang 2019/6/6
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class OfflineDataCacheRef {

    private static OfflineDataCacheRef mInstance;

    public synchronized static OfflineDataCacheRef getInstance(){
        if (mInstance == null){
            mInstance = new OfflineDataCacheRef();
        }
        return mInstance;
    }

    private SoftReference<OfflineResultData> offlineResultData;


    public OfflineResultData getOfflineResultData() {
        if (offlineResultData!=null){
            return offlineResultData.get();
        }
        return null;
    }

    public void setOfflineResultData(OfflineResultData offlineResultData) {
        this.offlineResultData = new SoftReference<>(offlineResultData);
    }

}
