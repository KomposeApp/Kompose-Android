package ch.ethz.inf.vs.kompose.service.audio;

import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;
import ch.ethz.inf.vs.kompose.service.youtube.YoutubeDownloadUtility;

public class DownloadWorker implements Runnable{

    private final String LOG_TAG = "##DownloadWorker";

    private WeakReference<AudioService> context;
    private SessionModel sessionModel;

    DownloadWorker(AudioService context, SessionModel sessionModel) {
        this.context = new WeakReference<>(context);
        this.sessionModel = sessionModel;

        sessionModel.getPlayQueue().addOnListChangedCallback(new PlaylistListener(context));

    }

    private MediaPlayer mediaPlayerFromFile(File file) {
        return MediaPlayer.create(context.get(), Uri.fromFile(file));
    }

    @Override
    public void run(){
        final YoutubeDownloadUtility youtubeDownloadUtility = new YoutubeDownloadUtility(context.get());

        int numSongsPreload = StateSingleton.getInstance().getPreferenceUtility().getPreload();

        while (!Thread.interrupted()) {

            synchronized (sessionModel.getPlayQueue()) {
                while (sessionModel.getPlayQueue().size() == 0) {
                    try {
                        Log.d(LOG_TAG, "DownloadWorker blocked, Playqueue empty.");
                        sessionModel.getPlayQueue().wait();
                        Log.d(LOG_TAG, "DownloadWorker unblocked.");
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "PlayQueue waiter interruped with reason: " + e.getMessage());
                    }
                }
            }

            synchronized (sessionModel.getDownloadedQueue()) {
                if (sessionModel.getDownloadedQueue().size() >= numSongsPreload) {
                    try {
                        Log.d(LOG_TAG, "DownloadWorker blocked, Preload limit reached.");
                        sessionModel.getDownloadedQueue().wait();
                        Log.d(LOG_TAG, "DownloadWorker unblocked");
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "DownloadQueue waiter interrupted with reason: " + e.getMessage());
                        break;
                    }
                }
            }

            int index = 0;
            synchronized (sessionModel.getPlayQueue()) {
                while (index < sessionModel.getPlayQueue().size()) {
                    final SongModel nextDownload = sessionModel.getPlayQueue().get(index);
                    if (nextDownload.getSongStatus().equals(SongStatus.RESOLVING) ||
                            !nextDownload.getDownloadStatus().equals(DownloadStatus.NOT_STARTED)) {
                        //Skip if we're already downloading this one
                        index++;
                    } else {
                        Log.d(LOG_TAG, "Index is: " + index);
                        Log.d(LOG_TAG, "Downloading: " + nextDownload.getTitle());
                        nextDownload.setDownloadStatus(DownloadStatus.STARTED);
                        sessionModel.getDownloadedQueue().add(nextDownload);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final File storedFile = youtubeDownloadUtility.downloadSong(nextDownload);
                                final Drawable thumbDrawable = youtubeDownloadUtility.downloadThumb(nextDownload);

                                if (storedFile != null) {
                                    nextDownload.setDownloadPath(storedFile);
                                    nextDownload.setDownloadStatus(DownloadStatus.FINISHED);
                                    nextDownload.setMediaPlayer(mediaPlayerFromFile(storedFile));
                                    if (thumbDrawable != null) {
                                        Log.d(LOG_TAG, "thumbnail downloaded " + thumbDrawable.isVisible());
                                        nextDownload.setThumbnail(thumbDrawable);
                                    }

                                    context.get().checkOnCurrentSong();
                                } else {
                                    nextDownload.setDownloadStatus(DownloadStatus.FAILED);
                                }

                                new OutgoingMessageHandler(context.get()).sendSessionUpdate();
                            }
                        }).start();
                        break;
                    }
                }

                if(index == sessionModel.getPlayQueue().size()){
                    try {
                        Log.d(LOG_TAG, "DownloadWorker blocked, reached index limit.");
                        sessionModel.getPlayQueue().wait();
                        Log.d(LOG_TAG, "DownloadWorker unblocked, received new elements.");
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "PlayQueue waiter interrupted with reason: " + e.getMessage());
                        break;
                    }
                }
            }
        }
        Log.e(LOG_TAG, "DownloadWorker is now dead");
    }

    private static class PlaylistListener extends ObservableList.OnListChangedCallback {

        private final String LOG_TAG = "##PlaylistListener";
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
        }

        @Override
        public void onItemRangeMoved(ObservableList observableList, int i, int i1, int i2) {
        }

        @Override
        public void onItemRangeRemoved(ObservableList observableList, int i, int i1) {
            Log.d(LOG_TAG, i1 + " items removed from play queue");

        }
    }

}