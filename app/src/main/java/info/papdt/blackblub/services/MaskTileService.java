package info.papdt.blackblub.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import 	android.service.quicksettings.TileService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import info.papdt.blackblub.C;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.TileReceiver;
import info.papdt.blackblub.utils.Settings;

@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
public class MaskTileService extends TileService {
    private Tile tile;
    private static final String TAG = MaskTileService.class.getSimpleName();
    private Settings mSettings;
    @Override
    public void onClick(){
        Log.i(TAG, "Tile service onClick method called");
        super.onClick();
        tile = getQsTile();
        int status = tile.getState();
        Log.i(TAG, "status:"+status+"\t receive");

        switch (status){
            case Tile.STATE_ACTIVE:
                updateActiveTile(tile);
                break;
            case Tile.STATE_INACTIVE:
                updateInactiveTile(tile);
                break;
            default:
                break;
        }
    }

    private void updateInactiveTile(Tile tile) {
        Intent inActiveIntent = new Intent();
        inActiveIntent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
        inActiveIntent.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
        sendBroadcast(inActiveIntent);

        Icon inActiveIcon = Icon
                .createWithResource(getApplicationContext(),
                        R.drawable.ic_wb_sunny_white_36dp);

        tile.setIcon(inActiveIcon);
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }

    private void updateActiveTile(Tile tile) {
        Intent activeIntent = new Intent();
        activeIntent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
        activeIntent.putExtra(C.EXTRA_ACTION, C.ACTION_START);
        sendBroadcast(activeIntent);

        Icon activeIcon = Icon
                .createWithResource(getApplicationContext(),
                        R.drawable.ic_brightness_2_white_36dp);

        tile.setIcon(activeIcon);
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onStartListening(){
        tile = getQsTile();
        mSettings = Settings.getInstance(getApplicationContext());
        boolean isAlive = mSettings.getBoolean(Settings.KEY_ALIVE, false);
        boolean isPaused = mSettings.getBoolean(C.ACTION_PAUSE, false);

        if (isAlive){
            updateActiveTile(tile);
        }
        else if (!isPaused){
            updateInactiveTile(tile);
        }
    }
}
