package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableList;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;

import ch.ethz.inf.vs.kompose.ConnectActivity;
import ch.ethz.inf.vs.kompose.PlaylistActivity;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.model.comparators.SongComparator;
import ch.ethz.inf.vs.kompose.model.comparators.UniqueModelComparator;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;
import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;

public class AudioService extends Service {

    private static final String LOG_TAG = "## AudioService";
    private final IBinder binder = new LocalBinder();

    private SessionModel sessionModel;
    //private ObservableUniqueSortedList<SongModel> playQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "started");
        sessionModel = StateSingleton.getInstance().activeSession;
        ObservableUniqueSortedList<SongModel> playQueue
                = (ObservableUniqueSortedList<SongModel>) sessionModel.getPlayQueue();

        // start the download worker
        DownloadWorker downloadWorker = new DownloadWorker(this, playQueue);
        downloadWorker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void togglePlayPause() {
        SongModel currentSong = sessionModel.getCurrentlyPlaying();
        MediaPlayer mediaPlayer = currentSong.getMediaPlayer();
        if (sessionModel.getSessionStatus() == SessionStatus.PLAYING && mediaPlayer != null) {
            mediaPlayer.pause();
        } else if (sessionModel.getSessionStatus() == SessionStatus.PAUSED && mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void startCurrentSong() {
        final SongModel currentSong = sessionModel.getCurrentlyPlaying();
        // TODO null check
        if (currentSong.getSongStatus() == SongStatus.IN_QUEUE
                && currentSong.getMediaPlayer() != null
                && currentSong.getDownloadStatus() != DownloadStatus.FINISHED) {

            currentSong.setSongStatus(SongStatus.PLAYING);
            MediaPlayer mediaPlayer = currentSong.getMediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    currentSong.setMediaPlayer(null);
                    currentSong.setSongStatus(SongStatus.PLAYED);
                    updateCurrentSong();
                    startCurrentSong();
                }
            });
            mediaPlayer.start();
            sessionModel.setSessionStatus(SessionStatus.PLAYING);
        }
    }

    private void updateCurrentSong() {
        Log.d(LOG_TAG, "updating currently playing song");
        SongModel currentSong = sessionModel.getCurrentlyPlaying();

        Log.d(LOG_TAG, "play queue size = " + sessionModel.getPlayQueue().size()); // tmp

        // currently playing song needs to be newly set
        if (currentSong == null || currentSong.getSongStatus() != SongStatus.PLAYING) {

            // current song was previously set
            if (currentSong != null) {

                // cleanup old MediaPlayer
                if (currentSong.getMediaPlayer() != null) {
                    MediaPlayer mediaPlayer = currentSong.getMediaPlayer();
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    currentSong.setMediaPlayer(null);
                }

                currentSong.setSongStatus(SongStatus.PLAYED);
            }

            Log.d(LOG_TAG, "play queue size = " + sessionModel.getPlayQueue().size()); // tmp

            // get the next song from the play queue and set it as the currently playing one
            if (sessionModel.getPlayQueue().size() > 0) {
                SongModel nextSong = sessionModel.getPlayQueue().get(0);
                nextSong.setSongStatus(SongStatus.PLAYING);
                sessionModel.setCurrentlyPlaying(nextSong);
                Log.d(LOG_TAG, "currently playing song: "
                        + sessionModel.getCurrentlyPlaying().getTitle());
            } else {
                sessionModel.setCurrentlyPlaying(null);
            }

            // notify clients about the currently playing song change
            new OutgoingMessageHandler().updateAllClients(sessionModel);
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

        private Phaser notifier;
        AudioService audioService;

        PlaylistListener(Phaser notifier, AudioService audioService) {
            this.notifier = notifier;
            this.audioService = audioService;
        }

        @Override
        public void onChanged(ObservableList observableList) { }

        @Override
        public void onItemRangeChanged(ObservableList observableList, int i, int i1) { }

        @Override
        public void onItemRangeInserted(ObservableList observableList, int i, int i1) {
            Log.d(LOG_TAG, (i1-i) + " new items in play queue");
            audioService.updateCurrentSong();
            notifier.register();
        }

        @Override
        public void onItemRangeMoved(ObservableList observableList, int i, int i1, int i2) { }

        @Override
        public void onItemRangeRemoved(ObservableList observableList, int i, int i1) {
            Log.d(LOG_TAG, (i1-i) + " items removed from play queue");
            notifier.register();
        }
    }

    private static class DownloadWorker extends AsyncTask<Void,Void,Void> {

        private Phaser notifier;
        private int numSongsPreload;
        private WeakReference<AudioService> context;
        private ObservableUniqueSortedList<SongModel> playQueue;

        DownloadWorker(AudioService context, ObservableUniqueSortedList<SongModel> playQueue) {

            this.context = new WeakReference<AudioService>(context);
            this.playQueue = playQueue;

            this.numSongsPreload = PreferenceUtility.getCurrentPreload(context);
            this.notifier = new Phaser(1);
            playQueue.addOnListChangedCallback(new PlaylistListener(notifier, context));
        }

        private MediaPlayer mediaPlayerFromFile(File file) {
            return MediaPlayer.create(context.get(), Uri.fromFile(file));
        }

        private int getNumDownloaded() {
            int n = 0;
            for (SongModel s : playQueue) {
                if (s.getDownloadStatus() == DownloadStatus.FINISHED) {
                    n++;
                }
            }
            return n;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            YoutubeDownloadUtility youtubeDownloadUtility = new YoutubeDownloadUtility(context.get());

            while (!isCancelled()) {
                // wait until the Phaser is unblocked (initially and when a new item enters
                // the download queue)
                notifier.arriveAndDeregister();

                int numDownloaded = getNumDownloaded();

                while (numDownloaded < numSongsPreload && playQueue.size() > 0) {
                    SongModel nextDownload = playQueue.remove(0);
                    Log.d(LOG_TAG, "Downloading: " + nextDownload.getTitle());

                    File storedFile = youtubeDownloadUtility.downloadSong(
                            nextDownload.getDownloadUrl().toString(),
                            nextDownload.getTitle() + ".m4a");

                    if (storedFile != null) {
                        nextDownload.setDownloadPath(storedFile);
                        nextDownload.setDownloadStatus(DownloadStatus.FINISHED);
                        nextDownload.setMediaPlayer(mediaPlayerFromFile(storedFile));
                        context.get().startCurrentSong();
                    } else {
                        nextDownload.setDownloadStatus(DownloadStatus.FAILED);
                    }
                }
            }
            return null;
        }
    }
}
