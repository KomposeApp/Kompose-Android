package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.net.InetAddress;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class StateService extends BaseService {
    private final String DEVICE_UUID = "device_uuid";
    private final String SETTING_KEY = "kompose";

    private UUID deviceUUID;

    private SessionModel liveSession;

    public SessionModel getLiveSession() {
        return liveSession;
    }

    public void setLiveSession(SessionModel liveSession) {
        this.liveSession = liveSession;
    }

    private SharedPreferences getPreferences() {
        return getSharedPreferences(SETTING_KEY, MODE_PRIVATE);
    }

    public UUID getDeviceUUID() {
        boolean newDeviceUUID = false;

        synchronized (this) {
            if (deviceUUID == null) {
                if (!getPreferences().contains(DEVICE_UUID)) {
                    deviceUUID = UUID.randomUUID();
                    newDeviceUUID = true;
                } else {
                    deviceUUID = UUID.fromString(getPreferences().getString(DEVICE_UUID, null));
                }
            }
        }

        if (newDeviceUUID) {
            SharedPreferences.Editor editor = getPreferences().edit();
            editor.putString(DEVICE_UUID, deviceUUID.toString());
            editor.apply();
        }

        return deviceUUID;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
