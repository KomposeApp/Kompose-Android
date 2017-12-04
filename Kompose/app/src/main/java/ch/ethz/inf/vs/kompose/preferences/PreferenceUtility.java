package ch.ethz.inf.vs.kompose.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class PreferenceUtility {

    private static final String LOG_TAG = "## Preference Repo";

    private static final String PREFERENCE_FILENAME = "kompose.prefs";

    private static final String KEY_UUID = "device_uuid";
    private static final String KEY_PRELOAD = "k_preload";
    private static final String KEY_USERNAME = "k_user";
    private static final String KEY_SESSIONNAME = "k_session";
    private static final String KEY_HPORT = "k_host_port";
    private static final String KEY_CPORT = "k_client_port";

    private static final String DEFAULT_USERNAME = "Komposer";
    private static final String DEFAULT_SESSIONNAME = "Default Session";
    private static final int DEFAULT_PRELOAD = 3;
    private static final int DEFAULT_PORT = 0;

    private SharedPreferences sPrefs;
    private SharedPreferences.Editor sEdits;

    @SuppressLint("CommitPrefEdits")
    public PreferenceUtility(Context ctx){
        this.sPrefs = ctx.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        this.sEdits = sPrefs.edit();
    }

    public void applyChanges(){
        sEdits.apply();
    }

    public void commitChanges(){
        sEdits.commit();
    }

    public void setUsername(String username){
        sEdits.putString(KEY_USERNAME, username);
    }
    public void setSessionName(String sessionName){
        sEdits.putString(KEY_SESSIONNAME, sessionName);
    }
    public void setHostPort(int port){
        sEdits.putInt(KEY_HPORT, port);
    }
    public void setClientPort(int port) {
        sEdits.putInt(KEY_CPORT, port);
    }
    public void setPreload( int preload){
        sEdits.putInt(KEY_PRELOAD, preload);
    }

    public String getUsername(){
        return sPrefs.getString(KEY_USERNAME, DEFAULT_USERNAME);
    }
    public String getSessionName(){
        return sPrefs.getString(KEY_SESSIONNAME, DEFAULT_SESSIONNAME);
    }
    public int getHostPort(){
        return sPrefs.getInt(KEY_HPORT, DEFAULT_PORT);
    }
    public int getClientPort(){
        return sPrefs.getInt(KEY_CPORT, DEFAULT_PORT);
    }
    public int getPreload(){
        return sPrefs.getInt(KEY_PRELOAD, DEFAULT_PRELOAD);
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
