package ch.ethz.inf.vs.kompose;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityPlaylistBinding;
import ch.ethz.inf.vs.kompose.databinding.DialogAddYoutubeLinkBinding;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.AudioService;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;
import ch.ethz.inf.vs.kompose.view.adapter.InQueueSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.InQueueSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.PlaylistViewModel;

public class PlaylistActivity extends BaseActivity implements InQueueSongViewHolder.ClickListener, PlaylistViewModel.ClickListener, SimpleListener<Integer, SongModel> {

    private static final String LOG_TAG = "## Playlist Activity";
    private OutgoingMessageHandler responseHandler;

    private final PlaylistViewModel viewModel = new PlaylistViewModel(StateSingleton.getInstance().activeSession, this);
    private Intent clientNetworkServiceIntent;

    private Dialog songRequestDialog;

    private AudioService audioService;
    private boolean audioServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        // share links
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }

        if (MainActivity.DESIGN_MODE) {
            viewModel.setSearchLink("https://www.youtube.com/watch?v=qT6XCvDUUsU");
            SampleService sampleService = new SampleService();
            sampleService.fillSampleSession(viewModel.getSessionModel());
        }

        responseHandler = new OutgoingMessageHandler();
        clientNetworkServiceIntent = this.getIntent().getParcelableExtra(MainActivity.KEY_CNETWORKSERVICE);
        Log.d(LOG_TAG, "Client NetworkServiceIntent is null : " + (clientNetworkServiceIntent == null));

        ActivityPlaylistBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new InQueueSongAdapter(viewModel.getSessionModel().getPlayQueue(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // bind to audio service
        if (StateSingleton.getInstance().activeSession.getIsHost()) {
            Log.d(LOG_TAG, "binding AudioService");
            Intent audioServiceIntent = new Intent(this.getBaseContext(), AudioService.class);
            bindService(audioServiceIntent, audioServiceConnection, BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection audioServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(LOG_TAG, "AudioService connected");
            audioServiceBound = true;
            audioService = ((AudioService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(LOG_TAG, "AudioService disconnected");
            audioServiceBound = false;
        }
    };

    private void handleSendText(Intent intent) {
        SessionModel activeSession = StateSingleton.getInstance().activeSession;
        if (activeSession == null) {
            showError("Not connected to a session!");
            return;
        }
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            resolveAndRequestSong(sharedText);
        }
    }

    private void resolveAndRequestSong(String youtubeUrl) {
        Log.d(LOG_TAG, "requesting URL: " + youtubeUrl);
        SessionModel activeSession = StateSingleton.getInstance().activeSession;

        //set session to active if host
        if (activeSession.getIsHost() && activeSession.getSessionStatus().equals(SessionStatus.WAITING)) {
            activeSession.setSessionStatus(SessionStatus.ACTIVE);
        }

        YoutubeDownloadUtility youtubeService = new YoutubeDownloadUtility(this);

        youtubeService.resolveSong(youtubeUrl, activeSession, StateSingleton.getInstance().activeClient, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clientNetworkServiceIntent != null) {
            stopService(clientNetworkServiceIntent);
            Log.d(LOG_TAG, "ClientNetworkService successfully stopped");
        }
        unbindService(audioServiceConnection);
        audioServiceBound = false;
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
                songRequestDialog = new Dialog(this);
                songRequestDialog.setCancelable(true);

                DialogAddYoutubeLinkBinding binding = DataBindingUtil.inflate(
                        getLayoutInflater().from(this), R.layout.dialog_add_youtube_link,
                        null, false);

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width = (int) (displaymetrics.widthPixels * 0.9);
                int height = (int) (displaymetrics.heightPixels * 0.7);
                songRequestDialog.getWindow().setLayout(width, height);

                songRequestDialog.setContentView(binding.getRoot());
                binding.setViewModel(viewModel);
                songRequestDialog.show();
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
        // unregister the client
        responseHandler.sendUnRegisterClient();
        this.finish();
    }

    @Override
    public void downVoteClicked(View v, int position) {
        SongModel songModel = viewModel.getSessionModel().getPlayQueue().get(position);

        if (songModel.getSkipVoteCasted()) {
            responseHandler.sendRemoveSkipSongVote(songModel);
        } else {
            responseHandler.sendCastSkipSongVote(songModel);
        }
    }

    @Override
    public void addSongClicked(View v) {
        String youtubeUrl = viewModel.getSearchLink();
        viewModel.setSearchLink("");
        songRequestDialog.dismiss();
        resolveAndRequestSong(youtubeUrl);
    }


    @Override
    public void playClicked(View v) {
        if (audioServiceBound) {
            audioService.startPlaying();
        }
    }

    @Override
    public void pauseClicked(View v) {
        if (audioServiceBound) {
            audioService.stopPlaying();
        }
    }

    @Override
    public void onEvent(Integer status, SongModel value) {
        if (status == YoutubeDownloadUtility.RESOLVE_SUCCESS) {
            Log.d(LOG_TAG, "resolved download url: " + value.getDownloadUrl());
            responseHandler.sendRequestSong(value);
        } else {
            Log.e(LOG_TAG, "resolving url failed");
            showError("Failed to resolve Youtube URL");
        }
    }
}
