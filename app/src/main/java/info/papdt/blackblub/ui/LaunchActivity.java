package info.papdt.blackblub.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import info.papdt.blackblub.C;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.TileReceiver;
import info.papdt.blackblub.services.MaskService;
import info.papdt.blackblub.utils.Settings;
import info.papdt.blackblub.utils.Utility;

public class LaunchActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

	private MessageReceiver mReceiver;

	private DiscreteSeekBar mSeekbar;
	private MaterialAnimatedSwitch mSwitch;

	private PopupMenu popupMenu;

	private boolean isRunning = false, hasDismissFirstRunDialog = false;
	private Settings mSettings;

	private static final int OVERLAY_PERMISSION_REQ_CODE = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSettings = Settings.getInstance(getApplicationContext());

		// Don't worry too much. Min SDK is 21.
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		getWindow().setNavigationBarColor(Color.TRANSPARENT);

		if (mSettings.getBoolean(Settings.KEY_DARK_THEME, false)) {
			setTheme(R.style.AppTheme_Dark);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		Intent i = new Intent(this, MaskService.class);
		i.putExtra(C.EXTRA_ACTION, C.ACTION_CHECK);
		startService(i);

		// Publish CM Tiles
		try {
			Utility.createStatusBarTiles(this, isRunning);
		} catch (Exception e) {

		}

		mSwitch = (MaterialAnimatedSwitch) findViewById(R.id.toggle);
		mSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(boolean b) {
				if (b) {
					Intent intent = new Intent();
					intent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
					intent.putExtra(C.EXTRA_ACTION, C.ACTION_START);
					sendBroadcast(intent);
					isRunning = true;

					// For safe
					if (mSettings.getBoolean(Settings.KEY_FIRST_RUN, true)) {
						hasDismissFirstRunDialog = false;
						final AlertDialog dialog = new AlertDialog.Builder(LaunchActivity.this)
								.setTitle(R.string.dialog_first_run_title)
								.setMessage(R.string.dialog_first_run_message)
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										hasDismissFirstRunDialog = true;
										mSettings.putBoolean(Settings.KEY_FIRST_RUN, false);
									}
								})
								.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialogInterface) {
										if (hasDismissFirstRunDialog) return;
										hasDismissFirstRunDialog = true;
										mSwitch.toggle();
										if (mSettings.getBoolean(Settings.KEY_FIRST_RUN, true)) {
											Intent intent = new Intent(LaunchActivity.this, MaskService.class);
											intent.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
											stopService(intent);
											isRunning = false;
										}
									}
								})
								.show();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								if (dialog.isShowing() && !hasDismissFirstRunDialog) {
									dialog.dismiss();
								}
							}
						}, 5000);
					}
				} else {
					Intent intent = new Intent(LaunchActivity.this, MaskService.class);
					intent.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
					stopService(intent);
					isRunning = false;
				}
			}
		});

		mSeekbar = (DiscreteSeekBar) findViewById(R.id.seek_bar);
		mSeekbar.setProgress(mSettings.getInt(Settings.KEY_BRIGHTNESS, 50));
		mSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
			int v = -1;
			@Override
			public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
				v = value;
				if (isRunning) {
					Intent intent = new Intent(LaunchActivity.this, MaskService.class);
					intent.putExtra(C.EXTRA_ACTION, C.ACTION_UPDATE);
					intent.putExtra(C.EXTRA_BRIGHTNESS, mSeekbar.getProgress());
					startService(intent);
				}
			}

			@Override
			public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
				if (v != -1) {
					mSettings.putInt(Settings.KEY_BRIGHTNESS, v);
				}
			}
		});

		ImageButton menuBtn = (ImageButton) findViewById(R.id.btn_menu);
		popupMenu = new PopupMenu(this, menuBtn);
		popupMenu.getMenuInflater().inflate(R.menu.menu_settings, popupMenu.getMenu());
		popupMenu.getMenu()
				.findItem(R.id.action_overlay_system)
				.setChecked(mSettings.getBoolean(Settings.KEY_OVERLAY_SYSTEM, false));
		popupMenu.getMenu()
				.findItem(R.id.action_dark_theme)
				.setChecked(mSettings.getBoolean(Settings.KEY_DARK_THEME, false));
		popupMenu.setOnMenuItemClickListener(this);
		menuBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				popupMenu.show();
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			menuBtn.setOnTouchListener(popupMenu.getDragToOpenListener());
		}

		FrameLayout rootLayout = (FrameLayout) findViewById(R.id.root_layout);
		rootLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mReceiver == null) {
			mReceiver = new MessageReceiver();
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(LaunchActivity.class.getCanonicalName());
		registerReceiver(mReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSettings.putInt(Settings.KEY_BRIGHTNESS, mSeekbar.getProgress());
	}

	@Override
	public void onStop() {
		super.onStop();
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (android.provider.Settings.canDrawOverlays(this)) {
					mSettings.putBoolean(Settings.KEY_OVERLAY_SYSTEM, true);
					popupMenu.getMenu().findItem(R.id.action_overlay_system).setChecked(true);
				}
			}
		}
	}

	@Override
	public boolean onMenuItemClick(final MenuItem menuItem) {
		int id = menuItem.getItemId();
		if (id == R.id.action_about) {
			new AlertDialog.Builder(this)
					.setView(R.layout.dialog_about)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							// Do nothing....
						}
					})
					.show();
			return true;
		} else if (id == R.id.action_overlay_system) {
			if (menuItem.isChecked()) {
				mSettings.putBoolean(Settings.KEY_OVERLAY_SYSTEM, false);
				menuItem.setChecked(false);
			} else {
				// http://stackoverflow.com/questions/32061934/permission-from-manifest-doesnt-work-in-android-6/32065680#32065680
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					if (!android.provider.Settings.canDrawOverlays(this)) {
						Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
								Uri.parse("package:" + getPackageName()));
						startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
					} else {
						mSettings.putBoolean(Settings.KEY_OVERLAY_SYSTEM, true);
						menuItem.setChecked(true);
					}
				} else {
					new AlertDialog.Builder(this)
							.setTitle(R.string.dialog_overlay_enable_title)
							.setMessage(R.string.dialog_overlay_enable_message)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									mSettings.putBoolean(Settings.KEY_OVERLAY_SYSTEM, true);
									menuItem.setChecked(true);
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									// Do nothing....
								}
							})
							.show();
				}
			}
			return true;
		} else if (id == R.id.action_dark_theme) {
			mSettings.putBoolean(Settings.KEY_DARK_THEME, !menuItem.isChecked());
			menuItem.setChecked(!menuItem.isChecked());
			finish();
			startActivity(new Intent(this, LaunchActivity.class));
			return true;
		}
		return false;
	}

	private class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int eventId = intent.getIntExtra(C.EXTRA_EVENT_ID, -1);
			switch (eventId) {
				case C.EVENT_CANNOT_START:
					// Receive a error from MaskService
					mSettings.putBoolean(Settings.KEY_ALIVE, false);
					isRunning = false;
					try {
						mSwitch.toggle();
						Toast.makeText(
								LaunchActivity.this,
								R.string.mask_fail_to_start,
								Toast.LENGTH_LONG
						).show();
					} finally {

					}
					break;
				case C.EVENT_DESTORY_SERVICE:
					if (isRunning) {
						mSettings.putBoolean(Settings.KEY_ALIVE, false);
						mSwitch.toggle();
						isRunning = false;
					}
					break;
				case C.EVENT_CHECK:
					isRunning = intent.getBooleanExtra("isShowing", false);
					if (isRunning) {
						// If I don't use postDelayed, Switch may cause a NPE because its animator wasn't initialized.
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								mSwitch.toggle();
							}
						}, 100);
					}
					break;
			}
		}

	}

}
