package info.papdt.blackblub.ui.shortcut;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import info.papdt.blackblub.C;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.TileReceiver;
import info.papdt.blackblub.utils.NightScreenSettings;

public class ToggleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
			Intent intent = new Intent();
			Parcelable icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_shortcut_switch);

			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_label_switch));
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

			Intent launchIntent = new Intent(getApplicationContext(), ToggleActivity.class);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);

			setResult(RESULT_OK, intent);
		} else {
			NightScreenSettings settings = NightScreenSettings.getInstance(getApplicationContext());
			boolean isAlive = settings.getBoolean(NightScreenSettings.KEY_ALIVE, false);

			Intent intent = new Intent();
			intent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
			intent.putExtra(C.EXTRA_ACTION, isAlive ? C.ACTION_STOP : C.ACTION_START);
			sendBroadcast(intent);
		}

		finish();
	}

}
