package info.papdt.blackblub.util;

import android.content.Context;
import android.content.SharedPreferences;

import info.papdt.blackblub.Constants;

public class Settings {

	private static final String PREFERENCES_NAME = "settings";

	public static final String KEY_BRIGHTNESS = "brightness",
			KEY_ADVANCED_MODE = "advanced_mode", KEY_FIRST_RUN = "first_run",
			KEY_DARK_THEME = "dark_theme", KEY_AUTO_MODE = "auto_mode",
			KEY_HOURS_SUNRISE = "hrs_sunrise", KEY_MINUTES_SUNRISE = "min_sunrise",
			KEY_HOURS_SUNSET = "hrs_sunset", KEY_MINUTES_SUNSET = "min_sunset";

	private volatile static Settings sInstance;

	private SharedPreferences mPrefs;

	public static Settings getInstance(Context context) {
		if (sInstance == null) {
			synchronized (Settings.class) {
				if (sInstance == null) {
					sInstance = new Settings(context.getApplicationContext());
				}
			}
		}
		return sInstance;
	}

	private Settings(Context context) {
		mPrefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
	}

	public Settings putBoolean(String key, boolean value) {
		mPrefs.edit().putBoolean(key, value).apply();
		return this;
	}

	public boolean getBoolean(String key, boolean def) {
		return mPrefs.getBoolean(key, def);
	}

	public Settings putInt(String key, int value) {
		mPrefs.edit().putInt(key, value).apply();
		return this;
	}

	public int getInt(String key, int defValue) {
		return mPrefs.getInt(key, defValue);
	}

	public Settings putString(String key, String value) {
		mPrefs.edit().putString(key, value).apply();
		return this;
	}

	public String getString(String key, String defValue) {
		return mPrefs.getString(key, defValue);
	}

	public int getBrightness(int defValue) {
	    return getInt(KEY_BRIGHTNESS, defValue);
    }

    public int getAdvancedMode() {
	    return getInt(KEY_ADVANCED_MODE, Constants.AdvancedMode.NONE);
    }

    public boolean isFirstRun() {
	    return getBoolean(KEY_FIRST_RUN, true);
    }

    public boolean isDarkTheme() {
	    return getBoolean(KEY_DARK_THEME, false);
    }

    public void setBrightness(int brightness) {
	    putInt(KEY_BRIGHTNESS, brightness);
    }

    public void setAdvancedMode(int advancedMode) {
	    putInt(KEY_ADVANCED_MODE, advancedMode);
    }

    public void setFirstRun(boolean isFirstRun) {
	    putBoolean(KEY_FIRST_RUN, isFirstRun);
    }

    public void setDarkTheme(boolean useDarkTheme) {
        putBoolean(KEY_DARK_THEME, useDarkTheme);
    }

}
