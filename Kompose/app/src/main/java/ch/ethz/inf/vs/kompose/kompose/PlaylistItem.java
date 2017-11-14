package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

class PlaylistItem {

    private static final String LOG_TAG = "### PlaylistItem";

    private boolean isDownloaded;
    private File storedFile;
    private String youTubeUrl;
    private  String downloadUrl;
    private String title;
    private OnDownloadFinished onFinishedCallback;
    private Context context;

    PlaylistItem(Context context, String url) {
        isDownloaded = false;
        this.youTubeUrl = url;
        this.context = context;
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

    /**
     * Start the download in an AsyncTask. When done, set isDownloaded to `true`,
     * and call the onFinishedCallback, if it is registered.
     *
     * A WeakReference to this PlaylistItem is kept to allow the AsyncTask to be static and
     * therefore avoid memory leaks.
     * See: https://stackoverflow.com/questions/44309241/this-asynctask-class-should-be-static-or-leaks-might-occur
     */
    private static class DownloadAudioTask extends AsyncTask<String, Void, Void> {

        private WeakReference<PlaylistItem> itemContext;

        DownloadAudioTask(PlaylistItem itemContext) {
            this.itemContext = new WeakReference<>(itemContext);
        }

        protected Void doInBackground(String ... urls) {
            if (urls.length < 1) {
                return null;
            }

            try {
                PlaylistItem ctx = itemContext.get();
                Log.d(LOG_TAG, "starting file download");
                URL url = new URL(ctx.downloadUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                // input stream
                InputStream input = new BufferedInputStream(connection.getInputStream());

                // output stream: file on internal storage
                ctx.storedFile = new File(ctx.context.getCacheDir(), ctx.title + ".m4a");
                OutputStream output = new FileOutputStream(ctx.storedFile);

                // write data to file system
                byte[] buffer = new byte[1024];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                Log.d(LOG_TAG, "stored file: " + ctx.storedFile.toString());
                ctx.isDownloaded = true;
                input.close();
                output.close();

                // issue the onFinished callback (if set)
                if (ctx.onFinishedCallback != null) {
                    ctx.onFinishedCallback.downloadFinished();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
