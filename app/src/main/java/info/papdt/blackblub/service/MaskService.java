package info.papdt.blackblub.service;

import android.animation.Animator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;

import info.papdt.blackblub.Constants;
import info.papdt.blackblub.IMaskServiceInterface;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.ActionReceiver;
import info.papdt.blackblub.ui.MainActivity;
import info.papdt.blackblub.util.Utility;

import static android.view.WindowManager.LayoutParams.*;

public class MaskService extends Service {

    // System Services
    private WindowManager mWindowManager;
    private NotificationManager mNotificationManager;
    private AccessibilityManager mAccessibilityManager;

    // Binder
    private MaskServiceBinder mBinder = new MaskServiceBinder();

    // Notification
    private Notification mNotification;

    // Floating Window
    private View mLayout;
    private WindowManager.LayoutParams mLayoutParams;

    // If floating window is showing
    private boolean isShowing = false;

    // Options
    private int mBrightness = 50;
    private int mAdvancedMode = Constants.AdvancedMode.NONE;

    // Constants
    private static final int ANIMATE_DURATION_MILES = 250;
    private static final int NOTIFICATION_NO = 1024;

    private static final String TAG = MaskService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mAccessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMaskView();

        // Notify MainActivity
        Intent broadcastIntent = new Intent(MainActivity.class.getCanonicalName());
        broadcastIntent.putExtra(Constants.Extra.EVENT_ID, Constants.Event.DESTROY_SERVICE);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int arg) {
        int action = -1;
        if (intent != null && intent.hasExtra(Constants.Extra.ACTION)) {
            action = intent.getIntExtra(Constants.Extra.ACTION, -1);
            mBrightness = intent.getIntExtra(Constants.Extra.BRIGHTNESS, 0);
            mAdvancedMode = intent.getIntExtra(Constants.Extra.ADVANCED_MODE, mAdvancedMode);

            switch (action) {
                case Constants.Action.START:
                    Log.i(TAG, "Start Mask");
                    if (mLayout == null){
                        createMaskView();
                    }
                    createNotification();
                    startForeground(NOTIFICATION_NO, mNotification);
                    try {
                        updateLayoutParams(mBrightness);
                        mWindowManager.updateViewLayout(mLayout, mLayoutParams);
                    } catch (Exception e) {
                        // do nothing....
                        e.printStackTrace();
                    }
                    isShowing = true;
                    break;
                case Constants.Action.PAUSE:
                    Log.i(TAG, "Pause Mask");
                    stopForeground(true);
                    destroyMaskView();
                    createPauseNotification();
                    showPausedNotification();
                    isShowing = false;
                    break;
                case Constants.Action.STOP:
                    Log.i(TAG, "Stop Mask");
                    isShowing = false;
                    destroyMaskView();
                    cancelNotification();
                    stopForeground(true);
                    stopSelf();
                    break;
                case Constants.Action.UPDATE:
                    mAccessibilityManager.isEnabled();
                    Log.i(TAG, "Update Mask");
                    isShowing = true;
                    try {
                        updateLayoutParams(mBrightness);
                        mWindowManager.updateViewLayout(mLayout, mLayoutParams);
                    } catch (Exception e) {
                        // do nothing....
                    }
                    Log.i(TAG, "Set alpha:" +
                            (100 - intent.getIntExtra(Constants.Extra.BRIGHTNESS, 0)));
                    break;
            }
        }

        // Send implicit broadcast to Blackbulb's components for updating running status
        if (intent != null) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(Constants.ACTION_TOGGLE);
            broadcastIntent.putExtra(Constants.Extra.EVENT_ID, Constants.Event.CHECK);
            broadcastIntent.putExtra(Constants.Extra.IS_SHOWING, isShowing);
            sendBroadcast(broadcastIntent);
        }

        return action == Constants.Action.STOP ? START_NOT_STICKY : START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public int getWindowType() {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {
            // Only TYPE_APPLICATION_OVERLAY is available in O.
            return TYPE_APPLICATION_OVERLAY;
        }
        // Default window type.
        int result = TYPE_SYSTEM_OVERLAY;
        if (mAdvancedMode == Constants.AdvancedMode.NO_PERMISSION
                && sdkInt < Build.VERSION_CODES.N) {
            // Toast Mode cannot work normally after N. Window will be set 10~ secs max timeout.
            result = TYPE_TOAST;
        } else if (mAdvancedMode == Constants.AdvancedMode.OVERLAY_ALL) {
            // It seems that this mode should use TYPE_SYSTEM_ERROR as window type.
            result = TYPE_SYSTEM_ERROR;
        }
        return result;
    }

    private void createMaskView() {
        updateLayoutParams(-1);

        if (mLayout == null) {
            mLayout = new View(this);
            mLayout.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );
            mLayout.setBackgroundColor(Color.BLACK);
            mLayout.setAlpha(0f);
        }

        try {
            mWindowManager.addView(mLayout, mLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.class.getCanonicalName());
            broadcastIntent.putExtra(Constants.Extra.EVENT_ID, Constants.Event.CANNOT_START);
            sendBroadcast(broadcastIntent);
        }
    }

    private void updateLayoutParams(int paramInt) {
        if (mLayoutParams == null) {
            mLayoutParams = new WindowManager.LayoutParams();
        }

        // Hacky method. However, I don't know how it works.
        mAccessibilityManager.isEnabled();

        // Apply layout params type & gravity
        mLayoutParams.type = getWindowType();
        mLayoutParams.gravity = Gravity.CENTER;

        // Apply layout params attributes
        if (getWindowType() == TYPE_SYSTEM_ERROR) {
            // This is the reason why it will not affect users' application installation.
            // Mask window won't cover any views.
            mLayoutParams.width = 0;
            mLayoutParams.height = 0;
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            // I haven't found what this two value mean. :p (I got them from another screen filter app)
            mLayoutParams.flags &= 0xFFDFFFFF;
            mLayoutParams.flags &= 0xFFFFFF7F;
            mLayoutParams.format = PixelFormat.OPAQUE;
            // Screen is dimmed by system.
            mLayoutParams.dimAmount = constrain((100 - paramInt) / 100.0F, 0.0F, 0.9F);
        } else {
            // A dirty fix to deal with screen rotation.
            int max = Math.max(
                    Utility.getRealScreenWidth(this),
                    Utility.getRealScreenHeight(this)
            );
            mLayoutParams.height = mLayoutParams.width = max + 200;

            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            mLayoutParams.format = PixelFormat.TRANSPARENT;

            // Set mask alpha to adjust screen brightness
            float targetAlpha = (100 - mBrightness) * 0.01f;
            if (paramInt != -1) {
                if (isShowing) {
                    // Start animation when value changes a lot.
                    if (Math.abs(targetAlpha - mLayout.getAlpha()) < 0.1f) {
                        mLayout.setAlpha(targetAlpha);
                    } else {
                        mLayout.animate().alpha(targetAlpha).setDuration(100).start();
                    }
                } else {
                    mLayout.animate().alpha(targetAlpha)
                            .setDuration(ANIMATE_DURATION_MILES).start();
                }
            }
        }

        if (mLayout != null) {
            // TODO Eyes care mode
            mLayout.setBackgroundColor(Color.BLACK);
        }
    }

    private void destroyMaskView() {
        isShowing = false;
        cancelNotification();
        if (mLayout != null) {
            mLayout.animate()
                    .alpha(0f)
                    .setDuration(ANIMATE_DURATION_MILES)
                    .setListener(new Animator.AnimatorListener() {
                        private View readyToRemoveView = mLayout;
                        @Override public void onAnimationStart(Animator animator) {}
                        @Override public void onAnimationCancel(Animator animator) {}
                        @Override public void onAnimationRepeat(Animator animator) {}
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            try {
                                mWindowManager.removeViewImmediate(readyToRemoveView);
                            } catch (Exception e) {
                                // Just do nothing
                            }
                        }
                    }).start();
            mLayout = null;
        }
    }

    private void createNotification() {
        Log.i(TAG, "Create running notification");

        // Create open and pause action intents
        Intent openIntent = new Intent(this, MainActivity.class);
        Intent pauseIntent = new Intent(this, ActionReceiver.class);
        pauseIntent.setAction(Constants.ACTION_UPDATE_STATUS);
        pauseIntent.putExtra(Constants.Extra.ACTION, Constants.Action.PAUSE);
        pauseIntent.putExtra(Constants.Extra.BRIGHTNESS, mBrightness);
        Notification.Action pauseAction = new Notification.Action(
                R.drawable.ic_wb_incandescent_black_24dp,
                getString(R.string.notification_action_turn_off),
                PendingIntent.getBroadcast(getBaseContext(), 0, pauseIntent, Intent.FILL_IN_DATA)
        );

        // Create notification
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.notification_running_title))
                .setContentText(getString(R.string.notification_running_msg))
                .setSmallIcon(R.drawable.ic_brightness_2_white_36dp)
                .addAction(pauseAction)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                        0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID_RS);
        }
        mNotification = builder.build();
    }

    private void createPauseNotification(){
        Log.i(TAG, "Create paused notification");

        // Create resume and close action intents
        Intent openIntent = new Intent(this, MainActivity.class);
        Intent resumeIntent = new Intent(this, ActionReceiver.class);
        resumeIntent.setAction(Constants.ACTION_UPDATE_STATUS);
        resumeIntent.putExtra(Constants.Extra.ACTION, Constants.Action.START);
        resumeIntent.putExtra(Constants.Extra.BRIGHTNESS, mBrightness);
        Intent closeIntent = new Intent(this, MaskService.class);
        closeIntent.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
        Notification.Action resumeAction = new Notification.Action(
                R.drawable.ic_wb_incandescent_black_24dp,
                getString(R.string.notification_action_turn_on),
                PendingIntent.getBroadcast(
                        getBaseContext(), 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT
                ));

        // Create notification
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.notification_paused_title))
                .setContentText(getString(R.string.notification_paused_msg))
                .setSmallIcon(R.drawable.ic_brightness_2_white_36dp)
                .addAction(resumeAction)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                                openIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setDeleteIntent(PendingIntent.getService(getBaseContext(), 0,
                        closeIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID_RS);
        }
        mNotification = builder.build();
    }

    private void showPausedNotification(){
        if (mNotification == null) {
            createPauseNotification();
        }
        mNotificationManager.notify(NOTIFICATION_NO, mNotification);
    }

    private void cancelNotification() {
        try {
            mNotificationManager.cancel(NOTIFICATION_NO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static float constrain(float paramFloat1, float paramFloat2, float paramFloat3) {
        if (paramFloat1 < paramFloat2) {
            return paramFloat2;
        }
        if (paramFloat1 > paramFloat3) {
            return paramFloat3;
        }
        return paramFloat1;
    }

    public class MaskServiceBinder extends IMaskServiceInterface.Stub {

        MaskService getService() {
            return MaskService.this;
        }

        @Override
        public boolean isShowing() {
            return getService() != null && getService().isShowing();
        }

    }

}
