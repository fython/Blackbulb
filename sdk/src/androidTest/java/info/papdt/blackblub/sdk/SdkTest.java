package info.papdt.blackblub.sdk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SdkTest {

    private NightScreenSDK mNightScreenSDK;

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        // Should call bind when activity onStart
        NightScreenSDK.bind(appContext, (sdk, e) -> {
            if (sdk != null) {
                mNightScreenSDK = sdk;
                System.out.println("NightScreenSDK#isEnabled=" + sdk.isEnabled());
            }
            if (e != null) {
                throw new RuntimeException(e);
            }
        });
    }

}
