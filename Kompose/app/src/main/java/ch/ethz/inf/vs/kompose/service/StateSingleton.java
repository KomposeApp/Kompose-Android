package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.concurrent.Phaser;

import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.preferences.PreferenceUtility;
import ch.ethz.inf.vs.kompose.service.audio.SongCacheMap;

/**
 * Stores all relevant information for the active application
 **/

public class StateSingleton {

    private final String LOG_TAG = "##StateSingleton:";

    private SessionModel activeSession;
    private ClientModel activeClient;
    private SessionModel activeHistorySession;
    private PreferenceUtility preferenceUtility; // Main access point for all preferences
    private boolean hasMainActivity; //Required for the Share Activity
    private boolean playlistIsActive; //Required for the Share Activity
    private Phaser audioServicePhaser;

    private StateSingleton() {
        hasMainActivity = false;
        playlistIsActive = false;
    }

    public Phaser getAudioServicePhaser() {
        return audioServicePhaser;
    }

    public void setAudioServicePhaser(Phaser audioServicePhaser) {
        this.audioServicePhaser = audioServicePhaser;
    }

    private static class LazyHolder {
        static final StateSingleton INSTANCE = new StateSingleton();
    }

    public static StateSingleton getInstance() {
        return LazyHolder.INSTANCE;
    }

    public SessionModel getActiveSession() {
        return activeSession;
    }

    public ClientModel getActiveClient() {
        return activeClient;
    }

    public SessionModel getActiveHistorySession() {
        return activeHistorySession;
    }

    public PreferenceUtility getPreferenceUtility() {
        return preferenceUtility;
    }

    public void setActiveSession(SessionModel session) {
        this.activeSession = session;
    }

    public void setActiveClient(ClientModel client) {
        this.activeClient = client;
    }

    public void setActiveHistorySession(SessionModel historySession) {
        this.activeHistorySession = historySession;
    }

    public void setPreferenceUtility(Context ctx) {
        preferenceUtility = new PreferenceUtility(ctx);
    }

    public boolean isStartedFromMainActivity() {
        return hasMainActivity;
    }

    public void setStartedFromMainActivity() {
        hasMainActivity = true;
    }

    public boolean getPlaylistIsActive() {
        return playlistIsActive;
    }

    public void setPlaylistIsActive(boolean value) {
        playlistIsActive = value;
    }

}
