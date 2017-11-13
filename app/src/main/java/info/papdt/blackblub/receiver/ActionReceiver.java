package info.papdt.blackblub.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import info.papdt.blackblub.Constants;
import info.papdt.blackblub.service.MaskService;
import info.papdt.blackblub.service.MaskTileService;
import info.papdt.blackblub.util.AlarmUtil;
import info.papdt.blackblub.util.Settings;
import info.papdt.blackblub.util.Utility;

public class ActionReceiver extends BroadcastReceiver {

    private static String TAG = ActionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Settings settings = Settings.getInstance(context);
        Log.i(TAG, "received \"" + intent.getAction() + "\" action");
        if (Constants.ACTION_UPDATE_STATUS.equals(intent.getAction())) {
            int action = intent.getIntExtra(Constants.Extra.ACTION, -1);
            int brightness = intent.getIntExtra(Constants.Extra.BRIGHTNESS, 50);
            int yellowFilterAlpha = intent.getIntExtra(Constants.Extra.YELLOW_FILTER_ALPHA, 0);

            Log.i(TAG, "handle \"" + action + "\" action");
            switch (action) {
                case Constants.Action.START:
                    Intent intent1 = new Intent(context, MaskService.class);
                    intent1.putExtra(Constants.Extra.ACTION, Constants.Action.START);
                    intent1.putExtra(Constants.Extra.BRIGHTNESS, settings.getBrightness(brightness));
                    intent1.putExtra(Constants.Extra.ADVANCED_MODE, settings.getAdvancedMode());
                    intent1.putExtra(Constants.Extra.YELLOW_FILTER_ALPHA,
                            settings.getYellowFilterAlpha(yellowFilterAlpha));
                    Utility.startForegroundService(context, intent1);
                    break;
                case Constants.Action.PAUSE:
                    Intent intent2 = new Intent(context, MaskService.class);
                    intent2.putExtra(Constants.Extra.ACTION, Constants.Action.PAUSE);
                    intent2.putExtra(Constants.Extra.BRIGHTNESS, settings.getBrightness(brightness));
                    intent2.putExtra(Constants.Extra.ADVANCED_MODE, settings.getAdvancedMode());
                    intent2.putExtra(Constants.Extra.YELLOW_FILTER_ALPHA,
                            settings.getYellowFilterAlpha(yellowFilterAlpha));
                    Utility.startForegroundService(context, intent2);
                    break;
                case Constants.Action.STOP:
                    Intent intent3 = new Intent(context, MaskService.class);
                    intent3.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
                    context.startService(intent3);
                    break;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Intent tileUpdateIntent = new Intent(context, MaskTileService.class);
                tileUpdateIntent.putExtra(Constants.Extra.ACTION, action);
                context.startService(tileUpdateIntent);
            }
        } else if (Constants.ACTION_ALARM_START.equals(intent.getAction())) {
            AlarmUtil.updateAlarmSettings(context);
            Intent intent1 = new Intent(context, MaskService.class);
            intent1.putExtra(Constants.Extra.ACTION, Constants.Action.START);
            intent1.putExtra(Constants.Extra.BRIGHTNESS, settings.getBrightness(50));
            intent1.putExtra(Constants.Extra.ADVANCED_MODE, settings.getAdvancedMode());
            intent1.putExtra(Constants.Extra.YELLOW_FILTER_ALPHA, settings.getYellowFilterAlpha());
            Utility.startForegroundService(context, intent1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Intent tileUpdateIntent = new Intent(context, MaskTileService.class);
                tileUpdateIntent.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
                context.startService(tileUpdateIntent);
            }
        } else if (Constants.ACTION_ALARM_STOP.equals(intent.getAction())) {
            AlarmUtil.updateAlarmSettings(context);
            Intent intent1 = new Intent(context, MaskService.class);
            intent1.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
            Utility.startForegroundService(context, intent1);

            // Notify TileService in N
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Intent tileUpdateIntent = new Intent(context, MaskTileService.class);
                tileUpdateIntent.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
                context.startService(tileUpdateIntent);
            }
        }
    }

    /**
     * Send action to ActionReceiver
     * @param context Context
     * @param action Action id
     */
    public static void sendAction(Context context, int action) {
        Intent activeIntent = new Intent(context, ActionReceiver.class);
        activeIntent.setAction(Constants.ACTION_UPDATE_STATUS);
        activeIntent.putExtra(Constants.Extra.ACTION, action);
        context.sendBroadcast(activeIntent);
    }

    /**
     * Send start action
     * @param context Context
     */
    public static void sendActionStart(Context context) {
        sendAction(context, Constants.Action.START);
    }

    /**
     * Send pause action
     * @param context Context
     */
    public static void sendActionPause(Context context) {
        sendAction(context, Constants.Action.PAUSE);
    }

    /**
     * Send stop action
     * @param context Context
     */
    public static void sendActionStop(Context context) {
        sendAction(context, Constants.Action.STOP);
    }

    /**
     * Send start or stop action
     * @param context Context
     * @param shouldStart Whether it should send start action
     */
    public static void sendActionStartOrStop(Context context, boolean shouldStart) {
        if (shouldStart) {
            sendActionStart(context);
        } else {
            sendActionStop(context);
        }
    }

}
