package ch.ethz.inf.vs.kompose.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class YoutubeService extends BaseService {

    //intent events
    public final static String DOWNLOAD_FINISHED =
            "YoutubeService.DOWNLOAD_FINISHED";
    public final static String DOWNLOAD_FAILED =
            "YoutubeService.DOWNLOAD_FAILED";

    private static String LOG_TAG = "##YoutubeService";

    public void resolveSong(final String sourceUrl) {
        @SuppressLint("StaticFieldLeak") YouTubeExtractor youTubeExtractor = new YouTubeExtractor(this) {
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

                        // notify observer & terminate
                        intent = new Intent(DOWNLOAD_FINISHED);
                        intent.putExtra("songDetails", song);
                    }
                } catch (Exception ex) {
                    // "error handling"
                    Log.e(LOG_TAG, ex.toString());
                } finally {
                    if (intent == null) {
                        intent = new Intent(DOWNLOAD_FAILED);
                    }
                }

                intent.putExtra("sourceUrl", sourceUrl);
                sendBroadcast(intent);
            }
        };
        youTubeExtractor.extract(sourceUrl, true, false);
    }
}
