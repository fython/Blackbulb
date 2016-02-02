package info.papdt.blackblub.services;

import android.animation.Animator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import info.papdt.blackblub.C;
import info.papdt.blackblub.ui.LaunchActivity;
import info.papdt.blackblub.utils.Settings;
import info.papdt.blackblub.utils.Utility;

public class MaskService extends Service {

	private WindowManager mWindowManager;

	private LinearLayout mLayout;
	private WindowManager.LayoutParams mLayoutParams;

	private Settings mSettings;
	private boolean enableOverlaySystem;

	private static final int ANIMATE_DURATION_MILES = 250;

	private static final String TAG = MaskService.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();

		mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
		mSettings = Settings.getInstance(getApplicationContext());
		enableOverlaySystem = mSettings.getBoolean(Settings.KEY_OVERLAY_SYSTEM, false);
		createMaskView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mLayout != null) {
			mSettings.putBoolean(Settings.KEY_ALIVE, false);
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

	private void createMaskView() {
		mLayoutParams = new WindowManager.LayoutParams();
		mLayoutParams.type = !enableOverlaySystem ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		mLayoutParams.format = PixelFormat.TRANSPARENT;
		// TODO Automatically change by listening to rotating
		mLayoutParams.height = Utility.getTrueScreenHeight(getApplicationContext()) + 200;
		mLayoutParams.width = Utility.getTrueScreenHeight(getApplicationContext()) + 200;
		mLayoutParams.gravity = Gravity.CENTER;

		if (mLayout == null) {
			mLayout = new LinearLayout(this);
			mLayout.setLayoutParams(
					new LinearLayout.LayoutParams(
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

	@Override
	public int onStartCommand(Intent intent, int flags, int arg) {
		if (intent != null) {
			String action = intent.getStringExtra(C.EXTRA_ACTION);
			float targetAlpha = (100 - intent.getIntExtra(C.EXTRA_BRIGHTNESS, 0)) * 0.01f;
			boolean temp = intent.getBooleanExtra(C.EXTRA_USE_OVERLAY_SYSTEM, false);
			switch (action) {
				case C.ACTION_START:
					Log.i(TAG, "Start Mask");
					if (temp != enableOverlaySystem) {
						mLayoutParams.type = !enableOverlaySystem ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
						enableOverlaySystem = temp;
					}

					mSettings.putBoolean(Settings.KEY_ALIVE, true);
					try {
						mWindowManager.updateViewLayout(mLayout, mLayoutParams);
						mLayout.animate()
								.alpha(targetAlpha)
								.setDuration(ANIMATE_DURATION_MILES)
								.start();
					} catch (Exception e) {
						// do nothing....
					}
					Log.i(TAG, "Set alpha:" + String.valueOf(100 - intent.getIntExtra(C.EXTRA_BRIGHTNESS, 0)));
					break;
				case C.ACTION_STOP:
					Log.i(TAG, "Stop Mask");
					this.onDestroy();
					break;
				case C.ACTION_UPDATE:
					Log.i(TAG, "Update Mask");
					if (temp != enableOverlaySystem) {
						mLayoutParams.type = !enableOverlaySystem ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
						enableOverlaySystem = temp;
						try {
							mWindowManager.updateViewLayout(mLayout, mLayoutParams);
						} catch (Exception e) {
							// do nothing....
						}
					}

					mSettings.putBoolean(Settings.KEY_ALIVE, true);
					if (Math.abs(targetAlpha - mLayout.getAlpha()) < 0.1f) {
						mLayout.setAlpha(targetAlpha);
					} else {
						mLayout.animate()
								.alpha(targetAlpha)
								.setDuration(100)
								.start();
					}
					Log.i(TAG, "Set alpha:" + String.valueOf(100 - intent.getIntExtra(C.EXTRA_BRIGHTNESS, 0)));
					break;
			}
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
