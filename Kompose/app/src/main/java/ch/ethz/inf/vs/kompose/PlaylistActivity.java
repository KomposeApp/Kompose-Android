package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class PlaylistActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Playlist Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_placeholder);
    }

    public void requestSong(View v){
        //TODO: Request song
    }

    public void downvoteItem(View v){
        //TODO: Downvote stuff
    }

    public void viewHistoryFromPlaylist(View v){
        Log.d(LOG_TAG, "History button pressed from Playlist Activity");
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
    }

    public void leaveParty(View v){
        Log.d(LOG_TAG, "Left the party by pressing the button");
        this.finish();
    }

}
