package info.papdt.blackblub.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zagum.expandicon.ExpandIconView;

import info.papdt.blackblub.Constants;
import info.papdt.blackblub.IMaskServiceInterface;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.ActionReceiver;
import info.papdt.blackblub.service.MaskService;
import info.papdt.blackblub.ui.dialog.SchedulerDialog;
import info.papdt.blackblub.util.AlarmUtil;
import info.papdt.blackblub.util.Settings;
import info.papdt.blackblub.util.Utility;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

public class MainActivity extends Activity {

    // Views & States
    private ImageButton mToggle;
    private SeekBar mSeekBar;
    private ExpandIconView mExpandIcon;
    private View mDivider;

    private View mSchedulerRow;
    private TextView mSchedulerStatus;
    private ImageView mSchedulerIcon;

    private View mDarkThemeRow;
    private Switch mDarkThemeSwitch;

    private View mYellowFilterRow;
    private SeekBar mYellowFilterSeekBar;

    private AlertDialog mFirstRunDialog;

    private static boolean isExpand = false, hasDismissFirstRunDialog = false;

    // Service states
    private boolean isRunning = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMaskServiceInterface msi = IMaskServiceInterface.Stub.asInterface(service);
            try {
                setToggleIconState(isRunning = msi.isShowing());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override public void onServiceDisconnected(ComponentName name) {}
    };

    // Settings
    private Settings mSettings;

    // Local broadcast receivers
    private MessageReceiver mReceiver;

    // Constants
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1001;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSettings = Settings.getInstance(this);

        // Apply theme and transparent system ui
        Utility.applyTransparentSystemUI(this);
        if (mSettings.isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        }

        // Apply Noto Sans CJK Full font from FontProvider API
        Utility.applyNotoSansCJK(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up rootView & aboutButton's click event
        findViewById(R.id.root_layout).setOnClickListener(v -> {
            // When rootView is clicked, exit main activity.
            finish();
        });
        findViewById(R.id.btn_about).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.dialog_about);
            builder.setPositiveButton(android.R.string.ok, (d, i) -> {});
            if (AlipayZeroSdk.hasInstalledAlipayClient(this)) {
                builder.setNeutralButton(R.string.about_donate_alipay,
                        (d, i) -> AlipayZeroSdk.startAlipayClient(
                                MainActivity.this, "aehvyvf4taua18zo6e"));
            }
            builder.show();
        });

        // Set up cardView's top padding and system ui visibility
        LinearLayout cardView = findViewById(R.id.card_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mSettings.isDarkTheme()) {
            // PS: When Blackbulb runs on pre-23 API, it will be hard to see status bar icons
            //     because of light background. I don't want to fix it. User experience requires
            //     users to keep system version not out-of-date.
            cardView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        cardView.setPadding(0, Utility.getStatusBarHeight(this), 0, 0);

        // Set up toggle
        mToggle = findViewById(R.id.toggle);
        mToggle.setOnClickListener(v -> {
            if (!isRunning) {
                if (!Utility.canDrawOverlays(this)) {
                    Utility.requestOverlayPermission(
                            this, REQUEST_CODE_OVERLAY_PERMISSION);
                    return;
                }
                startMaskService();
            } else {
                stopMaskService();
            }
        });

        // Set up seekBar
        mSeekBar = findViewById(R.id.seek_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mSeekBar.setProgress(mSettings.getBrightness(60) - 20, true);
        } else {
            mSeekBar.setProgress(mSettings.getYellowFilterAlpha());
        }
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int currentProgress = -1;
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProgress = progress + 20;
                if (isRunning) {
                    // Only send broadcast when running
                    Intent intent = new Intent(MainActivity.this, MaskService.class);
                    intent.putExtra(Constants.Extra.ACTION, Constants.Action.UPDATE);
                    intent.putExtra(Constants.Extra.BRIGHTNESS, currentProgress);
                    startService(intent);
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentProgress != -1) {
                    mSettings.setBrightness(currentProgress);
                }
            }
        });

        // Set up expandIcon
        mExpandIcon = findViewById(R.id.expand_icon);
        mExpandIcon.setOnClickListener(v -> {
            // Change the states of expandable views
            isExpand = !isExpand;
            updateExpandViews();
        });

        mDivider = findViewById(R.id.divider_line);

        initSchedulerRow();
        initDarkThemeRow();
        initYellowFilterRow();

        updateExpandViews();
    }

    private void updateExpandViews() {
        mExpandIcon.setState(isExpand ? ExpandIconView.LESS : ExpandIconView.MORE, true);
        int visibility = isExpand ? View.VISIBLE : View.GONE;
        mDivider.setVisibility(visibility);
        mSchedulerRow.setVisibility(visibility);
        mDarkThemeRow.setVisibility(visibility);
        mYellowFilterRow.setVisibility(visibility);
    }

    private void initSchedulerRow() {
        mSchedulerRow = findViewById(R.id.scheduler_row);
        mSchedulerIcon = findViewById(R.id.scheduler_icon);
        mSchedulerStatus = findViewById(R.id.tv_scheduler_status);
        Button mSchedulerSettingsButton = findViewById(R.id.btn_scheduler_settings);

        mSchedulerSettingsButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PowerManager pm = getSystemService(PowerManager.class);
                if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.dialog_ignore_battery_opt_title)
                            .setMessage(R.string.dialog_ignore_battery_opt_msg)
                            .setPositiveButton(android.R.string.ok, (d, i) -> showSchedulerDialog())
                            .setNeutralButton(R.string.dialog_button_go_to_set,
                                    (d, i) -> Utility.requestBatteryOptimization(this))
                            .show();
                    return;
                }
            }
            showSchedulerDialog();
        });

        updateSchedulerRow();
    }

    private void initYellowFilterRow() {
        mYellowFilterRow = findViewById(R.id.yellow_filter_row);
        mYellowFilterSeekBar = findViewById(R.id.yellow_filter_seek_bar);

        mYellowFilterSeekBar.setProgress(mSettings.getYellowFilterAlpha());

        mYellowFilterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int currentProgress = -1;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProgress = progress;
                if (isRunning) {
                    // Only send broadcast when running
                    Intent intent = new Intent(MainActivity.this, MaskService.class);
                    intent.putExtra(Constants.Extra.ACTION, Constants.Action.UPDATE);
                    intent.putExtra(Constants.Extra.YELLOW_FILTER_ALPHA, currentProgress);
                    startService(intent);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentProgress != -1) {
                    mSettings.setYellowFilterAlpha(currentProgress);
                }
            }
        });
    }

    private void updateSchedulerRow() {
        mSchedulerIcon.setImageResource(mSettings.isAutoMode() ?
                R.drawable.ic_alarm_black_24dp : R.drawable.ic_alarm_off_black_24dp);
        if (mSettings.isAutoMode()) {
            if (isRunning && AlarmUtil.isInSleepTime(this)) {
                mSchedulerStatus.setText(getString(R.string.scheduler_status_on_show_disable_time,
                        mSettings.getSunriseTimeText()));
            } else {
                mSchedulerStatus.setText(getString(R.string.scheduler_status_on_show_enable_time,
                        mSettings.getSunsetTimeText()));
            }
        } else {
            mSchedulerStatus.setText(R.string.scheduler_status_off);
        }
    }

    private void initDarkThemeRow() {
        mDarkThemeRow = findViewById(R.id.dark_theme_row);
        mDarkThemeSwitch = findViewById(R.id.dark_theme_switch);

        mDarkThemeSwitch.setChecked(mSettings.isDarkTheme());

        mDarkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mSettings.setDarkTheme(isChecked);
            // Restart main activity after theme changed
            Intent intent = Intent.makeRestartActivityTask(getComponentName());
            startActivity(intent);
            finish();
        });
    }

    private void showSchedulerDialog() {
        new SchedulerDialog(this, dialogInterface -> updateSchedulerRow()).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mReceiver == null) {
            mReceiver = new MessageReceiver();
        }
        registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_TOGGLE));

        // Request current state
        bindService(new Intent(this, MaskService.class),
                mServiceConnection, MaskService.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        unbindService(mServiceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Utility.canDrawOverlays(this)) {
                    startMaskService();
                }
            }
        }
    }

    private void setToggleIconState(boolean isRunning) {
        if (mToggle != null && !isFinishing()) {
            mToggle.setImageResource(isRunning ?
                    R.drawable.ic_brightness_2_black_24dp : R.drawable.ic_brightness_7_black_24dp);
        }
    }

    private void startMaskService() {
        Intent intent = new Intent(MainActivity.this, ActionReceiver.class);
        intent.setAction(Constants.ACTION_UPDATE_STATUS);
        intent.putExtra(Constants.Extra.ACTION, Constants.Action.START);
        sendBroadcast(intent);
        setToggleIconState(isRunning = true);

        // For safe
        if (mSettings.isFirstRun()) {
            if (mFirstRunDialog != null && mFirstRunDialog.isShowing()) {
                return;
            }
            hasDismissFirstRunDialog = false;
            mFirstRunDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.dialog_first_run_title)
                    .setMessage(R.string.dialog_first_run_message)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        hasDismissFirstRunDialog = true;
                        mSettings.setFirstRun(false);
                    })
                    .setOnDismissListener(dialogInterface -> {
                        if (hasDismissFirstRunDialog) return;
                        hasDismissFirstRunDialog = true;
                        if (mSettings.isFirstRun()) {
                            Intent intent1 =
                                    new Intent(MainActivity.this, MaskService.class);
                            intent1.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
                            stopService(intent1);
                            setToggleIconState(isRunning = false);
                        }
                    })
                    .show();
            new Handler().postDelayed(() -> {
                if (mFirstRunDialog.isShowing() && !hasDismissFirstRunDialog) {
                    mFirstRunDialog.dismiss();
                }
            }, 5000);
        }
    }

    private void stopMaskService() {
        Intent intent = new Intent(MainActivity.this, MaskService.class);
        intent.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
        startService(intent);
        setToggleIconState(isRunning = false);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mToggle == null) return;
            int eventId = intent.getIntExtra(Constants.Extra.EVENT_ID, -1);
            switch (eventId) {
                case Constants.Event.CANNOT_START:
                    // Receive a error from MaskService
                    isRunning = false;
                    setToggleIconState(false);
                    if (!isFinishing()) {
                        Toast.makeText(
                                context.getApplicationContext(),
                                R.string.mask_fail_to_start,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    break;
                case Constants.Event.DESTROY_SERVICE:
                    // MaskService is destroying
                    if (isRunning) {
                        setToggleIconState(false);
                        isRunning = false;
                    }
                    break;
                case Constants.Event.CHECK:
                    // Receive check event and update toggle icon state
                    isRunning = intent.getBooleanExtra(Constants.Extra.IS_SHOWING, false);
                    Log.i(TAG, "Checked " + isRunning);
                    setToggleIconState(isRunning);
                    break;
            }
        }

    }

}
