package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;

public class PlaylistActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Playlist Activity";
    private NetworkService networkService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_placeholder);
        networkService = new NetworkService();
    }

    public void requestSong(View v) {

        // TODO
        // get youtube url from view
        String youtubeUrl = "https://www.youtube.com/watch?v=-Fz85FE0KtQ";

        YoutubeDownloadUtility youtubeService = new YoutubeDownloadUtility(this);
        youtubeService.resolveSong(youtubeUrl, new SimpleListener() {
                    @Override
                    public void onEvent(int status) {}

                    @Override
                    public void onEvent(int status, Object object) {
                        Song song = (Song) object;
                        networkService.sendRequestSong(song);
                    }
                });
    }

    public void downvoteItem(View v) {
        // get song from view
        // TODO
        Song song = new Song();

        // send downvote request
        networkService.sendCastSkipSongVote(song);
    }

    public void viewHistoryFromPlaylist(View v) {
        Log.d(LOG_TAG, "History button pressed from Playlist Activity");
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
    }

    public void leaveParty(View v) {
        Log.d(LOG_TAG, "Left the party by pressing the button");

        // update state
        // TODO

        // unregister the client
        networkService.sendUnRegisterClient();

        this.finish();
    }

    /**
     * leaves the currently active session
     */
//    public void leaveSession() {
//        if (isHost) {
//            getNetworkService().sendFinishSession();
//        } else {
//            getNetworkService().sendUnRegisterClient();
//        }
//
//        isHost = false;
//        activeSessionModel = null;
//        activeClient = null;
//
//        broadcastConnectionChanged();
//    }

}
