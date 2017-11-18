package ch.ethz.inf.vs.kompose.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.net.URI;
import java.util.UUID;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.patterns.SimpleObserver;

public class YoutubeResolveService {
    public static final int SONG_RESOLVED_SUCCESSFULLY = 0x1;
    public static final int SONG_RESOLVING_ERROR = 0x2;

    private static String LOG_TAG = "##YoutubeResolveService";

    public void resolveSong(Context context, final String sourceUrl, final SimpleObserver observer) {
        @SuppressLint("StaticFieldLeak") YouTubeExtractor youTubeExtractor = new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
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
                        SongModel songDetails = new SongModel(UUID.randomUUID());
                        songDetails.setTitle(title);
                        songDetails.setDownloadUrl(URI.create(downloadUrl));
                        songDetails.setThumbnailUrl(URI.create(thumbnailUrl));
                        songDetails.setSourceUrl(URI.create(sourceUrl));
                        songDetails.setSecondsLength((int) length);

                        // notify observer & terminate
                        observer.notify(SONG_RESOLVED_SUCCESSFULLY, songDetails);
                        return;
                    }
                } catch (Exception ex) {
                    // "error handling"
                    Log.e(LOG_TAG, ex.toString());
                }
                observer.notify(SONG_RESOLVING_ERROR);
            }
        };
        youTubeExtractor.extract(sourceUrl, true, false);
    }
}
