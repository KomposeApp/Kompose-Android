package ch.ethz.inf.vs.kompose.service;

import android.annotation.SuppressLint;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ch.ethz.inf.vs.kompose.data.json.Song;


public class YoutubeDownloadUtility {

    private static final String LOG_TAG = "## Download Utility";

    private static final int RESOLVE_SUCCESS = 0x1;
    private static final int RESOLVE_FAILED = 0x2;

    private static final int DOWNLOAD_SUCCESS = 0x3;
    private static final int DOWNLOAD_FAILED = 0x4;

    private Context context;

    public YoutubeDownloadUtility(Context ctx){
        context = ctx;
    }

    /**
     * Resolve song metadata, including HTTP URL, Download URL, Download Thumbnail,
     * Title of the Video and Video Length.
     * @param sourceUrl Youtube URL as seen in the browser
     * @param listener Listener which will be notified upon completion
     */
    public void resolveSong(final String sourceUrl, final SimpleListener listener) {
        @SuppressLint("StaticFieldLeak")
        YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                Intent intent = null;
                try {
                    if (sparseArray != null) {
                        // magic number that selects m4a 128 bit
                        // TODO: more intelligent selection
                        int iTag = 140;

                        // get URI & title
                        String downloadUrl = sparseArray.get(iTag).getUrl();
                        String thumbnailUrl = videoMeta.getThumbUrl();
                        String title = videoMeta.getTitle();
                        long length = videoMeta.getVideoLength();

                        // construct song model
                        Song song = new Song();
                        song.setTitle(title);
                        song.setDownloadUrl(downloadUrl);
                        song.setThumbnailUrl(thumbnailUrl);
                        song.setSourceUrl(sourceUrl);
                        song.setLengthInSeconds((int) length);

                        // notify listener
                        listener.onEvent(RESOLVE_SUCCESS, song);
                    }
                } catch (Exception e) {
                    listener.onEvent(RESOLVE_FAILED, null);
                }
            }
        };
        youTubeExtractor.extract(sourceUrl, true, false);
    }

    /**
     * Download the file from the specified URL and notify observers when done.
     * The notifier will carry a MediaPlayer that can be used to play the file.
     * @param directURL WARNING: THIS IS NOT THE BROWSER URL. USE {@link #resolveSong(String, SimpleListener)} AND A LISTENER.
     * @param fileName file to store the song in
     * @param listener the listener that will be called upon success or failure. Will carry
     *                 the `File` if successful or `null` on failure.
     * @return true if the download succeeded, false otherwise
     */
    public void downloadSong(String directURL, String fileName, SimpleListener listener) {
        AsyncDownloader asyncDownloader = new AsyncDownloader(context, directURL, fileName, listener);
        asyncDownloader.execute();
    }

    private static class AsyncDownloader extends AsyncTask<Void,Void,Void> {

        private Context context;
        private String downloadUrl;
        private String fileName;
        private SimpleListener listener;

        private File storedFile = null;

        AsyncDownloader(Context context,
                        String downloadUrl,
                        String fileName,
                        SimpleListener listener) {
            this.context = context;
            this.downloadUrl = downloadUrl;
            this.fileName = fileName;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(downloadUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(connection.getInputStream());
                File storedFile = new File(context.getCacheDir(), fileName);
                OutputStream output = new FileOutputStream(storedFile);

                byte[] buffer = new byte[1024];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                input.close();
                output.close();

            } catch (Exception e) {
                Log.e(LOG_TAG, "File download failed");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            if (storedFile != null) {
                listener.onEvent(DOWNLOAD_SUCCESS, storedFile);
            } else {
                listener.onEvent(DOWNLOAD_FAILED, null);
            }
        }
    }
}
