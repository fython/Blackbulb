package info.papdt.blackblub.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import info.papdt.blackblub.C;
import info.papdt.blackblub.services.MaskService;
import info.papdt.blackblub.utils.Settings;

public class TileReceiver extends BroadcastReceiver {

	public static final String ACTION_UPDATE_STATUS = "info.papdt.blackbulb.ACTION_UPDATE_STATUS";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION_UPDATE_STATUS.equals(intent.getAction())) {
			switch (intent.getStringExtra(C.EXTRA_ACTION)) {
				case C.ACTION_START:
					Intent intent1 = new Intent(context, MaskService.class);
					intent1.putExtra(C.EXTRA_ACTION, C.ACTION_START);
					intent1.putExtra(C.EXTRA_BRIGHTNESS, Settings.getInstance(context).getInt(Settings.KEY_BRIGHTNESS, 50));
					context.startService(intent1);
					break;
				case C.ACTION_STOP:
					Intent intent2 = new Intent(context, MaskService.class);
					intent2.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
					context.stopService(intent2);
					break;
			}
		}
	}

}
