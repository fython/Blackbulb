package info.papdt.blackblub.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.content.Intent;
import android.util.Log;

import info.papdt.blackblub.C;
import info.papdt.blackblub.R;
import info.papdt.blackblub.receiver.TileReceiver;

@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
public class MaskTileService extends TileService {

	private boolean isRunning = false;
	private static final String TAG = MaskTileService.class.getSimpleName();

    @Override
    public void onClick(){
        Log.i(TAG, "Tile service onClick method called");
        super.onClick();
	    Tile tile = getQsTile();
	    if (tile == null) return;
        int status = tile.getState();
        Log.i(TAG, "status:"+status+"\t receive");

        switch (status){
            case Tile.STATE_INACTIVE:
	            Intent activeIntent = new Intent();
	            activeIntent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
	            activeIntent.putExtra(C.EXTRA_ACTION, C.ACTION_START);
	            activeIntent.putExtra(C.EXTRA_DO_NOT_SEND_CHECK, false);
	            sendBroadcast(activeIntent);
                updateActiveTile(tile);
                break;
            case Tile.STATE_ACTIVE:
	            Intent inActiveIntent = new Intent();
	            inActiveIntent.setAction(TileReceiver.ACTION_UPDATE_STATUS);
	            inActiveIntent.putExtra(C.EXTRA_ACTION, C.ACTION_STOP);
	            inActiveIntent.putExtra(C.EXTRA_DO_NOT_SEND_CHECK, false);
	            sendBroadcast(inActiveIntent);
                updateInactiveTile(tile);
                break;
            default:
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
		    isRunning = C.ACTION_START.equals(intent.getStringExtra(C.EXTRA_ACTION));
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
