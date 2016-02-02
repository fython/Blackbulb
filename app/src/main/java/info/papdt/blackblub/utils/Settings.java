package info.papdt.blackblub.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	public static final String PREFERENCES_NAME = "settings";

	public static final String KEY_BRIGHTNESS = "brightness", KEY_ALIVE = "alive",
			KEY_OVERLAY_SYSTEM = "overlay_system", KEY_FIRST_RUN = "first_run",
			KEY_DARK_THEME = "dark_theme";

	private static Settings sInstance;

	private SharedPreferences mPrefs;

	public static Settings getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Settings(context);
		}
		return sInstance;
	}

	private Settings(Context context) {
		mPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	public Settings putBoolean(String key, boolean value) {
		mPrefs.edit().putBoolean(key, value).commit();
		return this;
	}

	public boolean getBoolean(String key, boolean def) {
		return mPrefs.getBoolean(key, def);
	}

	public Settings putInt(String key, int value) {
		mPrefs.edit().putInt(key, value).commit();
		return this;
	}

	public int getInt(String key, int defValue) {
		return mPrefs.getInt(key, defValue);
	}

	public Settings putString(String key, String value) {
		mPrefs.edit().putString(key, value).commit();
		return this;
	}

	public String getString(String key, String defValue) {
		return mPrefs.getString(key, defValue);
	}

}
