package cc.bodyplus.sdk.ble.ota;


import android.app.Activity;

import no.nordicsemi.android.dfu.DfuBaseService;

/**
 * Created by shihu.wang on 2016/8/9.
 * Email shihu.wang@bodyplus.cc
 */
public class OTAService extends DfuBaseService {

	@Override
	protected Class<? extends Activity> getNotificationTarget() {
		return NotificationActivity.class;
	}
}
