package cc.bodyplus.sdk.ble.dfu;

import java.io.Serializable;

/**
 * Created by Shihoo.Wang 2019/7/8
 * Email shihu.wang@bodyplus.cc  451082005@qq.com
 */
public class DfuUpdateInfo implements Serializable {
    public String swVn = "0";
    public String hwVn = "0";
    public String log = "";
    public String filePath = "";
    public String firmPackageUrl = "";
}
