package info.papdt.blackblub.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import cyanogenmod.app.CMStatusBarManager;
import cyanogenmod.app.CustomTile;
import info.papdt.blackblub.C;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.TileReceiver;

@SuppressWarnings("unchecked")
public class Utility {

	public static final int CM_TILE_CODE = 1001;

	public static int getTrueScreenHeight(Context context) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getRealMetrics(dm);
		int dpi = dm.heightPixels;

		return dpi;
	}

	public static int getTrueScreenWidth(Context context) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getRealMetrics(dm);
		int dpi = dm.widthPixels;

		return dpi;
	}

	/** Create a tiles on the status bar through CyanogenMod SDK -- Fung Jichun
	 *  You can learn more from: https://cyngn.com/developer-blog/introducing-the-cyanogen-platform-sdk */
	public static void createStatusBarTiles(Context context, boolean nowStatus) {
		Intent intent = new Intent();
		intent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
		intent.putExtra(C.EXTRA_ACTION, nowStatus ? C.ACTION_STOP : C.ACTION_START);

		CustomTile customTile = new CustomTile.Builder(context)
				.shouldCollapsePanel(false)
				.setLabel(nowStatus ? R.string.notification_action_turn_off : R.string.app_name)
				.setIcon(nowStatus ? R.drawable.ic_brightness_2_white_36dp : R.drawable.ic_wb_sunny_white_36dp)
				.setOnClickIntent(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
				.build();

		CMStatusBarManager.getInstance(context).publishTile(CM_TILE_CODE, customTile);
	}

}
