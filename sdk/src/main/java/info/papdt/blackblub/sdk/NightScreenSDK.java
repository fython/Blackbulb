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

    private IMaskServiceInterface service;

    private boolean isAlive;

    private NightScreenSDK(IMaskServiceInterface service) {
        this.service = service;
        try {
            service.isShowing();
            this.isAlive = true;
        } catch (Exception e) {
            e.printStackTrace();
            this.isAlive = false;
        }
    }

    private void release() {
        isAlive = false;
        service = null;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isEnabled() {
        if (!isAlive) {
            throw new RuntimeException("MaskService is not alive!");
        }
        try {
            return service.isShowing();
        } catch (RemoteException e) {
            return false;
        }
    }

    public int getBrightness() {
        if (!isAlive) {
            throw new RuntimeException("MaskService is not alive!");
        }
        try {
            return service.getBrightness();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getYellowFilterAlpha() {
        if (!isAlive) {
            throw new RuntimeException("MaskService is not alive!");
        }
        try {
            return service.getYellowFilterAlpha();
        } catch (RemoteException e) {
            return 0;
        }
    }

    private static class BindServiceConnection implements ServiceConnection {

        private final InitCallback callback;

        private NightScreenSDK sdkInstance;

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
                sdkInstance = new NightScreenSDK(maskService);
                callback.onResult(sdkInstance, null);
            } catch (Exception e) {
                callback.onResult(null, e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (sdkInstance != null) {
                sdkInstance.release();
            }
        }

    }

    public interface InitCallback {

        void onResult(@Nullable NightScreenSDK sdk, @Nullable Exception e);

    }

}
