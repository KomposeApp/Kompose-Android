package ch.ethz.inf.vs.kompose.service.youtube;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.youtube.extractor.YouTubeExtractor;

//Note: The caching here ignores the CacheQuota size

public class YoutubeDownloadUtility {

    private final String LOG_TAG = "##DownloadUtility";
    private final String FILENAME_PREFIX="yt.dl.";

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
     * Download the file from the specified URL and notify observers when done. (or retrieve it from cache)
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

        // Open an URL connection and retrieve the actual filesize
        URLConnection connection;
        int fileLength;
        try {
            URL url = new URL(songModel.getDownloadUrl().toString());
            connection = url.openConnection();

            // Detect the file length
            fileLength = connection.getContentLength();
            if (fileLength <= 0){
                throw new IOException("Target Youtube Video is empty or far too large.");
            }
        } catch (IOException io){
            Log.e(LOG_TAG, "File download failed");
            io.printStackTrace();
            return null;
        }

        // Filter the cache for the desired file:
        final String fileName = FILENAME_PREFIX + videoID;
        File[] matchingFiles = context.getCacheDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (fileName.equals(pathname.getName()));
            }
        });

        //Check if the file that's stored is complete in size (if not, continue)
        File storedFile = null;
        for (File cachedFile: matchingFiles){
            long cachedLength = cachedFile.length();
            Log.d(LOG_TAG, "Filesize on Cache: " + cachedLength + " ----- Filesize on Youtube: " + fileLength);
            if (cachedLength == fileLength){
                storedFile = cachedFile;
                break;
            }
        }

        if (storedFile == null){
            Log.d(LOG_TAG, "No matching file cached. Proceeding to download from Youtube...");
            InputStream input = null;
            OutputStream output = null;
            try {
                //TODO: Filesize limit check from settings

                //Establish direct connection
                connection.connect();

                // Open streams
                input = new BufferedInputStream(connection.getInputStream());
                storedFile = new File(context.getCacheDir(), fileName);
                output = new FileOutputStream(storedFile);

                //Download the file and update the UI display
                byte[] buffer = new byte[4096];
                int in; int total = 0;
                while ((in = input.read(buffer)) != -1) {
                    output.write(buffer, 0, in);
                    total += in;

                    double currentProgress = (double) total / fileLength * 100.0;
                    songModel.setDownloadProgress((int) currentProgress);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "File download failed");
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (input != null)  input.close();
                    if (output != null) output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Failed to close input/outputstream. This will have consequences.");
                }
            }
        }
        return storedFile;
    }

    public Drawable downloadThumb(SongModel songModel) {
        // We won't cache this since it's tiny by comparison
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
