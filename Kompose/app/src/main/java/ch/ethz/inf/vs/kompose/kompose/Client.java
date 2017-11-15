package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.util.UUID;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class Client {

    private static final String LOG_TAG = "### Client";

    private Context context;
    private String username;
    private UUID uuid;

    public Client(Context context, String username, UUID uuid) {
        this.context = context;
        this.username = username;
        this.uuid = uuid;
    }

    private void sendRequestSong(String youtubeUrl, String downloadUrl, String title) {
        PlaylistItem playlistItem = new PlaylistItem(
                context,
                -1,
                -1,
                title,
                downloadUrl,
                youtubeUrl
        );
        Message message = new Message(
                Message.MessageType.REQUEST_SONG,
                this.username,
                this.uuid,
                "",
                null,
                playlistItem
        );

        // TODO
        // run a MessageSendTask
    }

    public void requestSong(final String youtubeUrl) {
        YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                if (sparseArray!= null) {
                    int itag = 140; // TODO: intelligent selection?
                    String downloadUrl = sparseArray.get(itag).getUrl();
                    String title = videoMeta.getTitle();
                    Log.d(LOG_TAG, "extracted YouTube URL (" + title + "): " + downloadUrl);
                    sendRequestSong(youtubeUrl, downloadUrl, title);
                }
            }
        };
        youTubeExtractor.extract(youtubeUrl, true, false);
    }
}
