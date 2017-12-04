package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;

public class SongRequestListener implements SimpleListener<Integer, SongModel> {

    private static final String LOG_TAG = "## SongRequestListener";

    private Context ctx;

    public SongRequestListener(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onEvent(Integer status, SongModel value) {
        if (status == YoutubeDownloadUtility.RESOLVE_SUCCESS) {
            Log.d(LOG_TAG, "resolved download url: " + value.getDownloadUrl());
            new OutgoingMessageHandler(ctx).sendRequestSong(value);
        } else {
            Log.e(LOG_TAG, "resolving url failed");
            Toast.makeText(ctx, "Failed to resolve Youtube URL", Toast.LENGTH_LONG).show();

            SessionModel sessionModel =
                    StateSingleton.getInstance().getActiveSession();
            if (sessionModel != null && sessionModel.getPlayQueue().contains(value)) {
                sessionModel.getPlayQueue().remove(value);
            }
        }
    }
}