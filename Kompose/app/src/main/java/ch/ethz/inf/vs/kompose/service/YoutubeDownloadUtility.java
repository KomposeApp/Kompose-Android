package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.youtube.YouTubeExtractor;


public class YoutubeDownloadUtility {

    private final String LOG_TAG = "## Download Utility";

    public static final int RESOLVE_SUCCESS = 0x1;
    public static final int RESOLVE_FAILED = 0x2;
    public static final int DOWNLOAD_SUCCESS = 0x3;
    public static final int DOWNLOAD_FAILED = 0x4;

    private Context context;

    public YoutubeDownloadUtility(Context ctx) {
        context = ctx;
    }

    /**
     * Resolve song metadata, including HTTP URL, Download URL, Download Thumbnail,
     * Title of the Video and Video Length.
     *
     * @param listener  Listener which will be notified upon completion
     */
    public void resolveSong(final SongModel songModel, final SimpleListener<Integer, SongModel> listener) {
        YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context, songModel, listener);
        youTubeExtractor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Download the file from the specified URL and notify observers when done.
     * The notifier will carry a MediaPlayer that can be used to play the file.
     * TODO: BUG: Find out why this is sometimes called 10+ times in a row
     * @return true if the download succeeded, false otherwise
     */
    public File downloadSong(final SongModel songModel) {

        String videoID = songModel.getVideoID();
        if (videoID == null){
            Log.e(LOG_TAG, "File download failed");
            return null;
        }

        final File storedFile;
        if (StateSingleton.getInstance().checkCacheByKey(videoID)) {
            storedFile = StateSingleton.getInstance().retrieveSongFromCache(videoID);
        }
        else{
            //Song not found in cache:
            try {
                Log.d(LOG_TAG, "Video ID: " + songModel.getVideoID());
                URL url = new URL(songModel.getDownloadUrl().toString());
                URLConnection connection = url.openConnection();
                connection.connect();

                // Detect the file length
                final int fileLength = connection.getContentLength();

                final InputStream input = new BufferedInputStream(connection.getInputStream());
                storedFile = new File(context.getCacheDir(), songModel.getFileName());
                final OutputStream output = new FileOutputStream(storedFile);

                byte[] buffer = new byte[1024];
                int count;
                int total = 0;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                    total += count;

                    final int currentTotal = total;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            songModel.setDownloadProgress((int) (currentTotal * 100 / fileLength));
                        }
                    });
                }
                input.close();
                output.close();

                StateSingleton.getInstance().addSongToCache(videoID, storedFile);
            } catch (Exception e) {
                Log.e(LOG_TAG, "File download failed");
                e.printStackTrace();
                return null;
            }
        }
        return storedFile;
    }
}
