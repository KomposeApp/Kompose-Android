package ch.ethz.inf.vs.kompose.service;

import android.content.Context;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;

/** Stores all relevant information for the active application **/

public class StateSingleton {

    private final String LOG_TAG = "## SINGLETON HUB:";

    private SessionModel activeSession;
    private ClientModel activeClient;  //TODO: Do we actually need this?
    private SessionModel activeHistorySession;
    private PreferenceUtility preferenceUtility; // Main access point for all preferences
    private boolean hasMainActivity; //Required for the Share Activity.
    private boolean playlistIsActive;

    private StateSingleton() {
        hasMainActivity = false;
        playlistIsActive = false;
    }

    private static class LazyHolder {
        static final StateSingleton INSTANCE = new StateSingleton();
    }

    public static StateSingleton getInstance() {
        return LazyHolder.INSTANCE;
    }


    public SessionModel getActiveSession(){
        return activeSession;
    }
    public ClientModel getActiveClient(){
        return activeClient;
    }
    public SessionModel getActiveHistorySession(){
        return activeHistorySession;
    }
    public PreferenceUtility getPreferenceUtility(){
        return preferenceUtility;
    }

    public void setActiveSession(SessionModel session){
        this.activeSession = session;
    }
    public void setActiveClient(ClientModel client){
        this.activeClient = client;
    }
    public void setActiveHistorySession(SessionModel historySession){
        this.activeHistorySession = historySession;
    }
    public void setPreferenceUtility(Context ctx){
        preferenceUtility = new PreferenceUtility(ctx);
    }

    public boolean isStartedFromMainActivity(){
        return hasMainActivity;
    }
    public void setStartedFromMainActivity(){
        hasMainActivity = true;
    }
    public boolean getPlaylistIsActive(){
        return playlistIsActive;
    }
    public void setPlaylistIsActive(boolean value){
        playlistIsActive = value;
    }

}
