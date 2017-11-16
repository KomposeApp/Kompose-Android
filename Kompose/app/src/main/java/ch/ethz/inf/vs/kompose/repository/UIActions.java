package ch.ethz.inf.vs.kompose.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;

import java.net.InetAddress;
import java.util.UUID;

import javax.microedition.khronos.opengles.GL;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.data.SongDetails;
import ch.ethz.inf.vs.kompose.model.GlobalState;
import ch.ethz.inf.vs.kompose.model.NetworkService;

public class UIActions {

    public static void requestSong(final String sourceUrl, Context context) {
        YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context) {
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
                    msg.setSenderUuid(GlobalState.getInstance().deviceUUID.toString());

                    SongDetails songDetails = new SongDetails();
                    songDetails.setDownloadUrl(downloadUrl);
                    songDetails.setSourceUrl(sourceUrl);
                    songDetails.setThumbnailUrl(thumbnailUrl);
                    songDetails.setItemUuid(UUID.randomUUID().toString());

                    msg.setSongDetails(songDetails);
                    InetAddress hostIP = GlobalState.getInstance().hostIP;
                    int hostPort = GlobalState.getInstance().hostPort;
                    NetworkService.sendMessage(msg, hostIP, hostPort);
                }
            }
        };
        youTubeExtractor.extract(sourceUrl, true, false);
    }

    // TODO
    public static void registerClient() {
    }

    // TODO
    public static void unregisterClient() {
    }

    // TODO
    public static void downvoteSong(UUID songUUID) {
    }
}
