package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ch.ethz.inf.vs.kompose.patterns.SimpleObserver;

/*
 * Service to download a song to the file system an creating a media player.
 */
public class SongService {

    private static final String LOG_TAG = "## SongService";

    public static final int DOWNLOAD_SUCCESSFUL = 0x1;
    public static final int DOWNLOAD_FAILED = 0x2;

    /**
     * Download the file from the specified URL and notify observers when done.
     * The notifier will carry a MediaPlayer that can be used to play the file.
     */
    public void downloadSong(Context context,
                             String downloadUrl,
                             String fileName,
                             SimpleObserver observer) {
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

            MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.fromFile(storedFile));
            observer.notify(DOWNLOAD_SUCCESSFUL, mediaPlayer);

        } catch (Exception e) {
            observer.notify(DOWNLOAD_FAILED, null);
        }
    }
}
