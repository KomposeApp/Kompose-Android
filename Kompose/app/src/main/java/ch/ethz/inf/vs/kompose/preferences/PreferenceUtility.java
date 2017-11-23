package ch.ethz.inf.vs.kompose.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Objects;
import java.util.UUID;

public abstract class PreferenceUtility {

    private static final String LOG_TAG = "## Preference Repo";

    private static final String KEY_DEVICE_UUID = "device_uuid";
    private static final String KEY_PRELOAD = "k_preload";
    private static final String KEY_USERNAME = "k_user";
    private static final String KEY_PORT = "k_port";

    private static final String DEFAULT_UUID = null;
    private static final int DEFAULT_PORT = 0;
    private static final String DEFAULT_USERNAME = "John Doe";
    private static final int DEFAULT_PRELOAD = 3;

    @SuppressLint("ApplySharedPref")
    public static void generateAndSet_DeviceUUID(Context ctx){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putString(KEY_DEVICE_UUID, UUID.randomUUID().toString());
        editor.commit(); //Using commit here to avoid issues with asynchronous code.
    }

    @Nullable
    public static String retrieveDeviceUUIDString(Context ctx){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String uuid_string = sPrefs.getString(KEY_DEVICE_UUID, DEFAULT_UUID);

        if (Objects.equals(uuid_string, DEFAULT_UUID)){
            Log.e(LOG_TAG,"ALERT: DEVICE UUID NOT SET");
            return null;
        }
        else {
            return uuid_string;
        }
    }

    @Nullable
    public static UUID retrieveDeviceUUID(Context ctx){
        return UUID.fromString(retrieveDeviceUUIDString(ctx));
    }

    public static void setCurrentUsername(Context ctx, String username){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public static String getCurrentUsername(Context ctx){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sPrefs.getString(KEY_USERNAME, DEFAULT_USERNAME);
    }

    public static void setCurrentPort(Context ctx, int port){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putInt(KEY_PORT, port);
        editor.apply();
    }

    public static int getCurrentPort(Context ctx){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sPrefs.getInt(KEY_PORT, DEFAULT_PORT);
    }

    public static void setCurrentPreload(Context ctx, int preload){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sPrefs.edit();

        editor.putInt(KEY_PRELOAD, preload);
        editor.apply();
    }

    public static int getCurrentPreload(Context ctx){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sPrefs.getInt(KEY_PRELOAD, DEFAULT_PRELOAD);
    }

}
