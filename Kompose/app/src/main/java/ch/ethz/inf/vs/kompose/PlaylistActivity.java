package ch.ethz.inf.vs.kompose;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_placeholder);
    }

    public void requestSong(View v){}

    public void downvoteItem(View v){}

    public void viewHistoryFromPlaylist(View v){}

    public void leaveParty(View v){}

}
