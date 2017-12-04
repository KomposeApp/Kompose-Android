package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableList;
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
import java.util.concurrent.Phaser;

import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;
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

        // start the download worker
        downloadWorker = new DownloadWorker(this, sessionModel);
        downloadWorker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

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
        if (sessionModel.getCurrentlyPlaying() == null) {
            goToNextSong();
        }
    }

    private void goToNextSong() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "updating currently playing song");
                if (sessionModel.getCurrentlyPlaying() != null) {
                    MediaPlayer mp = sessionModel.getCurrentlyPlaying().getMediaPlayer();
                    mp.stop();
                    mp.release();

                    SongModel songModel = sessionModel.getCurrentlyPlaying();
                    songModel.setSongStatus(SongStatus.PLAYED);
                    sessionModel.getCurrentlyPlaying().setMediaPlayer(null);

                    sessionModel.setCurrentlyPlaying(null);
                    Log.d(LOG_TAG, "stopping & removing current song");
                }


                if (sessionModel.getPlayQueue().size() > 0) {
                    SongModel chosenSong = null;
                    for (SongModel songModel : sessionModel.getPlayQueue()) {
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
                                goToNextSong();
                            }
                        });
                        mediaPlayer.start();

                        Log.d(LOG_TAG, "now playing song: " + sessionModel.getCurrentlyPlaying().getTitle());
                    }
                }

                new OutgoingMessageHandler(getBaseContext()).sendSessionUpdate();
            }
        });
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

        private Phaser notifier;
        AudioService audioService;

        PlaylistListener(Phaser notifier, AudioService audioService) {
            this.notifier = notifier;
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
            Log.d(LOG_TAG, (i1 - i) + " new items in play queue");
            audioService.checkOnCurrentSong();
            notifier.register();
        }

        @Override
        public void onItemRangeMoved(ObservableList observableList, int i, int i1, int i2) {
        }

        @Override
        public void onItemRangeRemoved(ObservableList observableList, int i, int i1) {
            Log.d(LOG_TAG, (i1 - i) + " items removed from play queue");
            notifier.register();
        }
    }

    private static class DownloadWorker extends AsyncTask<Void, Void, Void> {

        private Phaser notifier;
        private int numSongsPreload;
        private WeakReference<AudioService> context;
        private SessionModel sessionModel;

        DownloadWorker(AudioService context, SessionModel sessionModel) {
            this.context = new WeakReference<AudioService>(context);
            this.sessionModel = sessionModel;

            this.numSongsPreload = StateSingleton.getInstance().getPreferenceUtility().getCurrentPreload();
            this.notifier = new Phaser(1);
            sessionModel.getPlayQueue().addOnListChangedCallback(new PlaylistListener(notifier, context));
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
                notifier.arriveAndDeregister();

                int numDownloaded = 0;

                //todo:
                //make error handling better; and allow parallel downloads
                //simply create a thread for each song, instead of this weird loop; helps to better account for failures
                int index = 0;
                while (numDownloaded < numSongsPreload && index < sessionModel.getPlayQueue().size()) {
                    final SongModel nextDownload = sessionModel.getPlayQueue().get(index++);
                    if (nextDownload.getDownloadStatus() == DownloadStatus.NOT_STARTED) {
                        Log.d(LOG_TAG, "Downloading: " + nextDownload.getTitle());

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                nextDownload.setDownloadStatus(DownloadStatus.STARTED);
                            }
                        });


                        final File storedFile = youtubeDownloadUtility.downloadSong(
                                nextDownload.getDownloadUrl().toString(),
                                nextDownload.getTitle() + ".m4a");

                        if (storedFile != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    nextDownload.setDownloadPath(storedFile);
                                    nextDownload.setDownloadStatus(DownloadStatus.FINISHED);
                                    nextDownload.setMediaPlayer(mediaPlayerFromFile(storedFile));

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
                    } else {
                        numDownloaded++;
                    }
                }
            }
            return null;
        }
    }
}
