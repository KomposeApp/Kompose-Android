package ch.ethz.inf.vs.kompose.service.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

public class PreferenceUtility {

    private final String LOG_TAG = "##Preferences";

    public static final String PREFERENCE_FILENAME = "kompose.prefs";

    public static final String KEY_UUID = "device_uuid";
    public static final String KEY_PRELOAD = "k_preload";
    public static final String KEY_CACHESIZE = "k_cachesize";
    public static final String KEY_MAXDLSIZE = "k_maxdlsize";
    public static final String KEY_USERNAME = "k_user";
    public static final String KEY_SESSIONNAME = "k_session";
    public static final String KEY_HPORT = "k_host_port";
    public static final String KEY_CPORT = "k_client_port";
    public static final String KEY_DEFAULT_IP = "k_default_ip";
    public static final String KEY_DEFAULT_PORT = "k_default_port";

    public static final String DEFAULT_USERNAME = "Komposer";
    public static final String DEFAULT_SESSIONNAME = "Default Session";
    public static final int DEFAULT_PRELOAD = 3;
    public static final int DEFAULT_MAXDLSIZE = 1024;
    public static final int DEFAULT_PORT = 0;

    private SharedPreferences sPrefs;
    private SharedPreferences.Editor sEdits;
    private boolean hasChanged;

    @SuppressLint("CommitPrefEdits")
    public PreferenceUtility(Context ctx) {
        this.sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.sEdits = sPrefs.edit();
    }

    public void applyChanges() {
        sEdits.apply();
        hasChanged = true;
    }

    public void commitChanges() {
        sEdits.commit();
        hasChanged = true;
    }

    public boolean hasChanged() {
        boolean rvalue = hasChanged;
        hasChanged = false;
        return rvalue;
    }

    public void setChanged() {
        hasChanged = true;
    }

    public void setUsername(String username) {
        sEdits.putString(KEY_USERNAME, username);
    }

    public void setSessionName(String sessionName) {
        sEdits.putString(KEY_SESSIONNAME, sessionName);
    }

    public void setHostPort(int port) {
        sEdits.putInt(KEY_HPORT, port);
    }

    public void setClientPort(int port) {
        sEdits.putInt(KEY_CPORT, port);
    }

    public void setPreload(int preload) {
        sEdits.putInt(KEY_PRELOAD, preload);
    }

    public void setCurrentCacheSize(int cacheSize) {
        sEdits.putInt(KEY_CACHESIZE, cacheSize);
    }

    public void setCurrentMaxDLSize(int maxDLsize) { sEdits.putInt(KEY_MAXDLSIZE, maxDLsize);}

    public String getUsername() {
        return sPrefs.getString(KEY_USERNAME, DEFAULT_USERNAME);
    }

    public String getSessionName() {
        return sPrefs.getString(KEY_SESSIONNAME, DEFAULT_SESSIONNAME);
    }

    public int getHostPort() {
        return sPrefs.getInt(KEY_HPORT, DEFAULT_PORT);
    }

    public int getClientPort() {
        return sPrefs.getInt(KEY_CPORT, DEFAULT_PORT);
    }

    public int getPreload() {
        return sPrefs.getInt(KEY_PRELOAD, DEFAULT_PRELOAD);
    }

    public int getCurrentCacheSize() {
        return sPrefs.getInt(KEY_CACHESIZE, DEFAULT_PRELOAD);
    }

    public int getCurrentMaxDLSize() { return sPrefs.getInt(KEY_MAXDLSIZE, DEFAULT_MAXDLSIZE);}

    public void setDefaultIp(String defaultIp) {
        sEdits.putString(KEY_DEFAULT_IP, defaultIp);
    }

    public void setDefaultPort(String port) {
        sEdits.putString(KEY_DEFAULT_PORT, port);
    }

    public String getDefaultPort() {
        return sPrefs.getString(KEY_DEFAULT_PORT, "");
    }

    public String getDefaultIp() {
        return sPrefs.getString(KEY_DEFAULT_IP, "");
    }

    /**
     * Establishes a fixed UUID if not set. Automatically commits as well.
     * Reuse the previous UUID if present.
     */
    public UUID retrieveDeviceUUID() {
        if (!sPrefs.contains(KEY_UUID)) {
            UUID newUUID = UUID.randomUUID();
            sEdits.putString(KEY_UUID, newUUID.toString());
            sEdits.commit();
            return newUUID;
        } else {
            return UUID.fromString(sPrefs.getString(KEY_UUID, null));
        }
    }
}
