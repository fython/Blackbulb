package info.papdt.blackblub.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

public final class Utility {

    private static final int UI_VISIBILITY_TRANSPARENT_LOLLIPOP =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    public static void applyTransparentSystemUI(Activity activity) {
        Window window = activity.getWindow();
        window.getDecorView().setSystemUiVisibility(UI_VISIBILITY_TRANSPARENT_LOLLIPOP);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
