package info.papdt.blackblub.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import info.papdt.blackblub.Constants;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.ActionReceiver;

@TargetApi(Build.VERSION_CODES.N)
public class MaskTileService extends TileService {

    private boolean isRunning = false;
    private static final String TAG = MaskTileService.class.getSimpleName();

    @Override
    public void onClick(){
        Log.d(TAG, "Tile service onClick method called");
        super.onClick();
        Tile tile = getQsTile();
        if (tile == null) return;
        int status = tile.getState();
        Log.d(TAG, "status:" + status + "\t receive");

        switch (status) {
            case Tile.STATE_INACTIVE:
                ActionReceiver.sendActionStart(this);
                updateActiveTile(tile);
                break;
            case Tile.STATE_ACTIVE:
                ActionReceiver.sendActionStop(this);
                updateInactiveTile(tile);
                break;
        }
    }

    private void updateInactiveTile(Tile tile) {
        Icon inActiveIcon = Icon
                .createWithResource(getApplicationContext(),
                        R.drawable.ic_qs_night_mode_off);

        tile.setIcon(inActiveIcon);
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    private void updateActiveTile(Tile tile) {
        Icon activeIcon = Icon
                .createWithResource(getApplicationContext(),
                        R.drawable.ic_qs_night_mode_on);

        tile.setIcon(activeIcon);
        tile.setState(Tile.STATE_ACTIVE);
        tile.updateTile();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int arg) {
        if (intent != null) {
            isRunning = Constants.Action.START == intent.getIntExtra(Constants.Extra.ACTION, -1);
            this.onStartListening();
        }
        return super.onStartCommand(intent, flags, arg);
    }

    @Override
    public void onStartListening() {
        if (getQsTile() != null) {
            if (isRunning) {
                updateActiveTile(getQsTile());
            } else {
                updateInactiveTile(getQsTile());
            }
        }
    }
}
