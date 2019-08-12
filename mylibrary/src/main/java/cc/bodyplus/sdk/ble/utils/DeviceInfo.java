package cc.bodyplus.sdk.ble.utils;

import java.io.Serializable;

/**
 * Created by shihu.wang on 2017/3/25.
 * Email shihu.wang@bodyplus.cc
 */
public class DeviceInfo implements Serializable{
    /**
     * The Sn.
     */
    public String sn;
    /**
     * The Ble name.
     */
    public String bleName;
    /**
     * The Hw vn.
     */
    public String hwVn;
    /**
     * The Sw vn.
     */
    public String swVn;
    /**
     * The Ble vn.
     */
    @Deprecated
    public String bleVn;

    public String dfu;
    public String dfuAPP;
    public byte coreType;

    public String macAddress;

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "sn='" + sn + '\'' +
                ", bleName='" + bleName + '\'' +
                ", hwVn='" + hwVn + '\'' +
                ", swVn='" + swVn + '\'' +
                ", bleVn='" + bleVn + '\'' +
                ", dfu='" + dfu + '\'' +
                ", dfuAPP='" + dfuAPP + '\'' +
                ", coreType=" + coreType +
                ", macAddress=" + macAddress +
                '}';
    }
}
