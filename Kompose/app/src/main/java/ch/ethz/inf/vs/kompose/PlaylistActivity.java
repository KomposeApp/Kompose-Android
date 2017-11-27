package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.databinding.ActivityHistoryOverviewBinding;
import ch.ethz.inf.vs.kompose.databinding.ActivityPlaylistBinding;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;
import ch.ethz.inf.vs.kompose.view.adapter.InQueueSongAdapter;
import ch.ethz.inf.vs.kompose.view.adapter.PastSessionAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.InQueueSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.PlaylistViewModel;

public class PlaylistActivity extends AppCompatActivity implements InQueueSongViewHolder.ClickListener {

    private static final String LOG_TAG = "## Playlist Activity";
    private NetworkService networkService;

    private final PlaylistViewModel viewModel = new PlaylistViewModel(null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        networkService = new NetworkService();


        ActivityPlaylistBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new InQueueSongAdapter(viewModel.getSessionModel().getSongs(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);
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

    public void downvoteItem(View v) {
        // get song from view
        // TODO
        Song song = new Song();

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

    @Override
    public void downVoteClicked(View v, int position) {
        SongModel songModel = viewModel.getSessionModel().getSongs().get(position);
        //todo: transform songModel to song
        // send downvote request
        networkService.sendCastSkipSongVote(null);
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
