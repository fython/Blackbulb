package info.papdt.blackblub.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import info.papdt.blackblub.R;
import info.papdt.blackblub.util.Settings;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

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

            findPreference("about").setOnPreferenceClickListener(pref -> {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(R.layout.dialog_about);
                builder.setPositiveButton(android.R.string.ok, (d, i) -> {});
                if (AlipayZeroSdk.hasInstalledAlipayClient(getActivity())) {
                    builder.setNeutralButton(R.string.about_donate_alipay,
                            (d, i) -> AlipayZeroSdk.startAlipayClient(getActivity(), "aehvyvf4taua18zo6e"));
                }
                builder.show();
                return true;
            });
            findPreference("telegram").setOnPreferenceClickListener(pref -> {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://t.me/gwo_apps"));
                startActivity(intent);
                return true;
            });
        }

    }

}
