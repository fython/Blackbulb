package info.papdt.blackblub.services;

import android.animation.Animator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;

import info.papdt.blackblub.C;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.TileReceiver;
import info.papdt.blackblub.ui.LaunchActivity;
import info.papdt.blackblub.utils.Utility;

import static android.view.WindowManager.LayoutParams;

public class MaskService extends Service {

	private WindowManager mWindowManager;
	private NotificationManager mNotificationManager;
	private AccessibilityManager mAccessibilityManager;

	private Notification mNoti;

	private View mLayout;
	private WindowManager.LayoutParams mLayoutParams;

	private int mode = C.MODE_NO_PERMISSION;

	private boolean isShowing = false;

	private static final int ANIMATE_DURATION_MILES = 250;
	private static final int NOTIFICATION_NO = 1024;
	private static int brightness = 50;

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
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(LaunchActivity.class.getCanonicalName());
		broadcastIntent.putExtra(C.EXTRA_EVENT_ID, C.EVENT_DESTORY_SERVICE);
		sendBroadcast(broadcastIntent);
	}

	private void createMaskView() {
		mAccessibilityManager.isEnabled();

		updateLayoutParams(mode, -1);
		mLayoutParams.gravity = Gravity.CENTER;

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
			broadcastIntent.setAction(LaunchActivity.class.getCanonicalName());
			broadcastIntent.putExtra(C.EXTRA_EVENT_ID, C.EVENT_CANNOT_START);
			sendBroadcast(broadcastIntent);
		}
	}

	private void updateLayoutParams(int mode, int paramInt) {
		if (mLayoutParams == null) {
			mLayoutParams = new LayoutParams();
		}

		this.mAccessibilityManager.isEnabled();

		switch (mode) {
			case C.MODE_NO_PERMISSION:
				mLayoutParams.type = LayoutParams.TYPE_TOAST;
				break;
			case C.MODE_NORMAL:
			case C.MODE_EYES_CARE:
				mLayoutParams.type = LayoutParams.TYPE_SYSTEM_OVERLAY;
				break;
			case C.MODE_OVERLAY_ALL:
				mLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
				break;
		}
		if (mode == C.MODE_OVERLAY_ALL) {
			mLayoutParams.width = 0;
			mLayoutParams.height = 0;
			mLayoutParams.flags |= LayoutParams.FLAG_DIM_BEHIND;
			mLayoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
			mLayoutParams.flags |= LayoutParams.FLAG_NOT_TOUCHABLE;
			mLayoutParams.flags &= 0xFFDFFFFF;
			mLayoutParams.flags &= 0xFFFFFF7F;
			mLayoutParams.format = PixelFormat.OPAQUE;
			mLayoutParams.dimAmount = constrain((100 - paramInt) / 100.0F, 0.0F, 0.9F);
		} else {
			int max = Math.max(Utility.getTrueScreenWidth(this), Utility.getTrueScreenHeight(this));
			mLayoutParams.height = mLayoutParams.width = max + 200;
			mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
			mLayoutParams.format = PixelFormat.TRANSPARENT;
			float targetAlpha = (100 - brightness) * 0.01f;
			if (paramInt != -1) {
				if (isShowing) {
					if (Math.abs(targetAlpha - mLayout.getAlpha()) < 0.1f) {
						mLayout.setAlpha(targetAlpha);
					} else {
						mLayout.animate().alpha(targetAlpha).setDuration(100).start();
					}
				} else {
					mLayout.animate().alpha(targetAlpha).setDuration(ANIMATE_DURATION_MILES).start();
				}
			}
		}

		if (mLayout != null) {
			mLayout.setBackgroundColor(mode == C.MODE_EYES_CARE ? Color.argb(220, 200, 100, 0) : Color.BLACK);
		}
	}

	private float constrain(float paramFloat1, float paramFloat2, float paramFloat3) {
		if (paramFloat1 < paramFloat2) {
			return paramFloat2;
		}
		if (paramFloat1 > paramFloat3) {
			return paramFloat3;
		}
		return paramFloat1;
	}

	private void destroyMaskView() {
		isShowing = false;
		try {
			Utility.createStatusBarTiles(this, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		cancelNotification();
		if (mLayout != null) {
			mLayout.animate()
					.alpha(0f)
					.setDuration(ANIMATE_DURATION_MILES)
					.setListener(new Animator.AnimatorListener() {
						@Override
						public void onAnimationStart(Animator animator) {

						}

						@Override
						public void onAnimationEnd(Animator animator) {
							try {
								mWindowManager.removeViewImmediate(mLayout);
								mLayout = null;
							} catch (Exception e) {

							}
						}

						@Override
						public void onAnimationCancel(Animator animator) {

						}

						@Override
						public void onAnimationRepeat(Animator animator) {

						}
					});
		}
	}

	private void createNotification() {
		Log.i(TAG, "Create running notification");
		Intent openIntent = new Intent(this, LaunchActivity.class);
		Intent pauseIntent = new Intent();
		pauseIntent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
		Log.i(TAG, "Create "+C.ACTION_PAUSE+" action");
		pauseIntent.putExtra(C.EXTRA_ACTION, C.ACTION_PAUSE);
		pauseIntent.putExtra(C.EXTRA_BRIGHTNESS, brightness);

		Notification.Action pauseAction = new Notification.Action(
				R.drawable.ic_wb_incandescent_black_24dp,
				getString(R.string.notification_action_turn_off),
				PendingIntent.getBroadcast(getBaseContext(), 0, pauseIntent, Intent.FILL_IN_DATA)
		);

		mNoti = new Notification.Builder(getApplicationContext())
				.setContentTitle(getString(R.string.notification_running_title))
				.setContentText(getString(R.string.notification_running_msg))
				.setSmallIcon(R.drawable.ic_brightness_2_white_36dp)
				.addAction(pauseAction)
				.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT))
				.setAutoCancel(false)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setShowWhen(false)
				.build();

	}


	// implement pause notification
	private void createPauseNotification(){
		Log.i(TAG, "Create paused notification");
		Intent openIntent = new Intent(this, LaunchActivity.class);
		Intent resumeIntent = new Intent();
		resumeIntent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
		resumeIntent.putExtra(C.EXTRA_ACTION, C.ACTION_START);
		resumeIntent.putExtra(C.EXTRA_BRIGHTNESS, brightness);

		Intent closeIntent = new Intent(this, MaskService.class);
		closeIntent.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);

		Notification.Action resumeAction = new Notification.Action(R.drawable.ic_wb_incandescent_black_24dp,
				getString(R.string.notification_action_turn_on),
				PendingIntent.getBroadcast(getBaseContext(), 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		mNoti = new Notification.Builder(getApplicationContext())
				.setContentTitle(getString(R.string.notification_paused_title))
				.setContentText(getString(R.string.notification_paused_msg))
				.setSmallIcon(R.drawable.ic_brightness_2_white_36dp)
				.addAction(resumeAction)
				.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT))
				.setAutoCancel(true)
				.setOngoing(false)
				.setOnlyAlertOnce(true)
				.setShowWhen(false)
				.setDeleteIntent(PendingIntent.getService(getBaseContext(), 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT))
				.build();
	}

	private void showPausedNotification(){
		if (mNoti == null) {
			createPauseNotification();
		}
		mNotificationManager.notify(NOTIFICATION_NO, mNoti);
	}

	private void cancelNotification() {
		try {
			mNotificationManager.cancel(NOTIFICATION_NO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int arg) {
		if (intent != null && intent.hasExtra(C.EXTRA_ACTION)) {
			String action = intent.getStringExtra(C.EXTRA_ACTION);
			brightness = intent.getIntExtra(C.EXTRA_BRIGHTNESS, 0);
			mode = intent.getIntExtra(C.EXTRA_MODE, mode);

			switch (action) {
				case C.ACTION_START:
					Log.i(TAG, "Start Mask");
					if (mLayout == null){
						createMaskView();
					}

					createNotification();
					startForeground(NOTIFICATION_NO, mNoti);
					try {
						updateLayoutParams(mode, brightness);
						mWindowManager.updateViewLayout(mLayout, mLayoutParams);
						Utility.createStatusBarTiles(this, true);
					} catch (Exception e) {
						// do nothing....
						e.printStackTrace();
					}
					isShowing = true;
					Log.i(TAG, "Set alpha:" + String.valueOf(100 - intent.getIntExtra(C.EXTRA_BRIGHTNESS, 0)));
					break;
				case C.ACTION_PAUSE:
					Log.i(TAG, "Pause Mask");
					stopForeground(true);
					destroyMaskView();
					createPauseNotification();
					showPausedNotification();
					isShowing = false;
					break;
				case C.ACTION_STOP:
					Log.i(TAG, "Stop Mask");
					isShowing = false;
					stopSelf();
					break;
				case C.ACTION_UPDATE:
					mAccessibilityManager.isEnabled();
					Log.i(TAG, "Update Mask");
					isShowing = true;
					try {
						updateLayoutParams(mode, brightness);
						mWindowManager.updateViewLayout(mLayout, mLayoutParams);
					} catch (Exception e) {
						// do nothing....
					}
					Log.i(TAG, "Set alpha:" + String.valueOf(100 - intent.getIntExtra(C.EXTRA_BRIGHTNESS, 0)));
					break;
			}

		}

		if (intent != null && !intent.getBooleanExtra(C.EXTRA_DO_NOT_SEND_CHECK, false)) {
			Log.i(TAG, "Check Mask. Check from toggle:" + intent.getBooleanExtra(C.EXTRA_CHECK_FROM_TOGGLE, false));
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(
					intent.getBooleanExtra(C.EXTRA_CHECK_FROM_TOGGLE, false)
							? "info.papdt.blackbulb.ACTION_TOGGLE"
							: "info.papdt.blackbulb.ACTION_UPDATE_ACTIVITY_TOGGLE"
			);
			broadcastIntent.putExtra(C.EXTRA_EVENT_ID, C.EVENT_CHECK);
			broadcastIntent.putExtra("isShowing", isShowing);
			sendBroadcast(broadcastIntent);
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	private MaskBinder mBinder = new MaskBinder();

	public class MaskBinder extends Binder {

		public boolean isMaskShowing() {
			return isShowing;
		}

	}

}
