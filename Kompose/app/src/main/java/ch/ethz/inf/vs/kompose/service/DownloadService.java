package ch.ethz.inf.vs.kompose.service;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/*
 * Service to download a song to the file system an creating a media player.
 */
public class DownloadService {

    private static final String LOG_TAG = "## DownloadService";

    /**
     * Download the file from the specified URL and notify observers when done.
     * The notifier will carry a MediaPlayer that can be used to play the file.
     */
    public boolean downloadSong(String downloadUrl, String fileName) {
        try {
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            InputStream input = new BufferedInputStream(connection.getInputStream());
            File storedFile = new File(getCacheDir(), fileName);
            OutputStream output = new FileOutputStream(storedFile);

            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            input.close();
            output.close();

            // MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.fromFile(storedFile));

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "exception occured " + e.getMessage());
        }
        return false;
    }
}
