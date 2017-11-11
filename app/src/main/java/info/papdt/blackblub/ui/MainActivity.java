package info.papdt.blackblub.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import info.papdt.blackblub.R;
import info.papdt.blackblub.util.Utility;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Apply transparent system ui
        Utility.applyTransparentSystemUI(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up rootView's click event
        findViewById(R.id.root_layout).setOnClickListener(v -> {
            // When rootView is clicked, exit main activity.
            finish();
        });

        // Set up cardView's top padding and system ui visibility
        LinearLayout cardView = findViewById(R.id.card_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // PS: When Blackbulb runs on pre-23 API, it will be hard to see status bar icons
            //     because of light background. I don't want to fix it. User experience requires
            //     users to keep system version not out-of-date.
            cardView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        cardView.setPadding(0, Utility.getStatusBarHeight(this), 0, 0);


    }

}
