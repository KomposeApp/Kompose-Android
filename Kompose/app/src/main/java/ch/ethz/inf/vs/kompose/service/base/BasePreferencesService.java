package ch.ethz.inf.vs.kompose.service.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

public abstract class BasePreferencesService extends BaseService {

    public static final String KEY_DEVICE_UUID = "device_uuid";
    public static final String KEY_PRELOAD = "k_preload";
    public static final String KEY_USERNAME = "k_user";
    public static final String KEY_PORT = "k_port";

    public static final String DEFAULT_UUID = null;
    public static final int DEFAULT_PORT = 0;
    public static final String DEFAULT_USERNAME = "John Doe";
    public static final int DEFAULT_PRELOAD = 3;

    private String deviceUUIDString;
    private UUID deviceUUID;

    private void ensureDeviceUUIDSet() {
        boolean newDeviceUUID = false;

        synchronized (this) {
            if (deviceUUIDString == null) {
                SharedPreferences preferences = getSharedPreferences();
                if (!preferences.contains(KEY_DEVICE_UUID)) {
                    deviceUUID = UUID.randomUUID();
                    deviceUUIDString = deviceUUID.toString();
                    newDeviceUUID = true;
                } else {
                    deviceUUIDString = preferences.getString(KEY_DEVICE_UUID, DEFAULT_UUID);
                    deviceUUID = UUID.fromString(deviceUUIDString);
                }
            }
        }

        if (newDeviceUUID) {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(KEY_DEVICE_UUID, deviceUUIDString);
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

    public String getCurrentUsername(){
        return getSharedPreferences().getString(KEY_USERNAME, DEFAULT_USERNAME);
    }

    public int getCurrentPort(){
        return getSharedPreferences().getInt(KEY_PORT, DEFAULT_PORT);
    }

    public int getCurrentPreload(){
        return getSharedPreferences().getInt(KEY_PRELOAD, DEFAULT_PRELOAD);
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
