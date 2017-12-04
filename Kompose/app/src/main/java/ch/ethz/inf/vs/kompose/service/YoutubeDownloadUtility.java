package ch.ethz.inf.vs.kompose.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.youtube.VideoMeta;
import ch.ethz.inf.vs.kompose.service.youtube.YouTubeExtractor;
import ch.ethz.inf.vs.kompose.service.youtube.YtFile;


public class YoutubeDownloadUtility {

    private static final String LOG_TAG = "## Download Utility";

    public static final int RESOLVE_SUCCESS = 0x1;
    public static final int RESOLVE_FAILED = 0x2;
    public static final int DOWNLOAD_SUCCESS = 0x3;
    public static final int DOWNLOAD_FAILED = 0x4;

    private Context context;

    //Constructor
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
     *
     * @return true if the download succeeded, false otherwise
     */
    public File downloadSong(final SongModel songModel) {
        try {
            URL url = new URL(songModel.getDownloadUrl().toString());
            URLConnection connection = url.openConnection();
            connection.connect();

            // Detect the file lenghth
            final int fileLength = connection.getContentLength();

            final InputStream input = new BufferedInputStream(connection.getInputStream());
            final File storedFile = new File(context.getCacheDir(), songModel.getFileName());
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

            return storedFile;

        } catch (Exception e) {
            Log.e(LOG_TAG, "File download failed");
            e.printStackTrace();
        }

        return null;
    }
}
