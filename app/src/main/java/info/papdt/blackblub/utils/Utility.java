package info.papdt.blackblub.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

import cyanogenmod.app.CMStatusBarManager;
import cyanogenmod.app.CustomTile;
import info.papdt.blackblub.C;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.TileReceiver;
import info.papdt.blackblub.services.MaskService;

public class Utility {

	public static final int CM_TILE_CODE = 1001;

	public static int getTrueScreenHeight(Context context) {
		int dpi = 0;
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		if (Build.VERSION.SDK_INT >= 17) {
			display.getRealMetrics(dm);
			dpi = dm.heightPixels;
		} else {
			try {
				Class c = Class.forName("android.view.Display");
				Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
				method.invoke(display, dm);
				dpi = dm.heightPixels;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return dpi;
	}

	public static int getTrueScreenWidth(Context context) {
		int dpi = 0;
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		if (Build.VERSION.SDK_INT >= 17) {
			display.getRealMetrics(dm);
			dpi = dm.widthPixels;
		} else {
			try {
				Class c = Class.forName("android.view.Display");
				Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
				method.invoke(display, dm);
				dpi = dm.widthPixels;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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
