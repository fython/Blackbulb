package info.papdt.blackblub.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import info.papdt.blackblub.C;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.TileReceiver;
import info.papdt.blackblub.services.MaskService;
import info.papdt.blackblub.ui.adapter.ModeListAdapter;
import info.papdt.blackblub.utils.NightScreenSettings;
import info.papdt.blackblub.utils.Utility;

public class LaunchActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

	private DiscreteSeekBar mSeekbar;
	private static MaterialAnimatedSwitch mSwitch;
	private TextView mModeText;
	private ImageButton mSchedulerBtn;

	private PopupMenu popupMenu;
	private AlertDialog mAlertDialog, mModeDialog;

	private static boolean isRunning = false, hasDismissFirstRunDialog = false;
	private int targetMode;
	private NightScreenSettings mNightScreenSettings;

	private static final int OVERLAY_PERMISSION_REQ_CODE = 1001;

	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mNightScreenSettings = NightScreenSettings.getInstance(getApplicationContext());

		// Don't worry too much. Min SDK is 21.
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		getWindow().setNavigationBarColor(Color.TRANSPARENT);

		if (mNightScreenSettings.getBoolean(NightScreenSettings.KEY_DARK_THEME, false)) {
			setTheme(R.style.AppTheme_Dark);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		Intent i = new Intent(this, MaskService.class);
		startService(i);

		// Publish CM Tiles
		try {
			Utility.createStatusBarTiles(this, isRunning);
		} catch (Exception e) {

		}

		mSwitch = findViewById(R.id.toggle);
		mSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(boolean b) {
				if (b) {
					Intent intent = new Intent();
					intent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
					intent.putExtra(C.EXTRA_ACTION, C.ACTION_START);
					intent.putExtra(C.EXTRA_DO_NOT_SEND_CHECK, true);
					sendBroadcast(intent);
					isRunning = true;

					// For safe
					if (mNightScreenSettings.getBoolean(NightScreenSettings.KEY_FIRST_RUN, true)) {
						if (mAlertDialog != null && mAlertDialog.isShowing()) {
							return;
						}
						hasDismissFirstRunDialog = false;
						mAlertDialog = new AlertDialog.Builder(LaunchActivity.this)
								.setTitle(R.string.dialog_first_run_title)
								.setMessage(R.string.dialog_first_run_message)
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										hasDismissFirstRunDialog = true;
										mNightScreenSettings.putBoolean(NightScreenSettings.KEY_FIRST_RUN, false);
									}
								})
								.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialogInterface) {
										if (hasDismissFirstRunDialog) return;
										hasDismissFirstRunDialog = true;
										mSwitch.toggle();
										if (mNightScreenSettings.getBoolean(NightScreenSettings.KEY_FIRST_RUN, true)) {
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
								if (mAlertDialog.isShowing() && !hasDismissFirstRunDialog) {
									mAlertDialog.dismiss();
								}
							}
						}, 5000);
					}
				} else {
					Intent intent = new Intent(LaunchActivity.this, MaskService.class);
					intent.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
					intent.putExtra(C.EXTRA_DO_NOT_SEND_CHECK, true);
					stopService(intent);
					isRunning = false;
				}
			}
		});

		mSeekbar = findViewById(R.id.seek_bar);
		mSeekbar.setProgress(mNightScreenSettings.getInt(NightScreenSettings.KEY_BRIGHTNESS, 50));
		mSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
			int v = -1;
			@Override
			public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
				v = value;
				if (isRunning) {
					Intent intent = new Intent(LaunchActivity.this, MaskService.class);
					intent.putExtra(C.EXTRA_ACTION, C.ACTION_UPDATE);
					intent.putExtra(C.EXTRA_BRIGHTNESS, mSeekbar.getProgress());
					intent.putExtra(C.EXTRA_DO_NOT_SEND_CHECK, true);
					startService(intent);
				}
			}

			@Override
			public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
				if (v != -1) {
					mNightScreenSettings.putInt(NightScreenSettings.KEY_BRIGHTNESS, v);
				}
			}
		});

		mModeText = findViewById(R.id.mode_view);
		int mode = mNightScreenSettings.getInt(NightScreenSettings.KEY_MODE, C.MODE_NO_PERMISSION);
		mModeText.setText(getResources().getStringArray(R.array.mode_text)[mode]
		+ ((mode == C.MODE_NO_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
				? " " + getString(R.string.mode_text_no_permission_warning)
				: ""));
		findViewById(R.id.mode_view_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int current = mNightScreenSettings.getInt(NightScreenSettings.KEY_MODE, C.MODE_NO_PERMISSION);
				mModeDialog = new AlertDialog.Builder(LaunchActivity.this)
						.setTitle(R.string.dialog_choose_mode)
						.setSingleChoiceItems(
								new ModeListAdapter(current),
								current,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (which == 0) {
											((ModeListAdapter) mModeDialog.getListView().getAdapter())
													.setCurrent(which);
											applyNewMode(which);
										} else {
											// http://stackoverflow.com/questions/32061934/permission-from-manifest-doesnt-work-in-android-6/32065680#32065680
											if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
												if (!android.provider.Settings.canDrawOverlays(LaunchActivity.this)) {
													targetMode = which;
													Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
															Uri.parse("package:" + getPackageName()));
													startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
												} else {
													applyNewMode(which);
												}
											} else {
												targetMode = which;
												new AlertDialog.Builder(LaunchActivity.this)
														.setTitle(R.string.dialog_overlay_enable_title)
														.setMessage(R.string.dialog_overlay_enable_message)
														.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialogInterface, int i) {
																applyNewMode(targetMode);
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
										mModeDialog.dismiss();
									}
								}
						)
						.setNegativeButton(android.R.string.cancel, null)
						.show();
			}
		});

		ImageButton menuBtn = findViewById(R.id.btn_menu);
		popupMenu = new PopupMenu(this, menuBtn);
		popupMenu.getMenuInflater().inflate(R.menu.menu_settings, popupMenu.getMenu());
		popupMenu.getMenu()
				.findItem(R.id.action_dark_theme)
				.setChecked(mNightScreenSettings.getBoolean(NightScreenSettings.KEY_DARK_THEME, false));
		popupMenu.setOnMenuItemClickListener(this);
		menuBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				popupMenu.show();
			}
		});
		menuBtn.setOnTouchListener(popupMenu.getDragToOpenListener());

		mSchedulerBtn = findViewById(R.id.btn_scheduler);
		if (mNightScreenSettings.getBoolean(NightScreenSettings.KEY_AUTO_MODE, false)) {
			mSchedulerBtn.setImageResource(R.drawable.ic_alarm_black_24dp);
		} else {
			mSchedulerBtn.setImageResource(R.drawable.ic_alarm_off_black_24dp);
		}
		mSchedulerBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					PowerManager pm = getSystemService(PowerManager.class);
					if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
						new AlertDialog.Builder(LaunchActivity.this)
								.setTitle(R.string.dialog_ignore_battery_opt_title)
								.setMessage(R.string.dialog_ignore_battery_opt_msg)
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										showSchedulerDialog();
									}
								})
								.setNeutralButton(R.string.dialog_button_go_to_set, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										try {
											Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
											intent.setData(Uri.parse("package:" + getPackageName()));
											startActivity(intent);
										} catch (ActivityNotFoundException e) {
											e.printStackTrace();
										}
									}
								})
								.show();
						return;
					}
					showSchedulerDialog();
				}
			}
		});

		FrameLayout rootLayout = findViewById(R.id.root_layout);
		rootLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}

	private void showSchedulerDialog() {
		new SchedulerDialog(this, new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				if (mNightScreenSettings.getBoolean(NightScreenSettings.KEY_AUTO_MODE, false)) {
					mSchedulerBtn.setImageResource(R.drawable.ic_alarm_black_24dp);
				} else {
					mSchedulerBtn.setImageResource(R.drawable.ic_alarm_off_black_24dp);
				}
			}
		}).show();
	}

	@Override
	public void onPause() {
		super.onPause();
		mNightScreenSettings.putInt(NightScreenSettings.KEY_BRIGHTNESS, mSeekbar.getProgress());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSwitch = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (android.provider.Settings.canDrawOverlays(this)) {
					applyNewMode(targetMode);
				}
			}
		}
	}

	@SuppressLint("SetTextI18n")
	private void applyNewMode(int targetMode) {
		if (isRunning && targetMode != mNightScreenSettings.getInt(NightScreenSettings.KEY_MODE, C.MODE_NO_PERMISSION)) {
			mSwitch.toggle();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mSwitch.toggle();
				}
			}, 500);
		}
		mNightScreenSettings.putInt(NightScreenSettings.KEY_MODE, targetMode);
		mModeText.setText(getResources().getStringArray(R.array.mode_text)[targetMode]
				+ ((targetMode == C.MODE_NO_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
				? " " + getString(R.string.mode_text_no_permission_warning)
				: ""));
	}

	@Override
	public boolean onMenuItemClick(final MenuItem menuItem) {
		int id = menuItem.getItemId();
		if (id == R.id.action_about) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(R.layout.dialog_about);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							// Do nothing....
						}
					});
			if (AlipayZeroSdk.hasInstalledAlipayClient(this)) {
				builder.setNeutralButton(R.string.about_donate_alipay, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						AlipayZeroSdk.startAlipayClient(LaunchActivity.this, "aehvyvf4taua18zo6e");
					}
				});
			}
			builder.show();
			return true;
		} else if (id == R.id.action_dark_theme) {
			mNightScreenSettings.putBoolean(NightScreenSettings.KEY_DARK_THEME, !menuItem.isChecked());
			menuItem.setChecked(!menuItem.isChecked());
			finish();
			startActivity(new Intent(this, LaunchActivity.class));
			return true;
		}
		return false;
	}

	public static class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mSwitch == null) return;
			int eventId = intent.getIntExtra(C.EXTRA_EVENT_ID, -1);
			switch (eventId) {
				case C.EVENT_CANNOT_START:
					// Receive a error from MaskService
					isRunning = false;
					try {
						mSwitch.toggle();
						Toast.makeText(
								context.getApplicationContext(),
								R.string.mask_fail_to_start,
								Toast.LENGTH_LONG
						).show();
					} finally {

					}
					break;
				case C.EVENT_DESTORY_SERVICE:
					if (isRunning) {
						mSwitch.toggle();
						isRunning = false;
					}
					break;
				case C.EVENT_CHECK:
					Log.i("C", "Checked" + intent.getBooleanExtra("isShowing", false));
					if (isRunning = intent.getBooleanExtra("isShowing", false) != mSwitch.isChecked()) {
						// If I don't use postDelayed, Switch may cause a NPE because its animator wasn't initialized.
						mHandler.sendEmptyMessageDelayed(1, 100);
					}
					break;
			}
		}

	}

	@SuppressLint("HandlerLeak")
	private static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what < 10) {
				if (mSwitch == null) {
					mHandler.sendEmptyMessageDelayed(msg.what + 1, 100);
				} else {
					mSwitch.toggle();
				}
			}
		}

	};

}
