package ch.ethz.inf.vs.kompose.kompose.ch.ethz.inf.vs.kompose.kompose.archive;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import ch.ethz.inf.vs.kompose.kompose.PlaylistItem;

public class DownloadAudioTask extends AsyncTask<String, Void, Void> {

    private PlaylistItem playlistItemContext;
    private static final String LOG_TAG = "### DownloadAudioTask";

    DownloadAudioTask(PlaylistItem playlistItemContext) {
        this.playlistItemContext =  playlistItemContext;
    }

    protected Void doInBackground(String ... urls) {
        if (urls.length < 1) {
            return null;
        }

        try {
            PlaylistItem ctx = playlistItemContext;

            Log.d(LOG_TAG, "starting file download");
            URL url = new URL(ctx.getDownloadUrl());
            URLConnection connection = url.openConnection();
            connection.connect();

            // input stream
            InputStream input = new BufferedInputStream(connection.getInputStream());

            // output stream: file on internal storage
            ctx.setStoredFile(new File(ctx.getContext().getCacheDir(), ctx.getTitle() + ".m4a"));
            OutputStream output = new FileOutputStream(ctx.getStoredFile());

            // write data to file system
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            Log.d(LOG_TAG, "stored file: " + ctx.getStoredFile().toString());
            ctx.setIsDownloaded(true);
            input.close();
            output.close();

            // issue the onFinished callback (if set)
            if (ctx.getOnFinishedCallback() != null) {
                ctx.getOnFinishedCallback().downloadFinished();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
