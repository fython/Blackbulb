package info.papdt.blackblub;

import android.app.Application;
import info.papdt.blackblub.utils.Utility;

public class BlackbulbApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Utility.updateAlarmSettings(this);
	}

}
