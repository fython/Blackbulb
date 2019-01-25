package info.papdt.blackblub.sdk;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import info.papdt.blackblub.IMaskServiceInterface;

import static java.util.Objects.requireNonNull;

public final class NightScreenSDK {

    private static final Intent SERVICE_INTENT = new Intent("info.papdt.blackblub.IMaskServiceInterface");

    private final IMaskServiceInterface service;

    private NightScreenSDK(IMaskServiceInterface service) {
        this.service = service;
    }

    public static ServiceConnection bind(@NonNull Context context,
                                         @NonNull InitCallback callback) {
        requireNonNull(context);
        requireNonNull(callback);
        final ServiceConnection connection = new BindServiceConnection(callback);
        try {
            context.bindService(
                    SERVICE_INTENT,
                    connection,
                    Service.BIND_AUTO_CREATE
            );
        } catch (Exception e) {
            callback.onResult(null, e);
        }
        return connection;
    }

    public boolean isEnabled() {
        try {
            return service.isShowing();
        } catch (RemoteException e) {
            return false;
        }
    }

    public int getBrightness() {
        try {
            return service.getBrightness();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getYellowFilterAlpha() {
        try {
            return service.getYellowFilterAlpha();
        } catch (RemoteException e) {
            return 0;
        }
    }

    private static class BindServiceConnection implements ServiceConnection {

        private final InitCallback callback;

        BindServiceConnection(InitCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service == null) {
                callback.onResult(null, new NullPointerException(
                        "ServiceConnection returns null service."));
            }
            try {
                final IMaskServiceInterface maskService =
                        IMaskServiceInterface.Stub.asInterface(service);
                callback.onResult(new NightScreenSDK(maskService), null);
            } catch (Exception e) {
                callback.onResult(null, e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    }

    public interface InitCallback {

        void onResult(@Nullable NightScreenSDK sdk, @Nullable Exception e);

    }

}
