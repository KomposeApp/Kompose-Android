package ch.ethz.inf.vs.kompose.service.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.network.ConnectionDetails;

/**
 * Created by git@famoser.ch on 21/11/2017.
 */

public abstract class BasePreferencesService extends BaseService {

    public static final String DEVICE_UUID = "device_uuid";
    public static final String KEY_PRELOAD = "k_preload";
    public static final String KEY_USERNAME = "k_user";

    private String deviceUUIDString;
    private UUID deviceUUID;

    private void ensureDeviceUUIDSet() {
        boolean newDeviceUUID = false;

        synchronized (this) {
            if (deviceUUIDString == null) {
                SharedPreferences preferences = getSharedPreferences();
                if (!preferences.contains(DEVICE_UUID)) {
                    deviceUUID = UUID.randomUUID();
                    deviceUUIDString = deviceUUID.toString();
                    newDeviceUUID = true;
                } else {
                    deviceUUIDString = preferences.getString(DEVICE_UUID, null);
                    deviceUUID = UUID.fromString(deviceUUIDString);
                }
            }
        }

        if (newDeviceUUID) {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(DEVICE_UUID, deviceUUIDString);
            editor.apply();
        }
    }

    public String getDeviceUUIDString() {
        ensureDeviceUUIDSet();

        return deviceUUIDString;
    }

    public UUID getDeviceUUID() {
        ensureDeviceUUIDSet();

        return deviceUUID;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
