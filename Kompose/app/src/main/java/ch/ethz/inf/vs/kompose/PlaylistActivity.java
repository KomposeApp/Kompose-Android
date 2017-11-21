package ch.ethz.inf.vs.kompose;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.service.SongService;
import ch.ethz.inf.vs.kompose.service.YoutubeService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class PlaylistActivity extends BaseServiceActivity implements BaseServiceActivity.IntentActionCallbackReceiver {

    private static final String LOG_TAG = "## Playlist Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_placeholder);

        bindBaseService(YoutubeService.class);
        bindBaseService(SongService.class);

        subscribeToIntentActions(new String[]{
                YoutubeService.DOWNLOAD_FAILED, YoutubeService.DOWNLOAD_SUCCESSFUL
        }, this);
    }

    public void requestSong(View v) {
        getYoutubeService().resolveSong("https://www.youtube.com/watch?v=-Fz85FE0KtQ");
    }

    public void downvoteItem(View v) {
        //TODO: Downvote stuff
    }

    public void viewHistoryFromPlaylist(View v) {
        Log.d(LOG_TAG, "History button pressed from Playlist Activity");
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
    }

    public void leaveParty(View v) {
        Log.d(LOG_TAG, "Left the party by pressing the button");
        this.finish();
    }

    @Override
    public void intentActionReceived(String action, Intent intent) {
        if (YoutubeService.DOWNLOAD_SUCCESSFUL.equals(action)) {
            getSongService().requestNewSong(intent.<Song>getParcelableExtra("songDetails"));
        } else if (YoutubeService.DOWNLOAD_FAILED.equals(action)) {
            Toast.makeText(PlaylistActivity.this, R.string.youtube_service_download_failed, Toast.LENGTH_LONG).show();
        }
    }
}
