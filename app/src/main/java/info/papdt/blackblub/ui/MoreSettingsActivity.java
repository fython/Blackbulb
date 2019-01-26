package info.papdt.blackblub.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import info.papdt.blackblub.R;
import info.papdt.blackblub.util.Settings;

public class MoreSettingsActivity extends Activity {

    private Settings mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSettings = Settings.getInstance(this);

        if (mSettings.isDarkTheme()) {
            setTheme(android.R.style.Theme_Material);
        }

        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(Settings.PREFERENCES_NAME);
            addPreferencesFromResource(R.xml.pref_more_settings);

            findPreference(Settings.KEY_DARK_THEME).setOnPreferenceChangeListener((pref, newValue) -> {
                final Activity parent = getActivity();
                if (parent != null) {
                    Settings.getInstance(parent).setDarkTheme((boolean) newValue);
                    new Handler(Looper.getMainLooper()).postDelayed(parent::recreate, 200);
                    return true;
                } else {
                    return false;
                }
            });
        }

    }

}
