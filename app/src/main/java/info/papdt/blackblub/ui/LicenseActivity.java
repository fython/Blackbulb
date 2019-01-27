package info.papdt.blackblub.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import info.papdt.blackblub.util.Settings;

public class LicenseActivity extends Activity {

    private static final String LICENSE_URL = "file:///android_asset/licenses.html";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Settings settings = Settings.getInstance(this);

        if (settings.isDarkTheme()) {
            setTheme(android.R.style.Theme_Material);
        }

        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (mWebView == null) {
            mWebView = new WebView(this);
        }
        setContentView(mWebView);

        mWebView.loadUrl(LICENSE_URL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
