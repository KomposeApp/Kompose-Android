package ch.ethz.inf.vs.kompose.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class PreferenceUtility {

    private static final String LOG_TAG = "## Preference Repo";

    private static final String KEY_PRELOAD = "k_preload";
    private static final String KEY_USERNAME = "k_user";
    private static final String KEY_PORT = "k_port";

    private static final int DEFAULT_PORT = 0;
    private static final String DEFAULT_USERNAME = "John Doe";
    private static final int DEFAULT_PRELOAD = 3;


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
