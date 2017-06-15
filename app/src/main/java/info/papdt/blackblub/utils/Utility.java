package info.papdt.blackblub.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import cyanogenmod.app.CMStatusBarManager;
import cyanogenmod.app.CustomTile;
import info.papdt.blackblub.C;
import info.papdt.blackblub.another.R;
import info.papdt.blackblub.receiver.TileReceiver;

import java.util.Calendar;

@SuppressWarnings("unchecked")
public class Utility {

	public static final int CM_TILE_CODE = 1001;

	public static final int REQUEST_ALARM_SUNRISE = 1002, REQUEST_ALARM_SUNSET = 1003;

	public static final String TAG = Utility.class.getSimpleName();

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

	public static void updateAlarmSettings(Context context) {
		NightScreenSettings settings = NightScreenSettings.getInstance(context);
		if (settings.getBoolean(NightScreenSettings.KEY_AUTO_MODE, false)) {
			int hrsSunrise = settings.getInt(NightScreenSettings.KEY_HOURS_SUNRISE, 6);
			int minSunrise = settings.getInt(NightScreenSettings.KEY_MINUTES_SUNRISE, 0);
			int hrsSunset = settings.getInt(NightScreenSettings.KEY_HOURS_SUNSET, 22);
			int minSunset = settings.getInt(NightScreenSettings.KEY_MINUTES_SUNSET, 0);

			Calendar now = Calendar.getInstance();
			Calendar sunriseCalendar = (Calendar) now.clone();
			Calendar sunsetCalendar = (Calendar) now.clone();

			sunriseCalendar.set(Calendar.HOUR_OF_DAY, hrsSunrise);
			sunriseCalendar.set(Calendar.MINUTE, minSunrise);
			sunriseCalendar.set(Calendar.SECOND, 0);
			sunriseCalendar.set(Calendar.MILLISECOND, 0);
			if (sunriseCalendar.before(now)) sunriseCalendar.add(Calendar.DATE, 1);

			sunsetCalendar.set(Calendar.HOUR_OF_DAY, hrsSunset);
			sunsetCalendar.set(Calendar.MINUTE, minSunset);
			sunsetCalendar.set(Calendar.SECOND, 0);
			sunsetCalendar.set(Calendar.MILLISECOND, 0);
			if (sunsetCalendar.before(now)) sunsetCalendar.add(Calendar.DATE, 1);

			Log.i(TAG, "Reset alarm");

			cancelAlarm(context, REQUEST_ALARM_SUNRISE, C.ALARM_ACTION_STOP);
			cancelAlarm(context, REQUEST_ALARM_SUNSET, C.ALARM_ACTION_START);
			setAlarm(context,
					AlarmManager.RTC_WAKEUP,
					sunriseCalendar.getTimeInMillis(),
					AlarmManager.INTERVAL_DAY,
					REQUEST_ALARM_SUNRISE,
					C.ALARM_ACTION_STOP);
			setAlarm(context,
					AlarmManager.RTC_WAKEUP,
					sunsetCalendar.getTimeInMillis(),
					AlarmManager.INTERVAL_DAY,
					REQUEST_ALARM_SUNSET,
					C.ALARM_ACTION_START);
		} else {
			Log.i(TAG, "Cancel alarm");
			cancelAlarm(context, REQUEST_ALARM_SUNRISE, C.ALARM_ACTION_STOP);
			cancelAlarm(context, REQUEST_ALARM_SUNSET, C.ALARM_ACTION_START);
		}
	}

	private static void setAlarm(Context context, int type, long triggerAtMillis,
	                      long intervalMillis, int requestCode, String action) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(type, triggerAtMillis, intervalMillis, PendingIntent.getBroadcast(context,
				requestCode, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT));
	}

	private static void cancelAlarm(Context context, int requestCode, String action) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(PendingIntent.getBroadcast(context,
				requestCode, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT));
	}

	public static float dpToPx(Context context, float dp) {
		Resources r = context.getResources();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
	}

}
