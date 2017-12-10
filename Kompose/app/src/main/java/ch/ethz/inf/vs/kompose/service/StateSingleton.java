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

    /**
     * Song Cache stuff
     **/

    //The Song Cache itself (initially null)
    private SongCacheMap songCache;

    /**
     * Set up a new empty song cache, and clears the old one.
     * Cache size is the maximum of preload size and desired cache size.
     *
     * @param preloadSize Number of songs we'd like to preload
     * @param cacheSize   Number of songs we want to cache
     */
    public void initializeSongCache(int preloadSize, int cacheSize) {
        if (songCache != null) songCache.clear();
        songCache = new SongCacheMap(Math.max(preloadSize + 1, cacheSize));
        Log.d("## SongCacheMap", "Song cache initialized.");
    }

    /**
     * Add a song to the cache, the identifier being the Youtube VideoID
     *
     * @param id   VideoID that uniquely identifies the song
     * @param file File that points to the Song in the hardware cache
     */
    public void addSongToCache(String id, File file) {
        if (songCache == null){
            throw new IllegalStateException("Cache has not been initialized.");
        }
        Log.d("## SongCacheMap", "Added file: " + file.getName() + " - from VideoID: " + id + "- to the cache");
        songCache.put(id, file);
    }

    /**
     * Retrieve a previously cached song by its VideoID
     *
     * @param id VideoID of the Youtube video that corresponds to the song
     * @return File descriptor of the song in cache
     */
    public File retrieveSongFromCache(String id) {
        if (songCache == null){
            throw new IllegalStateException("Cache has not been initialized.");
        }
        Log.d("##SongCacheMap", "Retrieving file with VideoID: " + id + " from the cache");
        return songCache.get(id);
    }


    public boolean checkCacheByKey(String id) {
        if (songCache == null){
            throw new IllegalStateException("Cache has not been initialized.");
        }
        return songCache.containsKey(id);
    }

    public boolean checkCacheByValue(File file) {
        if (songCache == null){
            throw new IllegalStateException("Cache has not been initialized.");
        }
        return songCache.containsValue(file);
    }

    public void clearCache() {
        if (songCache!= null) songCache.clear();
        Log.d("##SongCacheMap", "Cache cleared");
    }


}
