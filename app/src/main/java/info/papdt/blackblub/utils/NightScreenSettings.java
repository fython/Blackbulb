package info.papdt.blackblub.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("CommitPrefEdits")
public class NightScreenSettings {

	public static final String PREFERENCES_NAME = "settings";

	public static final String KEY_BRIGHTNESS = "brightness",
			KEY_MODE = "mode", KEY_FIRST_RUN = "first_run",
			KEY_DARK_THEME = "dark_theme", KEY_AUTO_MODE = "auto_mode",
			KEY_HOURS_SUNRISE = "hrs_sunrise", KEY_MINUTES_SUNRISE = "min_sunrise",
			KEY_HOURS_SUNSET = "hrs_sunset", KEY_MINUTES_SUNSET = "min_sunset";

	private volatile static NightScreenSettings sInstance;

	private SharedPreferences mPrefs;

	public static NightScreenSettings getInstance(Context context) {
		if (sInstance == null) {
			synchronized (NightScreenSettings.class) {
				if (sInstance == null) {
					sInstance = new NightScreenSettings(context);
				}
			}
		}
		return sInstance;
	}

	private NightScreenSettings(Context context) {
		mPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
	}

	public NightScreenSettings putBoolean(String key, boolean value) {
		mPrefs.edit().putBoolean(key, value).commit();
		return this;
	}

	public boolean getBoolean(String key, boolean def) {
		return mPrefs.getBoolean(key, def);
	}

	public NightScreenSettings putInt(String key, int value) {
		mPrefs.edit().putInt(key, value).commit();
		return this;
	}

	public int getInt(String key, int defValue) {
		return mPrefs.getInt(key, defValue);
	}

	public NightScreenSettings putString(String key, String value) {
		mPrefs.edit().putString(key, value).commit();
		return this;
	}

	public String getString(String key, String defValue) {
		return mPrefs.getString(key, defValue);
	}

}
