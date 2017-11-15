package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

class PlaylistItem {

    private static final String LOG_TAG = "### PlaylistItem";

    private boolean isDownloaded = false;
    private File storedFile;
    private OnDownloadFinished onFinishedCallback;
    private Context context;

    private int id;
    private int numDownvotes;
    private String downloadUrl;
    private String youTubeUrl;
    private String title;

    PlaylistItem(Context context,
                 int id,
                 int numDownvotes,
                 String title,
                 String downloadUrl,
                 String youTubeUrl) {
        this.context = context;
        this.id = id;
        this.numDownvotes = numDownvotes;
        this.title = title;
        this.downloadUrl = downloadUrl;
        this.youTubeUrl = youTubeUrl;
    }

    PlaylistItem(Context context, JSONObject json) {
        this.context = context;
        this.id = json.optInt("id");
        this.numDownvotes = json.optInt("num_downvotes");
        this.title = json.optString("title");
        this.downloadUrl = json.optString("download_url");
        this.youTubeUrl = json.optString("youtube_url");
    }

    boolean getIsDownloaded() {
        return isDownloaded;
    }

    File getStoredFile() {
        return storedFile;
    }

    OnDownloadFinished getOnFinishedCallback() {
        return onFinishedCallback;
    }

    Context getContext() {
        return context;
    }

    String getDownloadUrl() {
        return downloadUrl;
    }

    String getTitle() {
        return title;
    }

    int getId() {
        return id;
    }

    void setStoredFile(File file) {
        storedFile = file;
    }

    void setIsDownloaded(boolean status) {
        isDownloaded = status;
    }

    // Store a callback that will be executed when the file download has completed.
    void registerOnDownloadFinishedCallback(OnDownloadFinished callback) {
        this.onFinishedCallback = callback;
    }

    // Deregister the onFinished callback.
    void unregisterOnDownloadFinishedCallback() {
        this.onFinishedCallback = null;
    }

    // Construct a MediaPlayer from the locally stored audio file.
    // If not yet downloaded, return null.
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

    // Extract the YouTube download URL and then start an AsyncTask to download the file.
    void downloadInBackground() {
        Log.d(LOG_TAG, "starting background download");
        DownloadAudioTask dlTask = new DownloadAudioTask(getThis());
        dlTask.execute(downloadUrl);
    }

    JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("num_downvotes", numDownvotes);
        json.put("title", title);
        json.put("download_url", downloadUrl);
        json.put("youtube_url", youTubeUrl);
        return json;
    }
}
