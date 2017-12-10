package ch.ethz.inf.vs.kompose.service.audio;

import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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

public class DownloadWorker extends AsyncTask<Void, Void, Void> {

    private final String LOG_TAG = "##DownloadWorker";

    private WeakReference<AudioService> context;
    private SessionModel sessionModel;
    private PlaylistListener playlistListener;

    DownloadWorker(AudioService context, SessionModel sessionModel) {
        this.context = new WeakReference<>(context);
        this.sessionModel = sessionModel;

        this.playlistListener = new PlaylistListener(context);
        this.sessionModel.getPlayQueue().addOnListChangedCallback(playlistListener);
    }

    private MediaPlayer mediaPlayerFromFile(File file) {
        return MediaPlayer.create(context.get(), Uri.fromFile(file));
    }

    @Override
    protected Void doInBackground(Void... voids) {
        YoutubeDownloadUtility youtubeDownloadUtility = new YoutubeDownloadUtility(context.get());
        int numSongsPreload = StateSingleton.getInstance().getPreferenceUtility().getPreload();


        restart: while (!isCancelled()) {
            int numDownloaded = 0;
            int index = 0;

            //TODO: Data race on playqueue
            while (numDownloaded < numSongsPreload && index < sessionModel.getPlayQueue().size()) {
                final SongModel nextDownload;
                try {
                     nextDownload = sessionModel.getPlayQueue().get(index);
                }catch(IndexOutOfBoundsException e){
                    Log.e(LOG_TAG, "Cheap attempt to avert the data race");
                    break restart;
                }
                if (!nextDownload.getSongStatus().equals(SongStatus.RESOLVING) &&
                        nextDownload.getDownloadStatus() == DownloadStatus.NOT_STARTED) {
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
                                context.get().checkOnCurrentSong();
                            }
                        });
                    }

                    //TODO: Is this really necessary?
                    new OutgoingMessageHandler(context.get()).sendSessionUpdate();
                    index = 0;
                } else {
                    numDownloaded++;
                    index++;
                }
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void r){
        this.sessionModel.getPlayQueue().removeOnListChangedCallback(playlistListener);
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