package ch.ethz.inf.vs.kompose.service.audio;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.inf.vs.kompose.R;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;
import ch.ethz.inf.vs.kompose.service.youtube.YoutubeDownloadUtility;

public abstract class SongResolveHandler {

    private static final String LOG_TAG = "##SongResolveHandler";

    /**
     * Handles the song requesting per URL. Required by at least two activities.
     * @param ctx Context in which the song resolver is called
     * @param url URL to resolve
     * @return true iff the URL was a proper youtube URL
     */
    public static boolean resolveAndRequestSong(Context ctx, String url) {
        Log.d(LOG_TAG, "received the following link: " + url);

        String youtubeUrl;
        if (url == null) youtubeUrl = "";
        else youtubeUrl = url.trim();

        SessionModel activeSession = StateSingleton.getInstance().getActiveSession();
        ClientModel clientModel = StateSingleton.getInstance().getActiveClient();

        if (activeSession == null || clientModel == null){
            throw new IllegalStateException("Failed to resolve URL: No active session!!");
        }

        //set session to active if host
        if (activeSession.getIsHost() && activeSession.getSessionStatus().equals(SessionStatus.WAITING)) {
            activeSession.setSessionStatus(SessionStatus.ACTIVE);
        }

        //Parse URL for Identity String:
        //Note: We do not check whether the URL really is Youtube -- instead we want to retrieve a video ID.
        String regex = "(?:^.*(?:\\?|&)v=([^&?]*)(?:&.*$|$))|(?:^http(?:s?)://youtu\\.be/([^&?]*)$)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(youtubeUrl);

        String videoID;
        if (m.find()) {
            if (m.group(1) != null) {
                Log.d(LOG_TAG, "Standard URL with Video ID ?v=... parsed.");
                videoID = m.group(1);
            } else if (m.group(2) != null) {
                Log.d(LOG_TAG, "Compressed Youtube link parsed.");
                videoID = m.group(2);
            } else {
                throw new IllegalStateException("Regex broke");
            }
        }else{
            Log.w(LOG_TAG, "Malformed URL passed");
            return false;
        }

        // Create a clean youtube link to discard any Youtube playlist link metadata or similar
        youtubeUrl = "https://www.youtube.com/watch?v=" + videoID;
        Log.d(LOG_TAG, "requesting URL: " + youtubeUrl);

        URI youtubeURI;
        try {
            youtubeURI = URI.create(youtubeUrl);
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to create URI object from Youtube URL.");
            return false;
        }

        SongModel songModel = new SongModel(UUID.randomUUID(), clientModel, activeSession);
        songModel.setSourceUrl(youtubeURI);
        songModel.setTitle("downloading info...");
        songModel.setSongStatus(SongStatus.RESOLVING);
        songModel.setVideoID(videoID);

        ProgressDialog pDialog = null;
        if (StateSingleton.getInstance().isPlaylistInForeground()) {
         pDialog = ProgressDialog.show(ctx, ctx.getString(R.string.progress_resolve_title),
                    ctx.getString(R.string.progress_be_patient), true, false);
        }

        YoutubeDownloadUtility youtubeService = new YoutubeDownloadUtility(ctx);
        youtubeService.resolveSong(songModel, new SongRequestListener(ctx, pDialog));
        return true;
    }


    private static class SongRequestListener implements SimpleListener<Integer, SongModel> {

        private final String LOG_TAG = "##SongRequestListener";

        private Context ctx;
        private ProgressDialog progressDialog;

        private SongRequestListener(Context ctx, ProgressDialog progressDialog) {
            this.ctx = ctx;
            this.progressDialog = progressDialog;
        }

        @Override
        public void onEvent(Integer status, SongModel value) {
           if (progressDialog != null) progressDialog.cancel();
            if (status == YoutubeDownloadUtility.RESOLVE_SUCCESS) {
                value.setSongStatus(SongStatus.REQUESTED);
                Log.d(LOG_TAG, "resolved download url: " + value.getDownloadUrl());
                new OutgoingMessageHandler(ctx).sendRequestSong(value);
            } else {
                Log.e(LOG_TAG, "resolving url failed");
                Toast.makeText(ctx, ctx.getText(R.string.view_error_resolve), Toast.LENGTH_LONG).show();
            }
        }
    }
}
