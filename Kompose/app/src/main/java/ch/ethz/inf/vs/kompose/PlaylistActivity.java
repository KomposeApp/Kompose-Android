package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.databinding.ActivityPlaylistBinding;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;
import ch.ethz.inf.vs.kompose.view.adapter.InQueueSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.InQueueSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.PlaylistViewModel;

public class PlaylistActivity extends AppCompatActivity implements InQueueSongViewHolder.ClickListener {

    private static final String LOG_TAG = "## Playlist Activity";
    private NetworkService networkService;

    private final PlaylistViewModel viewModel = new PlaylistViewModel(StateSingleton.getInstance().activeSession);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkService = new NetworkService();

        setContentView(R.layout.activity_playlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        ActivityPlaylistBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new InQueueSongAdapter(viewModel.getSessionModel().getSongs(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_playlist_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.leave_session:
                //todo leave session
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void requestSong(View v) {

        // TODO
        // get youtube url from view
        String youtubeUrl = "https://www.youtube.com/watch?v=-Fz85FE0KtQ";

        YoutubeDownloadUtility youtubeService = new YoutubeDownloadUtility(this);
        youtubeService.resolveSong(youtubeUrl, new SimpleListener() {
            @Override
            public void onEvent(int status) {
            }

            @Override
            public void onEvent(int status, Object object) {
                Song song = (Song) object;
                networkService.sendRequestSong(song);
            }
        });
    }

    public void viewHistory(View v) {
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

    @Override
    public void downVoteClicked(View v, int position) {
        SongModel songModel = viewModel.getSessionModel().getSongs().get(position);
        //todo: transform songModel to song
        // send downvote request
        networkService.sendCastSkipSongVote(null);
    }
}
