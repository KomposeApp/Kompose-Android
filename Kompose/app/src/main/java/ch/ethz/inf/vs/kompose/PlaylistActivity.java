package ch.ethz.inf.vs.kompose;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
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
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;
import ch.ethz.inf.vs.kompose.view.adapter.InQueueSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.InQueueSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.PlaylistViewModel;

public class PlaylistActivity extends BaseActivity implements InQueueSongViewHolder.ClickListener, PlaylistViewModel.ClickListener {

    private static final String LOG_TAG = "## Playlist Activity";
    private OutgoingMessageHandler responseHandler;

    private final PlaylistViewModel viewModel = new PlaylistViewModel(StateSingleton.getInstance().activeSession, this);
    private Intent clientNetworkServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);


        responseHandler = new OutgoingMessageHandler();
        clientNetworkServiceIntent = this.getIntent().getParcelableExtra(MainActivity.KEY_CNETWORKSERVICE);
        Log.d(LOG_TAG, "Client NetworkServiceIntent is null : " + (clientNetworkServiceIntent == null));

        ActivityPlaylistBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new InQueueSongAdapter(viewModel.getSessionModel().getPlayQueue(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);

        if (MainActivity.DESIGN_MODE) {
            viewModel.setSearchLink("https://www.youtube.com/watch?v=qT6XCvDUUsU");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clientNetworkServiceIntent != null) {
            stopService(clientNetworkServiceIntent);
            Log.d(LOG_TAG, "ClientNetworkService successfully stopped");
        }

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
                Dialog dialog = new Dialog(this);
                dialog.setCancelable(true);

                DialogAddYoutubeLinkBinding binding = DataBindingUtil.inflate(getLayoutInflater().from(this), R.layout.dialog_add_youtube_link, null, false);

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width = (int) (displaymetrics.widthPixels * 0.9);
                int height = (int) (displaymetrics.heightPixels * 0.7);
                dialog.getWindow().setLayout(width, height);

                dialog.setContentView(binding.getRoot());
                binding.setViewModel(viewModel);
                dialog.show();
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

    public void showHistory() {
        Log.d(LOG_TAG, "History button pressed from Playlist Activity");
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
    }

    private void leaveSession() {
        Log.d(LOG_TAG, "Left the party by pressing the button");

        //todo technical: do what you must

        // unregister the client
        responseHandler.sendUnRegisterClient();

        this.finish();
    }

    @Override
    public void downVoteClicked(View v, int position) {
        SongModel songModel = viewModel.getSessionModel().getPlayQueue().get(position);
        //todo technical: transform songModel to song
        // send downvote request
        responseHandler.sendCastSkipSongVote(null);
    }

    @Override
    public void addSongClicked(View v) {
        // get youtube url from view
        String youtubeUrl = viewModel.getSearchLink();
        viewModel.setSearchLink("");

        Log.d(LOG_TAG, "requesting URL: " + youtubeUrl);

        YoutubeDownloadUtility youtubeService = new YoutubeDownloadUtility(this);
        youtubeService.resolveSong(youtubeUrl, new SimpleListener<Integer, Song>() {
            @Override
            public void onEvent(Integer status, Song song) {
                Log.d(LOG_TAG, "resolved download url: " + song.getDownloadUrl());
                responseHandler.sendRequestSong(song);
            }
        });
    }
}
