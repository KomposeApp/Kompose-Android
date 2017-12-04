package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;

/** Stores all relevant information for the active application **/

public class StateSingleton {

    private final String LOG_TAG = "## SINGLETON HUB:";

    private SessionModel activeSession;
    private ClientModel activeClient;
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

    /** Song Cache stuff **/
    private int maxCacheSize = 10;
    private final LinkedHashMap<String, File> songCache = new LinkedHashMap<String, File>(){
        @Override
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            boolean result = (size() > maxCacheSize);
            if (result){
                File file = (File) eldest.getValue();
                Log.d(LOG_TAG, "Evicting file " + file.getName() + " from cache");
                if (!file.delete()){
                    Log.e(LOG_TAG, "Failed to delete file: " + file.getName());
                } else{
                    Log.d(LOG_TAG, "Successfully deleted file");
                }
            }
            return result;
        }

        @Override
        public void clear(){
            for (File f: this.values()){
                if (!f.delete()){
                    Log.e(LOG_TAG, "Failed to delete file: " + f.getAbsolutePath());
                } else{
                    Log.d(LOG_TAG, "Successfully deleted file: " + f.getAbsolutePath());
                }
            }
            super.clear();
        }
    };
    public void addSongToCache(String id, File file){
        Log.d(LOG_TAG, "Added file: " + file.getName() + " - from VideoID: " + id + "- to the cache");
        songCache.put(id, file);
    }
    public File retrieveSongFromCache(String id){
        Log.d(LOG_TAG, "Retrieving file with VideoID: " + id + " from the cache");
        return songCache.get(id);
    }
    public boolean checkCacheByKey(String id){ return songCache.containsKey(id);}
    public boolean checkCacheByValue(File file){ return songCache.containsValue(file);}
    public void clearCache(){songCache.clear();}


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
