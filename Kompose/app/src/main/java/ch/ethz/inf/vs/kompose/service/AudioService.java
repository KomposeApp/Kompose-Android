package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Context;
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
import android.util.StateSet;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;

public class AudioService extends Service {

    private static final String LOG_TAG = "## AudioService";
    private final IBinder binder = new LocalBinder();

    private SessionModel sessionModel;
    private DownloadWorker downloadWorker;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "started");
        sessionModel = StateSingleton.getInstance().getActiveSession();
        sessionModel.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {

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
        });

        // start the download worker
        downloadWorker = new DownloadWorker(this, sessionModel);
        downloadWorker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private SongModel connectedSongModel;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (downloadWorker != null) {
            downloadWorker.cancel(true);
        }
    }

    public void stopPlaying() {
        final SongModel currentSong = sessionModel.getCurrentlyPlaying();
        final MediaPlayer mediaPlayer = currentSong.getMediaPlayer();
        if (currentSong.getSongStatus() == SongStatus.PLAYING && mediaPlayer != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mediaPlayer.pause();
                    currentSong.setSongStatus(SongStatus.PAUSED);

                    new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();
                }
            });
        }
    }

    public void startPlaying() {
        if (sessionModel.getIsHost()) {
            final SongModel currentSong = sessionModel.getCurrentlyPlaying();
            final MediaPlayer mediaPlayer = currentSong.getMediaPlayer();
            if (currentSong.getSongStatus() == SongStatus.PAUSED && mediaPlayer != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlayer.start();
                        currentSong.setSongStatus(SongStatus.PLAYING);

                        new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();
                    }
                });
            }
        }
    }

    private void checkOnCurrentSong() {
        synchronized (StateSingleton.getInstance()) {
            if (sessionModel.getCurrentlyPlaying() == null) {
                goToNextSong(null);
            }
        }
    }

    private void goToNextSong(final SongModel localModel) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                synchronized (StateSingleton.getInstance()) {
                    Log.d(LOG_TAG, "updating currently playing song");
                    SongModel songToStop = sessionModel.getCurrentlyPlaying();
                    if (localModel != null) {
                        songToStop = localModel;
                    }

                    if (songToStop != null) {
                        MediaPlayer mp = songToStop.getMediaPlayer();
                        mp.stop();
                        mp.release();

                        songToStop.setMediaPlayer(null);
                    }

                    if (sessionModel.getCurrentlyPlaying() != null) {
                        SongModel songModel = sessionModel.getCurrentlyPlaying();
                        songModel.setSongStatus(SongStatus.PLAYED);

                        sessionModel.setCurrentlyPlaying(null);
                        Log.d(LOG_TAG, "stopping & removing current song");
                    }

                    if (sessionModel.getPlayQueue().size() > 0) {
                        SongModel chosenSong = null;

                        for (SongModel songModel : new ArrayList<>(sessionModel.getPlayQueue())) {
                            if (songModel.getDownloadStatus() == DownloadStatus.FINISHED) {
                                chosenSong = songModel;
                                break;
                            } else if (songModel.getDownloadStatus().equals(DownloadStatus.FAILED)) {
                                //directly to trash :P
                                sessionModel.getPlayQueue().remove(songModel);
                                sessionModel.getPastSongs().add(songModel);
                                songModel.setSongStatus(SongStatus.SKIPPED_BY_ERROR);
                            } else {
                                //else wait for download to finish
                                break;
                            }
                        }
                        if (chosenSong != null && chosenSong.getMediaPlayer() != null) {
                            sessionModel.getPlayQueue().remove(chosenSong);

                            chosenSong.setSongStatus(SongStatus.PLAYING);
                            sessionModel.setCurrentlyPlaying(chosenSong);

                            MediaPlayer mediaPlayer = chosenSong.getMediaPlayer();
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
            while (true) {
                if (!songModel.getSongStatus().equals(SongStatus.PAUSED) && !songModel.getSongStatus().equals(SongStatus.PLAYING)) {
                    break;
                }

                final int position = mediaPlayer.getCurrentPosition();
                final int duration = mediaPlayer.getDuration();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Double dounb = (double) position / duration * 100;
                        songModel.setPlaybackProgress(dounb.intValue());
                    }
                });
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
                int registered = StateSingleton.getInstance().getAudioServicePhaser().getRegisteredParties();
                StateSingleton.getInstance().getAudioServicePhaser().arriveAndDeregister();

                int numDownloaded = 0;

                int index = 0;
                while (numDownloaded <= numSongsPreload && index < sessionModel.getPlayQueue().size()) {
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
