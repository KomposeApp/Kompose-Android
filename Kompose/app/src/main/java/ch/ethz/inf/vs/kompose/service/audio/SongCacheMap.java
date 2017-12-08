package ch.ethz.inf.vs.kompose.service.audio;

import android.util.Log;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;


public class SongCacheMap extends LinkedHashMap<String, File> {

    private final String LOG_TAG = "## SongCacheMap";
    private int maxCacheSize;

    public SongCacheMap(int cacheSize){
        this.maxCacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry eldest) {
        boolean result = (size() > maxCacheSize);
        if (result) {
            File file = (File) eldest.getValue();
            Log.d(LOG_TAG, "Evicting file " + file.getName() + " from cache");
            if (!file.delete()) {
                Log.e(LOG_TAG, "Failed to delete file: " + file.getName());
            } else {
                Log.d(LOG_TAG, "Successfully deleted file: " + file.getName());
            }
        }
        return result;
    }

    @Override
    public void clear() {
        for (File f : this.values()) {
            if (!f.delete()) {
                Log.e(LOG_TAG, "Failed to delete file: " + f.getAbsolutePath());
            } else {
                Log.d(LOG_TAG, "Successfully deleted file: " + f.getAbsolutePath());
            }
        }
        super.clear();
    }
}
