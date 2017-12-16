package ch.ethz.inf.vs.kompose.service.audio;

import android.app.Service;
import android.content.Intent;
import android.databinding.Observable;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;

public class AudioService extends Service{

    private static final String LOG_TAG = "##AudioService";

    private SessionModel sessionModel;
    private Thread downloadWorkerThread;
    private SongModel connectedSongModel;

    //Callback listener for when one of the songs change
    private Observable.OnPropertyChangedCallback songModelCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable observable, int i) {
            if (i == BR.songStatus) {
                SongStatus status = ((SongModel) observable).getSongStatus();
                if (status.equals(SongStatus.SKIPPED)) {
                    DownloadStatus dlstatus = connectedSongModel.getDownloadStatus();
                    if (dlstatus.equals(DownloadStatus.STARTED)){
                        connectedSongModel.setDownloadStatus(DownloadStatus.FAILED);
                    }
                    goToNextSong(connectedSongModel);
                }
            }
        }
    };

    // Callback listener for when the current session model changes
    private Observable.OnPropertyChangedCallback sessionModelCallback = new Observable.OnPropertyChangedCallback() {

        @Override
        public void onPropertyChanged(Observable observable, int index) {
            if (index == BR.currentlyPlaying) {
                SessionModel sessionModel = (SessionModel) observable;
                if (connectedSongModel != sessionModel.getCurrentlyPlaying()) {
                    if (connectedSongModel != null) {
                        connectedSongModel.removeOnPropertyChangedCallback(songModelCallback);
                        connectedSongModel = null;
                    }
                    if (sessionModel.getCurrentlyPlaying() != null) {
                        connectedSongModel = sessionModel.getCurrentlyPlaying();
                        sessionModel.getCurrentlyPlaying().addOnPropertyChangedCallback(songModelCallback);
                    }
                }
            }
        }
    };

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "bound");
        sessionModel = StateSingleton.getInstance().getActiveSession();
        sessionModel.addOnPropertyChangedCallback(sessionModelCallback);

        return binder;
    }


    public void startDownloadWorker() {
        downloadWorkerThread = new Thread(new DownloadWorker(this, sessionModel));
        downloadWorkerThread.start();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "Unbound Service");

        //Cleanup
        if (sessionModel != null) {
            Log.d(LOG_TAG, "Removing callback from sessionmodel");
            sessionModel.removeOnPropertyChangedCallback(sessionModelCallback);
            if (sessionModel.getCurrentlyPlaying() != null) {
                Log.d(LOG_TAG, "Stopping playback");
                MediaPlayer mp = sessionModel.getCurrentlyPlaying().getMediaPlayer();
                if (mp != null) mp.release();
                sessionModel.setCurrentlyPlaying(null);
            }
        }

        if (connectedSongModel!= null) {
            Log.d(LOG_TAG, "Removing callback from songmodel");
            connectedSongModel.removeOnPropertyChangedCallback(songModelCallback);
        }
        if (downloadWorkerThread != null) {
            Log.d(LOG_TAG, "Attempting to interrupt downloadWorker");
            downloadWorkerThread.interrupt();
        }
        return false;
    }

    /**
     * Pause the Song when in "playing" state
     * Runs on main thread through events from the Activity.
     */
    public void pausePlaying() {
        SongModel currentSong = sessionModel.getCurrentlyPlaying();
        MediaPlayer mediaPlayer = currentSong.getMediaPlayer();

        if (currentSong.getSongStatus().equals(SongStatus.PLAYING)) {
            mediaPlayer.pause();
            currentSong.setSongStatus(SongStatus.PAUSED);
            //new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();
        }
    }

    /**
     * Resume the Song when in "paused" state.
     * Runs on main thread through events from the Activity.
     */
    public void startPlaying() {
        SongModel currentSong = sessionModel.getCurrentlyPlaying();
        MediaPlayer mediaPlayer = currentSong.getMediaPlayer();

        if (currentSong.getSongStatus().equals(SongStatus.PAUSED)) {
            mediaPlayer.start();
            currentSong.setSongStatus(SongStatus.PLAYING);
            //new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();
        }

    }

    /**
     * Check if there is currently no song playing. If so, advance to the next one.
     */
    public void checkOnCurrentSong() {
        if (sessionModel.getCurrentlyPlaying() == null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    goToNextSong(null);
                }
            });
        }
    }

    /**
     * Advance to the next song in the playlist.
     * @param workaroundSong Parameter for when the current song is downvoted.
     */
    private void goToNextSong(final SongModel workaroundSong) {
        Log.d(LOG_TAG, "updating currently playing song");
        SongModel songToStop;

        // If previous song is specified as an argument, stop that one.
        if (workaroundSong != null) {
            songToStop = workaroundSong;
        } else {
            songToStop = sessionModel.getCurrentlyPlaying();
        }

        // Cleanup of the song that was previously playing
        if (songToStop != null) {
            MediaPlayer mp = songToStop.getMediaPlayer();
            mp.stop();
            mp.release();

            songToStop.setMediaPlayer(null);
        }

        // Flag the current song as finished and remove it
        SongModel currentSong = sessionModel.getCurrentlyPlaying();
        if (currentSong != null) {
            currentSong.setSongStatus(SongStatus.FINISHED);

            sessionModel.setCurrentlyPlaying(null);
            Log.d(LOG_TAG, "stopping & removing current song");
        }

        // Look for a new song in the queue
        synchronized (sessionModel.getPlayQueue()) {
            if (sessionModel.getPlayQueue().size() > 0) {
                SongModel chosenSong = null;

                // Loop over all the songs in the queue to remove ones where the download has
                // unexpectedly stopped with an error. On finding the first working song, break out.
                for (SongModel songModel : new ArrayList<>(sessionModel.getPlayQueue())) {
                    if (songModel.getDownloadStatus() == DownloadStatus.FINISHED) {
                        chosenSong = songModel;
                        break;
                    } else if (songModel.getDownloadStatus().equals(DownloadStatus.FAILED)) {
                        //directly to trash

                        sessionModel.getPlayQueue().remove(songModel);
                        synchronized (sessionModel.getDownloadedQueue()) {
                            sessionModel.getDownloadedQueue().remove(songModel);
                            sessionModel.getDownloadedQueue().notify();
                        }

                        songModel.setSongStatus(SongStatus.SKIPPED);
                    } else {
                        //else wait for download to finish
                        break;
                    }
                }
                if (chosenSong != null && chosenSong.getMediaPlayer() != null) {
                    //When playing a new song, remove it from the ObservableList

                    sessionModel.getPlayQueue().remove(chosenSong);

                    synchronized (sessionModel.getDownloadedQueue()) {
                        sessionModel.getDownloadedQueue().remove(chosenSong);
                        sessionModel.getDownloadedQueue().notify();
                    }

                    chosenSong.setSongStatus(SongStatus.PLAYING);
                    sessionModel.setCurrentlyPlaying(chosenSong);

                    MediaPlayer mediaPlayer = chosenSong.getMediaPlayer();
                    // Note: Error listener is redundant, as it will call the following Listener as well
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            goToNextSong(null);
                        }
                    });
                    mediaPlayer.start();

                    PlaybackProgressObserver observer = new PlaybackProgressObserver(chosenSong, mediaPlayer);
                    new Thread(observer).start();

                    Log.d(LOG_TAG, "now playing song: " + sessionModel.getCurrentlyPlaying().getTitle());
                }
            }
        }

        new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();

    }


    private class PlaybackProgressObserver implements Runnable {
        private SongModel songModel;
        private MediaPlayer mediaPlayer;

        PlaybackProgressObserver(SongModel songModel, MediaPlayer mediaPlayer) {
            this.songModel = songModel;
            this.mediaPlayer = mediaPlayer;
        }

        @Override
        public void run() {
            while (songModel.getSongStatus().equals(SongStatus.PAUSED) || songModel.getSongStatus().equals(SongStatus.PLAYING)) {
                try {
                    int position = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    Double d = (double) position / duration * 100;
                    songModel.setPlaybackProgress(d.intValue());

                }catch(IllegalStateException e){
                    Log.d(LOG_TAG, "MediaPlayer no longer available, stopping progress tracker...");
                    return;
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.w(LOG_TAG, "Media progress sleeper was interrupted");
                }
            }
        }
    }


}
