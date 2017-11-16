package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.kompose.ch.ethz.inf.vs.kompose.kompose.archive.DownloadAudioTask;

class PlaylistItem {

    private static final String LOG_TAG = "### PlaylistItem";

    private boolean isDownloaded = false;
    private File storedFile;
    private OnDownloadFinished onFinishedCallback;

    public int order;
    public UUID itemUUID;
    public List<UUID> downvotes;
    public String title;
    public UUID proposedBy;
    public String downloadUrl;
    public String sourceUrl;

    PlaylistItem(int order,
                 UUID itemUUID,
                 List<UUID> downvotes,
                 String title,
                 UUID proposedBy,
                 String downloadUrl,
                 String sourceUrl) {
        this.order = order;
        this.itemUUID = itemUUID;
        this.title = title;
        this.proposedBy = proposedBy;
        this.downloadUrl = downloadUrl;
        this.sourceUrl = sourceUrl;
    }

    PlaylistItem(JSONObject json) {
        this.order = json.optInt("order", 0);

        String itemUUIDParse = json.optString("item_uuid", null);
        if (itemUUIDParse != null)
        this.itemUUID
//        this.id = json.optInt("id");
//        this.numDownvotes = json.optInt("num_downvotes");
//        this.title = json.optString("title");
//        this.downloadUrl = json.optString("download_url");
//        this.youTubeUrl = json.optString("youtube_url");
    }

    // TODO
    private List<UUID> jsonToUUIDList(JSONArray json) {
        return null;
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
    MediaPlayer getMediaPlayer(Context context) {
        MediaPlayer mediaPlayer = null;
        if (isDownloaded && storedFile != null) {
            mediaPlayer = MediaPlayer.create(context, Uri.fromFile(this.storedFile));
        }
        return mediaPlayer;
    }

    // Extract the YouTube download URL and then start an AsyncTask to download the file.
    void downloadInBackground(Context context) {
        Log.d(LOG_TAG, "starting background download");
        DownloadAudioTask dlTask = new DownloadAudioTask(this);
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
