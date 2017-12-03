package ch.ethz.inf.vs.kompose.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
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

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;


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
     * @param sourceUrl Youtube URL as seen in the browser
     * @param listener  Listener which will be notified upon completion
     */
    public void resolveSong(final String sourceUrl, final SessionModel sessionModel, final ClientModel clientModel, final SimpleListener<Integer, SongModel> listener) {
        @SuppressLint("StaticFieldLeak")
        YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                if (ytFiles != null) {
                    // find the best audio track
                    int iTag = -1;
                    int maxBitrate = 0;
                    for (int i = 0, temp_itag; i < ytFiles.size(); i++) {

                        temp_itag = ytFiles.keyAt(i);
                        YtFile file = ytFiles.get(temp_itag);

                        int fBitrate = file.getFormat().getAudioBitrate();
                        if ((file.getFormat().getHeight() == -1) && (fBitrate > maxBitrate)) {
                            iTag = temp_itag;
                            maxBitrate = fBitrate;
                        }
                    }

                    Log.d(LOG_TAG, "Selected itag: " + iTag);

                    if (iTag == -1) {
                        Log.e(LOG_TAG, "Failed to find audio track for given Youtube Link");
                        listener.onEvent(RESOLVE_FAILED, null);
                        return;
                    }

                    // get URI & title
                    String downloadUrl = ytFiles.get(iTag).getUrl();
                    String thumbnailUrl = videoMeta.getThumbUrl();
                    String title = videoMeta.getTitle();
                    long length = videoMeta.getVideoLength();

                    if (downloadUrl.isEmpty() || length <= 0) {
                        Log.e(LOG_TAG, "Download link was empty or length was too short");
                        listener.onEvent(RESOLVE_FAILED, null);
                        return;
                    }

                    // construct song model
                    SongModel songModel = new SongModel(UUID.randomUUID(), clientModel, sessionModel);
                    songModel.setTitle(title);
                    songModel.setDownloadUrl(URI.create(downloadUrl));
                    songModel.setThumbnailUrl(URI.create(thumbnailUrl));
                    songModel.setSourceUrl(URI.create(sourceUrl));
                    songModel.setSecondsLength((int) length);

                    // notify listener
                    listener.onEvent(RESOLVE_SUCCESS, songModel);
                } else {
                    Log.w(LOG_TAG, "Failed to resolve youtube URL -- possible malformed link");
                    listener.onEvent(RESOLVE_FAILED, null);
                }
            }
        };
        String parsedSource = sourceUrl.replace("m.youtube", "youtube");
        youTubeExtractor.extract(parsedSource, true, false);
    }

    /**
     * Download the file from the specified URL and notify observers when done.
     * The notifier will carry a MediaPlayer that can be used to play the file.
     *
     * @param directURL WARNING: THIS IS NOT THE BROWSER URL. USE {@link #resolveSong(String, SessionModel, ClientModel, SimpleListener)} AND A LISTENER.
     * @param fileName  file to store the song in
     * @return true if the download succeeded, false otherwise
     */
    public File downloadSong(String directURL, String fileName) {
        try {
            URL url = new URL(directURL);
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

            return storedFile;

        } catch (Exception e) {
            Log.e(LOG_TAG, "File download failed");
            e.printStackTrace();
        }

        return null;
    }
}
