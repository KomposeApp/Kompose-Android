package ch.ethz.inf.vs.kompose;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.databinding.ActivityPlaylistBinding;
import ch.ethz.inf.vs.kompose.databinding.DialogAddYoutubeLinkBinding;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;
import ch.ethz.inf.vs.kompose.view.adapter.InQueueSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.InQueueSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.PlaylistViewModel;

public class PlaylistActivity extends BaseActivity implements InQueueSongViewHolder.ClickListener {

    private static final String LOG_TAG = "## Playlist Activity";
    private NetworkService networkService;

    private final PlaylistViewModel viewModel = new PlaylistViewModel(StateSingleton.getInstance().activeSession);


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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_playlist_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add_link:
                DialogAddYoutubeLinkBinding binding = DataBindingUtil.setContentView(this, R.layout.dialog_add_youtube_link);

                Dialog builder = new Dialog(this);
                builder.setCancelable(true);

               // builder.setContentView(binding.getRoot());

                binding.setViewModel(viewModel);

                builder.show();
                return true;
            case R.id.leave_session:
                leaveSession();
                finish();
                return true;
            case R.id.show_history:
                showHistory();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void requestSong() {
        // get youtube url from view
        String youtubeUrl = viewModel.getSearchLink();
        viewModel.setSearchLink("");

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

    public void showHistory() {
        Log.d(LOG_TAG, "History button pressed from Playlist Activity");
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
    }

    private void leaveSession() {
        Log.d(LOG_TAG, "Left the party by pressing the button");

        //todo technical: do what you must

        // unregister the client
        networkService.sendUnRegisterClient();

        this.finish();
    }

    @Override
    public void downVoteClicked(View v, int position) {
        SongModel songModel = viewModel.getSessionModel().getSongs().get(position);
        //todo technical: transform songModel to song
        // send downvote request
        networkService.sendCastSkipSongVote(null);
    }
}
