package ch.ethz.inf.vs.kompose.service.audio;

import android.app.Service;
import android.content.Intent;
import android.databinding.Observable;
import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.youtube.YoutubeDownloadUtility;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;

public class AudioService extends Service{

    private static final String LOG_TAG = "## AudioService";
    private final IBinder binder = new LocalBinder();

    private SessionModel sessionModel;
    private DownloadWorker downloadWorker;
    private SongModel connectedSongModel;

    //Callback listener for when one of the songs change
    private Observable.OnPropertyChangedCallback songModelCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable observable, int i) {
            if (i == BR.songStatus) {
                SongModel songModel = (SongModel) observable;
                if (songModel.getSongStatus().equals(SongStatus.SKIPPED_BY_POPULAR_VOTE)) {
                    goToNextSong(connectedSongModel);
                }
            }
        }
    };

    // Callback listener for when the current session model changes
    private Observable.OnPropertyChangedCallback sessionModelCallback = new Observable.OnPropertyChangedCallback() {

        @Override
        public void onPropertyChanged(Observable observable, int i) {
            if (i == BR.currentlyPlaying) {
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

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "started");
        sessionModel = StateSingleton.getInstance().getActiveSession();
        sessionModel.addOnPropertyChangedCallback(sessionModelCallback);

        // start the download worker
        downloadWorker = new DownloadWorker(this, sessionModel);
        downloadWorker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (sessionModel != null) {
            sessionModel.removeOnPropertyChangedCallback(sessionModelCallback);
            MediaPlayer mp = sessionModel.getCurrentlyPlaying().getMediaPlayer();
            if (mp!=null) mp.release();
            sessionModel.setCurrentlyPlaying(null);
        }
        if (connectedSongModel!= null) connectedSongModel.removeOnPropertyChangedCallback(songModelCallback);
        if (downloadWorker != null) {
            downloadWorker.cancel(true);
        }

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
            //TODO: Why would the client care?
            new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();
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
            //TODO: Why would the client care?
            new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();
        }

    }

    private void checkOnCurrentSong() {
        synchronized (StateSingleton.getInstance()) {
            if (sessionModel.getCurrentlyPlaying() == null) {
                goToNextSong(null);
            }
        }
    }

    /**
     * Advance to the next song in the playlist.
     * @param workaroundSong Parameter for when the current song is downvoted.
     *                  For some weird reason, the current song in the sessionmodel
     *                  is set to null when that happens.
     */
    private void goToNextSong(final SongModel workaroundSong) {

        // Apparently we need to run this on the main thread because Observable Objects
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                synchronized (StateSingleton.getInstance()) {
                    Log.d(LOG_TAG, "updating currently playing song");
                    SongModel songToStop;

                    // If previous song is specified as an argument, stop that one.
                    if (workaroundSong != null) {
                        songToStop = workaroundSong;
                    } else{
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
                                sessionModel.getPastSongs().add(songModel);
                                songModel.setSongStatus(SongStatus.SKIPPED_BY_ERROR);
                            } else {
                                //else wait for download to finish
                                break;
                            }
                        }
                        if (chosenSong != null && chosenSong.getMediaPlayer() != null) {
                            //When playing a new song, remove it from the ObservableList
                            sessionModel.getPlayQueue().remove(chosenSong);

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

                    // TODO: restrict to cases where it's actually necessary
                    new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();
                }
            }
        });
    }


    private class PlaybackProgressObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);
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
                    final int position = mediaPlayer.getCurrentPosition();
                    final int duration = mediaPlayer.getDuration();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Double d = (double) position / duration * 100;
                            songModel.setPlaybackProgress(d.intValue());
                        }
                    });
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
    
    public class LocalBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private static class PlaylistListener extends ObservableList.OnListChangedCallback {

        private final String LOG_TAG = "## PlaylistListener";
        AudioService audioService;

        PlaylistListener(AudioService audioService) {
            this.audioService = audioService;
        }

        @Override
        public void onChanged(ObservableList observableList) {
        }

        @Override
        public void onItemRangeChanged(ObservableList observableList, int i, int i1) {
        }

        @Override
        public void onItemRangeInserted(ObservableList observableList, int i, int i1) {
            Log.d(LOG_TAG, i1 + " new items in play queue");
            audioService.checkOnCurrentSong();
            try {
                StateSingleton.getInstance().getAudioServicePhaser().register();
            } catch (Exception e) {
                //whatever
            }
        }

        @Override
        public void onItemRangeMoved(ObservableList observableList, int i, int i1, int i2) {
        }

        @Override
        public void onItemRangeRemoved(ObservableList observableList, int i, int i1) {
            Log.d(LOG_TAG, i1 + " items removed from play queue");
            try {
                StateSingleton.getInstance().getAudioServicePhaser().register();
            } catch (Exception e) {
                //whatever
            }
        }
    }

    private static class DownloadWorker extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "## DownloadWorker";
        private int numSongsPreload;
        private WeakReference<AudioService> context;
        private SessionModel sessionModel;

        DownloadWorker(AudioService context, SessionModel sessionModel) {
            this.context = new WeakReference<>(context);
            this.sessionModel = sessionModel;

            this.numSongsPreload = StateSingleton.getInstance().getPreferenceUtility().getPreload();
            StateSingleton.getInstance().setAudioServicePhaser(new Phaser(1));
            sessionModel.getPlayQueue().addOnListChangedCallback(new PlaylistListener(context));
        }

        private MediaPlayer mediaPlayerFromFile(File file) {
            return MediaPlayer.create(context.get(), Uri.fromFile(file));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            YoutubeDownloadUtility youtubeDownloadUtility = new YoutubeDownloadUtility(context.get());

            while (!isCancelled()) {
                // wait until the Phaser is unblocked (initially and when a new item enters
                // the download queue)
                // TODO: lock is fucked
                int registered = StateSingleton.getInstance().getAudioServicePhaser().getRegisteredParties();
                StateSingleton.getInstance().getAudioServicePhaser().arriveAndDeregister();

                int numDownloaded = 0;
                int index = 0;

                while (numDownloaded < numSongsPreload && index < sessionModel.getPlayQueue().size()) {
                    try {
                        final SongModel nextDownload = sessionModel.getPlayQueue().get(index);
                        if (!nextDownload.getSongStatus().equals(SongStatus.RESOLVING) && nextDownload.getDownloadStatus() == DownloadStatus.NOT_STARTED) {
                            Log.d(LOG_TAG, "Downloading: " + nextDownload.getTitle());

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    nextDownload.setDownloadStatus(DownloadStatus.STARTED);
                                }
                            });

                            final File storedFile = youtubeDownloadUtility.downloadSong(nextDownload);
                            final Drawable thumbDrawable = youtubeDownloadUtility.downloadThumb(nextDownload);

                            if (storedFile != null) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        nextDownload.setDownloadPath(storedFile);
                                        nextDownload.setDownloadStatus(DownloadStatus.FINISHED);
                                        nextDownload.setMediaPlayer(mediaPlayerFromFile(storedFile));
                                        if (thumbDrawable != null) {
                                            Log.d(LOG_TAG, "thumbnail downloaded " + thumbDrawable.isVisible());
                                            nextDownload.setThumbnail(thumbDrawable);
                                        }

                                        context.get().checkOnCurrentSong();
                                    }
                                });
                                numDownloaded++;

                            } else {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        nextDownload.setDownloadStatus(DownloadStatus.FAILED);
                                    }
                                });
                            }

                            new OutgoingMessageHandler(context.get()).sendSessionUpdate();
                            index = 0;
                        } else {
                            numDownloaded++;
                            index++;
                        }
                    } catch (Exception ex) {
                        //error occurs when playlist is empty because .size() is retarded (return > 0 even though its 0)
                    }
                }
            }

            return null;
        }
    }
}
