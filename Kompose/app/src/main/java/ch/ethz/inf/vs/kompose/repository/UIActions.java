package ch.ethz.inf.vs.kompose.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;

import java.net.InetAddress;
import java.util.UUID;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.data.SongDetails;
import ch.ethz.inf.vs.kompose.service.StateService;
import ch.ethz.inf.vs.kompose.service.NetworkService;

/**
 * Application actions that can be triggered from the UI.
 */
public class UIActions {

    public static void requestSong(Context context, final String sourceUrl) {
        @SuppressLint("StaticFieldLeak") YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                if (sparseArray != null) {
                    int itag = 140;
                    String downloadUrl = sparseArray.get(itag).getUrl();
                    String thumbnailUrl = videoMeta.getThumbUrl();
                    String title = videoMeta.getTitle();

                    // build message
                    Message msg = new Message();
                    msg.setSenderUsername("fixme"); // TODO
                    msg.setSenderUuid(StateService.getInstance().deviceUUID.toString());

                    SongDetails songDetails = new SongDetails();
                    songDetails.setDownloadUrl(downloadUrl);
                    songDetails.setSourceUrl(sourceUrl);
                    songDetails.setThumbnailUrl(thumbnailUrl);
                    songDetails.setItemUuid(UUID.randomUUID().toString());

                    msg.setSongDetails(songDetails);
                    InetAddress hostIP = StateService.getInstance().hostIP;
                    int hostPort = StateService.getInstance().hostPort;
                    NetworkService.sendMessage(msg, hostIP, hostPort);
                }
            }
        };
        youTubeExtractor.extract(sourceUrl, true, false);
    }

    // TODO
    public static void registerClient(UUID clientUUID, String userName) {
    }

    // TODO
    public static void unregisterClient(UUID clientUUID) {
    }

    // TODO
    public static void downvoteSong(UUID songUUID) {
    }
}
