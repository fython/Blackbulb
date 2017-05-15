package info.papdt.blackblub.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import info.papdt.blackblub.C;
import info.papdt.blackblub.services.MaskService;
import info.papdt.blackblub.services.MaskTileService;
import info.papdt.blackblub.utils.NightScreenSettings;

public class TileReceiver extends BroadcastReceiver {

	public static final String ACTION_UPDATE_STATUS = "info.papdt.blackbulb.ACTION_UPDATE_STATUS";
	private static String TAG = "TileReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION_UPDATE_STATUS.equals(intent.getAction())) {
			String action = intent.getStringExtra(C.EXTRA_ACTION);
			int brightness = intent.getIntExtra(C.EXTRA_BRIGHTNESS, 50);
			boolean dontSendCheck = intent.getBooleanExtra(C.EXTRA_DO_NOT_SEND_CHECK, false);

			Log.i(TAG, "handle \"" + action + "\" action");
			NightScreenSettings settings = NightScreenSettings.getInstance(context);
			switch (action) {
				case C.ACTION_START:
					Intent intent1 = new Intent(context, MaskService.class);
					intent1.putExtra(C.EXTRA_ACTION, C.ACTION_START);
					intent1.putExtra(C.EXTRA_BRIGHTNESS, settings.getInt(NightScreenSettings.KEY_BRIGHTNESS, brightness));
					intent1.putExtra(C.EXTRA_MODE, settings.getInt(NightScreenSettings.KEY_MODE, C.MODE_NO_PERMISSION));
					intent1.putExtra(C.EXTRA_DO_NOT_SEND_CHECK, dontSendCheck);
					context.startService(intent1);
					break;
				case C.ACTION_PAUSE:
					Intent intent2 = new Intent(context, MaskService.class);
					intent2.putExtra(C.EXTRA_ACTION, C.ACTION_PAUSE);
					intent2.putExtra(C.EXTRA_BRIGHTNESS, settings.getInt(NightScreenSettings.KEY_BRIGHTNESS, brightness));
					intent2.putExtra(C.EXTRA_MODE, settings.getInt(NightScreenSettings.KEY_MODE, C.MODE_NO_PERMISSION));
					intent2.putExtra(C.EXTRA_DO_NOT_SEND_CHECK, dontSendCheck);
					context.startService(intent2);
					break;
				case C.ACTION_STOP:
					Intent intent3 = new Intent(context, MaskService.class);
					intent3.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
					intent3.putExtra(C.EXTRA_DO_NOT_SEND_CHECK, dontSendCheck);
					context.startService(intent3);
					break;
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				Intent tileUpdateIntent = new Intent(context, MaskTileService.class);
				tileUpdateIntent.putExtra(C.EXTRA_ACTION, action);
				context.startService(tileUpdateIntent);
			}
		}
	}

}
