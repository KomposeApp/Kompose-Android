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

import java.net.MalformedURLException;
import java.net.URL;

import ch.ethz.inf.vs.kompose.service.YoutubeService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class PlaylistActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Playlist Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_placeholder);

        Intent gattServiceIntent = new Intent(this, YoutubeService.class);
        boolean isBound = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Log.d(LOG_TAG, "finished creation, bound service: " + isBound);
    }

    public void requestSong(View v) {
        youtubeService.resolveSong("https://www.youtube.com/watch?v=-Fz85FE0KtQ");
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


    YoutubeService youtubeService;

    //service connection
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            youtubeService = (YoutubeService) ((BaseService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        //register for events
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(YoutubeService.DOWNLOAD_FAILED);
        intentFilter.addAction(YoutubeService.DOWNLOAD_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    //the receiver
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (YoutubeService.DOWNLOAD_FINISHED.equals(action)) {
                //now send the song to the repository
            }
        }
    };

}
