package info.papdt.blackblub.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import info.papdt.blackblub.C;
import info.papdt.blackblub.services.MaskService;
import info.papdt.blackblub.utils.NightScreenSettings;

public class TileReceiver extends BroadcastReceiver {

	public static final String ACTION_UPDATE_STATUS = "info.papdt.blackbulb.ACTION_UPDATE_STATUS";
	private static int brightness = 50;
	private static String TAG = "TileReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION_UPDATE_STATUS.equals(intent.getAction())) {
			String action = intent.getStringExtra(C.EXTRA_ACTION);
			brightness = intent.getIntExtra(C.EXTRA_BRIGHTNESS, 50);

			Log.i(TAG, "handle \"" + action + "\" action");
			switch (action) {
				case C.ACTION_START:
					Intent intent1 = new Intent(context, MaskService.class);
					intent1.putExtra(C.EXTRA_ACTION, C.ACTION_START);
					intent1.putExtra(C.EXTRA_BRIGHTNESS, NightScreenSettings.getInstance(context).getInt(NightScreenSettings.KEY_BRIGHTNESS, brightness));
					context.startService(intent1);
					break;
				case C.ACTION_PAUSE:
					Intent intent2 = new Intent(context, MaskService.class);
					intent2.putExtra(C.EXTRA_ACTION, C.ACTION_PAUSE);
					intent2.putExtra(C.EXTRA_BRIGHTNESS, NightScreenSettings.getInstance(context).getInt(NightScreenSettings.KEY_BRIGHTNESS, brightness));
					context.startService(intent2);
					break;
				case C.ACTION_STOP:
					Intent intent3 = new Intent(context, MaskService.class);
					intent3.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
					context.startService(intent3);
					break;
			}
		}
	}

}
