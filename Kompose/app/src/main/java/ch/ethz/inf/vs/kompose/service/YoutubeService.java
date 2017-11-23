package ch.ethz.inf.vs.kompose.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ch.ethz.inf.vs.kompose.data.json.Song;

public class YoutubeService {

    private static final String LOG_TAG = "##YoutubeService";
    private static final int RESOLVE_SUCCESS = 0x1;
    private static final int RESOLVE_FAILED = 0x2;

    public void resolveSong(Context context, final String sourceUrl, final SimpleListener listener) {
        @SuppressLint("StaticFieldLeak") YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context) {
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

                        // notify listener
                        listener.onEvent(RESOLVE_SUCCESS, song);
                    }
                } catch (Exception e) {
                    listener.onEvent(RESOLVE_FAILED, null);
                }
            }
        };
        youTubeExtractor.extract(sourceUrl, true, false);
    }
}
