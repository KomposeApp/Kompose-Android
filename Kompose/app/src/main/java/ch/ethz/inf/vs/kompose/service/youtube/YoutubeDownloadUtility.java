package ch.ethz.inf.vs.kompose.service.youtube;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.youtube.extractor.YouTubeExtractor;


public class YoutubeDownloadUtility {

    private final String LOG_TAG = "##DownloadUtility";

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
        Log.d(LOG_TAG, "Video ID: " + videoID);

        if (videoID == null){
            Log.e(LOG_TAG, "File resolving failed");
            return null;
        }

        final File storedFile;
        if (StateSingleton.getInstance().checkCacheByKey(videoID)) {
            //Song found in cache
            storedFile = StateSingleton.getInstance().retrieveSongFromCache(videoID);
        }
        else{
            //Song not found in cache:
            InputStream input = null;
            OutputStream output = null;
            try {
                URL url = new URL(songModel.getDownloadUrl().toString());
                URLConnection connection = url.openConnection();
                connection.connect();

                input = new BufferedInputStream(connection.getInputStream());
                storedFile = new File(context.getCacheDir(), songModel.getFileName());
                output = new FileOutputStream(storedFile);

                // Detect the file length
                final int fileLength = connection.getContentLength();

                byte[] buffer = new byte[4096];
                int count, total = 0;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                    total += count;

                    final int currentTotal = total;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            songModel.setDownloadProgress(currentTotal * 100 / fileLength);
                        }
                    });
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "File download failed");
                return null;
            } finally {
                try {
                    if (input != null)  input.close();
                    if (output != null) output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Failed to close input/outputstream. This will probably have consequences.");
                }
            }

            Log.d(LOG_TAG,"Filesize: " + storedFile.length());
            StateSingleton.getInstance().addSongToCache(videoID, storedFile);
        }
        return storedFile;
    }

    public Drawable downloadThumb(SongModel songModel) {

        // We don't cache thumbnails because they're extremely lightweight
        InputStream input = null;
        OutputStream output = null;
        try {
            URL url = new URL(songModel.getThumbnailUrl().toString());
            URLConnection connection = url.openConnection();
            connection.connect();

            input = new BufferedInputStream(connection.getInputStream());
            File thumbFile = new File(context.getCacheDir(), "thumb_" + songModel.getUUID() + ".jpg");
            output = new FileOutputStream(thumbFile);

            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return Drawable.createFromPath(thumbFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Thumbnail download failed");
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (input != null)  input.close();
                if (output != null) output.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Failed to close input/outputstream. This will probably have consequences.");
            }
        }
    }
}
