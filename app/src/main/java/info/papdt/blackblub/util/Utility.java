package info.papdt.blackblub.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cyanogenmod.app.CMStatusBarManager;
import cyanogenmod.app.CustomTile;
import info.papdt.blackblub.Constants;
import info.papdt.blackblub.R;
import moe.shizuku.fontprovider.FontProviderClient;

/**
 * @author Fung Gwo (fython) fython@163.com
 */
public final class Utility {

    private static boolean sFontInitialized = false;

    private static final int CM_TILE_CODE = 1001;

    private static final int UI_VISIBILITY_TRANSPARENT_LOLLIPOP =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    /**
     * Make your activity's status bar and navigation bar transparent
     * @param activity Current activity
     */
    public static void applyTransparentSystemUI(Activity activity) {
        Window window = activity.getWindow();
        window.getDecorView().setSystemUiVisibility(UI_VISIBILITY_TRANSPARENT_LOLLIPOP);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    /**
     * Apply Noto Sans CJK Full-size font to application for better display
     * @param context Context
     */
    public static void applyNotoSansCJK(Context context) {
        if (!sFontInitialized) {
            FontProviderClient client = FontProviderClient.create(context);
            if (client != null) {
                client.setNextRequestReplaceFallbackFonts(true);
                client.replace("Noto Sans CJK", "sans-serif", "sans-serif-medium");
            }
            sFontInitialized = true;
        }
    }

    /**
     * Get the height of status bar
     * @param context Context
     * @return The height of status bar
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Get real metrics of default display
     * @param context Context
     * @return The real metrics of default display
     */
    private static DisplayMetrics getDefaultDisplayRealMetrics(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display;
        if (windowManager != null && (display = windowManager.getDefaultDisplay()) != null) {
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            return dm;
        } else {
            return null;
        }
    }

    /**
     * Get the real height of screen
     * @param context Context
     * @return The real height of screen
     */
    public static int getRealScreenHeight(Context context) {
        DisplayMetrics dm = getDefaultDisplayRealMetrics(context);
        return dm != null ? dm.heightPixels : 0;
    }

    /**
     * Get the real width of screen
     * @param context Context
     * @return The real width of screen
     */
    public static int getRealScreenWidth(Context context) {
        DisplayMetrics dm = getDefaultDisplayRealMetrics(context);
        return dm != null ? dm.widthPixels : 0;
    }

    /**
     * Start foreground service (Must call Service.startForeground after starting)
     * @param context Context
     * @param intent Service Intent
     */
    public static void startForegroundService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /**
     * Check if application can draw over other apps
     * @param context Context
     * @return Boolean
     */
    public static boolean canDrawOverlays(Context context) {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.M) {
            if (sdkInt == Build.VERSION_CODES.O) {
                // Sometimes Settings.canDrawOverlays returns false after allowing permission.
                // Google Issue Tracker: https://issuetracker.google.com/issues/66072795
                AppOpsManager appOpsMgr = context.getSystemService(AppOpsManager.class);
                if (appOpsMgr != null) {
                    int mode = appOpsMgr.checkOpNoThrow(
                            "android:system_alert_window",
                            android.os.Process.myUid(),
                            context.getPackageName()
                    );
                    return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
                } else {
                    return false;
                }
            }
            // Default
            return android.provider.Settings.canDrawOverlays(context);
        }
        return true; // This fallback may returns a incorrect result.
    }

    /**
     * Request overlay permission to draw over other apps
     * @param activity Current activity
     * @param requestCode Request code
     */
    public static void requestOverlayPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, requestCode);
        }
        // TODO Support third-party customize ROM?
    }

    /**
     * Request doze mode disable
     * @param activity Current activity
     */
    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint("BatteryLife")
    public static void requestBatteryOptimization(Activity activity) {
        try {
            Intent intent = new Intent(
                    android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert dp into px unit
     * @param context Context
     * @param dp origin value (Unit: dp)
     * @return value in px unit
     */
    public static float dpToPx(Context context, float dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    /**
     * Create a tiles on the status bar through CyanogenMod SDK -- Fung Jichun
     * You can learn more from: https://cyngn.com/developer-blog/introducing-the-cyanogen-platform-sdk
     * @param context Context
     * @param nowStatus Now
     */
    public static void createStatusBarTiles(Context context, boolean nowStatus) {
        try {
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_UPDATE_STATUS);
            intent.putExtra(Constants.Extra.ACTION,
                    nowStatus ? Constants.Action.STOP : Constants.Action.START);

            CustomTile customTile = new CustomTile.Builder(context)
                    .shouldCollapsePanel(false)
                    .setLabel(nowStatus ? R.string.notification_action_turn_off : R.string.app_name)
                    .setIcon(nowStatus ?
                            R.drawable.ic_qs_night_mode_on : R.drawable.ic_qs_night_mode_off)
                    .setOnClickIntent(PendingIntent.getBroadcast(
                            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .build();

            CMStatusBarManager.getInstance(context).publishTile(CM_TILE_CODE, customTile);
        } catch (Exception e) {
            Log.d("Utility", "Failed to create CM status bar tile. Ignore it.");
        }
    }

}
