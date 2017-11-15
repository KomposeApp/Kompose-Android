package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

class PlaylistItem {

    private static final String LOG_TAG = "### PlaylistItem";

    private boolean isDownloaded = false;
    private File storedFile;
    private OnDownloadFinished onFinishedCallback;
    private Context context;

    // original YouTube URL
    private String youTubeUrl;

    // extracted download URL
    private String downloadUrl;

    private String title;
    private String id;

    PlaylistItem(Context context, String url) {
        isDownloaded = false;
        this.youTubeUrl = url;
        this.context = context;
    }

    boolean getIsDownloaded() {
        return isDownloaded;
    }

    File getStoredFile() {
        return storedFile;
    }

    OnDownloadFinished getOnFinishedCallback() {
        return onFinishedCallback;
    }

    Context getContext() {
        return context;
    }

    String getDownloadUrl() {
        return downloadUrl;
    }

    String getTitle() {
        return title;
    }

    void setStoredFile(File file) {
        storedFile = file;
    }

    void setIsDownloaded(boolean status) {
        isDownloaded = status;
    }

    /**
     * Store a callback that will be executed when the file download has completed.
     */
    void registerOnDownloadFinishedCallback(OnDownloadFinished callback) {
        this.onFinishedCallback = callback;
    }

    /**
     * Deregister the onFinished callback.
     */
    void unregisterOnDownloadFinishedCallback() {
        this.onFinishedCallback = null;
    }

    /**
     * Construct a MediaPlayer from the locally stored audio file.
     * If not yet downloaded, return null.
     */
    MediaPlayer getMediaPlayer() {
        MediaPlayer mediaPlayer = null;
        if (isDownloaded && storedFile != null) {
            mediaPlayer = MediaPlayer.create(context, Uri.fromFile(this.storedFile));
        }
        return mediaPlayer;
    }

    private PlaylistItem getThis() {
        return this;
    }

    /**
     * Extract the YouTube download URL and then start an AsyncTask to download the file.
     */
    void downloadInBackground() {
        Log.d(LOG_TAG, "starting background download");
        Log.d(LOG_TAG, "extracting: " + youTubeUrl);
        YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                if (sparseArray!= null) {
                    int itag = 140;
                    downloadUrl = sparseArray.get(itag).getUrl();
                    title = videoMeta.getTitle();
                    Log.d(LOG_TAG, "extracted YouTube URL: " + downloadUrl);

                    // start the file download
                    DownloadAudioTask dlTask = new DownloadAudioTask(getThis());
                    dlTask.execute(downloadUrl);
                }
            }
        };
        youTubeExtractor.extract(youTubeUrl, true, false);
    }
}
