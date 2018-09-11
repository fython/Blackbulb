package info.papdt.blackblub.ui.shortcut;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;

import info.papdt.blackblub.IMaskServiceInterface;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.ActionReceiver;
import info.papdt.blackblub.service.MaskService;

public class ToggleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
			Intent intent = new Intent();
			Parcelable icon = Intent.ShortcutIconResource
                    .fromContext(this, R.mipmap.ic_shortcut_switch);

			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_label_switch));
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

			Intent launchIntent = new Intent(getApplicationContext(), ToggleActivity.class);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);

			setResult(RESULT_OK, intent);
			finish();
		} else {
			Intent i = new Intent(this, MaskService.class);
			bindService(i, mServiceConnection, MaskService.BIND_AUTO_CREATE);
		}
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IMaskServiceInterface msi = IMaskServiceInterface.Stub.asInterface(service);
			try {
			    ActionReceiver.sendActionStartOrStop(ToggleActivity.this, !msi.isShowing());
                unbindService(this);
                finish();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		@Override public void onServiceDisconnected(ComponentName name) {}
	};
}
