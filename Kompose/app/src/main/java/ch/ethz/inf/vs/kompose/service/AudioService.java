package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Intent;
import android.databinding.ObservableList;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;

public class AudioService extends Service {

    private static final String LOG_TAG = "## AudioService";
    private final IBinder binder = new LocalBinder();

    private SessionModel sessionModel;
    private ObservableList<SongModel> songs;

    private boolean initialized = false;
    private MediaPlayer mediaPlayer;
    private int numSongsPreload;
    private int numCached = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        this.numSongsPreload = PreferenceUtility.getCurrentPreload(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // register observer on the song list
        if (initialized) {
            songs.addOnListChangedCallback(new PlaylistListner());
        } else {
            Log.e(LOG_TAG, "was not initialized");
        }

        return START_STICKY;
    }

    public void initializeAudioService(SessionModel sessionModel) {
        this.initialized = true;
        this.sessionModel = sessionModel;
        this.songs = sessionModel.getSongs();
    }

    public void startPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNext();
                    mp.release();
                }
            });
            mediaPlayer.start();
        }
    }

    public void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    // TODO
    public void playNext() {
        mediaPlayer = null;
    }

    private MediaPlayer mediaPlayerFromFile(File file) {
        return mediaPlayer = MediaPlayer.create(this, Uri.fromFile(file));
    }

    // TODO
    private void updateCache() {
    }

    public class LocalBinder extends Binder {
        AudioService getService() {
            return AudioService.this;
        }
    }

    // MediaPlayer

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class PlaylistListner extends ObservableList.OnListChangedCallback {
        @Override
        public void onChanged(ObservableList observableList) {
        }

        @Override
        public void onItemRangeChanged(ObservableList observableList, int i, int i1) {
        }

        @Override
        public void onItemRangeInserted(ObservableList observableList, int i, int i1) {
        }

        @Override
        public void onItemRangeMoved(ObservableList observableList, int i, int i1, int i2) {
        }

        @Override
        public void onItemRangeRemoved(ObservableList observableList, int i, int i1) {
        }
    }
}
