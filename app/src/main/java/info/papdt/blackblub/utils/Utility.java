package info.papdt.blackblub.utils;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class Utility {

	public static int getTrueScreenHeight(Context context) {
		int dpi = 0;
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		if (Build.VERSION.SDK_INT >= 17) {
			display.getRealMetrics(dm);
			dpi = dm.heightPixels;
		} else {
			try {
				Class c = Class.forName("android.view.Display");
				Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
				method.invoke(display, dm);
				dpi = dm.heightPixels;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return dpi;
	}

	public static int getTrueScreenWidth(Context context) {
		int dpi = 0;
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		if (Build.VERSION.SDK_INT >= 17) {
			display.getRealMetrics(dm);
			dpi = dm.widthPixels;
		} else {
			try {
				Class c = Class.forName("android.view.Display");
				Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
				method.invoke(display, dm);
				dpi = dm.widthPixels;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return dpi;
	}

}
