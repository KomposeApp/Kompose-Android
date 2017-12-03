package ch.ethz.inf.vs.kompose;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityPlaylistBinding;
import ch.ethz.inf.vs.kompose.databinding.DialogAddYoutubeLinkBinding;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.AudioService;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.SongRequestListener;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;
import ch.ethz.inf.vs.kompose.view.adapter.InQueueSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.InQueueSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.PlaylistViewModel;

public class PlaylistActivity extends BaseActivity implements InQueueSongViewHolder.ClickListener, PlaylistViewModel.ClickListener {

    private static final String LOG_TAG = "## Playlist Activity";

    private final PlaylistViewModel viewModel = new PlaylistViewModel(StateSingleton.getInstance().getActiveSession(), this);
    private Dialog songRequestDialog;

    private OutgoingMessageHandler responseHandler;

    // Audio Service
    private AudioService audioService;
    private boolean audioServiceBound = false;

    /* Intents of Services originating from the preceeding Activities
     * Only one of the two should be non-null at any given point
    */
    private Intent clientNetworkServiceIntent;
    private Intent hostServerServiceIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        if (MainActivity.DESIGN_MODE) {
            viewModel.setSearchLink("https://www.youtube.com/watch?v=qT6XCvDUUsU");
            SampleService sampleService = new SampleService();
            sampleService.fillSampleSession(viewModel.getSessionModel());
        }

        Intent intent = getIntent();
        responseHandler = new OutgoingMessageHandler(this);
        hostServerServiceIntent = intent.getParcelableExtra(MainActivity.KEY_SERVERSERVICE);
        clientNetworkServiceIntent = intent.getParcelableExtra(MainActivity.KEY_NETWORKSERVICE);

        if(((hostServerServiceIntent==null) == (clientNetworkServiceIntent== null))){
            throw new IllegalStateException("Application managed to simultaneously be host and client, or neither.");
        }

        ActivityPlaylistBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new InQueueSongAdapter(viewModel.getSessionModel().getPlayQueue(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // bind to audio service
        if (StateSingleton.getInstance().getActiveSession().getIsHost()) {
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

    private void resolveAndRequestSong(String youtubeUrl) {
        Log.d(LOG_TAG, "requesting URL: " + youtubeUrl);
        SessionModel activeSession = StateSingleton.getInstance().getActiveSession();

        //set session to active if host
        if (activeSession.getIsHost() && activeSession.getSessionStatus().equals(SessionStatus.WAITING)) {
            activeSession.setSessionStatus(SessionStatus.ACTIVE);
        }

        YoutubeDownloadUtility youtubeService = new YoutubeDownloadUtility(this);

        youtubeService.resolveSong(youtubeUrl, activeSession,
                StateSingleton.getInstance().getActiveClient(), new SongRequestListener(this));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clientNetworkServiceIntent != null) {
            stopService(clientNetworkServiceIntent);
            Log.d(LOG_TAG, "ClientNetworkService successfully stopped");
        }
        if (audioServiceBound) {
            unbindService(audioServiceConnection);
            audioServiceBound = false;
        }
        StateSingleton.getInstance().setActiveSession(null);
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


}
