package ch.ethz.inf.vs.kompose;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityPlaylistBinding;
import ch.ethz.inf.vs.kompose.databinding.DialogAddYoutubeLinkBinding;
import ch.ethz.inf.vs.kompose.databinding.DialogHostInfoBinding;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.audio.AudioService;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;
import ch.ethz.inf.vs.kompose.service.audio.SongResolveHandler;
import ch.ethz.inf.vs.kompose.service.host.HostServerService;
import ch.ethz.inf.vs.kompose.view.adapter.InQueueSongAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.InQueueSongViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.PlaylistViewModel;

public class PlaylistActivity extends BaseActivity implements InQueueSongViewHolder.ClickListener, PlaylistViewModel.ClickListener {

    private static final String LOG_TAG = "## Playlist Activity";
    private final PlaylistViewModel viewModel = new PlaylistViewModel(StateSingleton.getInstance().getActiveSession(), this);
    private Dialog songRequestDialog;
    private OutgoingMessageHandler responseHandler;

    /* Intent of Service originating from the preceeding Activities*/
    private Intent clientNetworkServiceIntent;

    // Audio Service
    private AudioService audioService;
    private boolean audioServiceBound = false;

    // Host Service
    private HostServerService hostService;
    private boolean hostServiceBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Signals to ShareActivity that the playlist is active and songs can be requested
        StateSingleton.getInstance().setPlaylistIsActive(true);

        Intent intent = getIntent();
        responseHandler = new OutgoingMessageHandler(this);
        clientNetworkServiceIntent = intent.getParcelableExtra(MainActivity.KEY_NETWORKSERVICE);

        ActivityPlaylistBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_playlist);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new InQueueSongAdapter(viewModel.getSessionModel().getPlayQueue(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);

        findViewById(R.id.currently_playing_title).setSelected(true);

        // setup toolbar
        Toolbar mainToolbar = findViewById(R.id.kompose_toolbar_playlist);
        setSupportActionBar(mainToolbar);

        // bind to host and audio service
        if (StateSingleton.getInstance().getActiveSession().getIsHost()) {
            Log.d(LOG_TAG, "binding AudioService");
            Intent audioServiceIntent = new Intent(this.getBaseContext(), AudioService.class);
            bindService(audioServiceIntent, audioServiceConnection, BIND_AUTO_CREATE);

            Log.d(LOG_TAG, "binding HostServerService");
            Intent hostServerIntent = new Intent(this.getBaseContext(), HostServerService.class);
            bindService(hostServerIntent, hostServiceConnection, BIND_AUTO_CREATE);
        }
        else{

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // ShareActivity should no longer recognize us at this point
        StateSingleton.getInstance().setPlaylistIsActive(false);

        responseHandler.sendUnRegisterClient();

        if (clientNetworkServiceIntent != null) {
            Log.d(LOG_TAG, "Stopping ClientNetworkService");
            stopService(clientNetworkServiceIntent);
        }
        if (hostServiceBound) {
            Log.d(LOG_TAG, "Stopping HostServerService");
            unbindService(hostServiceConnection);
        }
        if (audioServiceBound) {
            unbindService(audioServiceConnection);
            audioServiceBound = false;
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
                songRequestDialog = new Dialog(this);
                songRequestDialog.setCancelable(true);

                DialogAddYoutubeLinkBinding binding = DataBindingUtil.inflate(
                        LayoutInflater.from(this), R.layout.dialog_add_youtube_link,
                        null, false);

                songRequestDialog.setContentView(binding.getRoot());
                binding.setViewModel(viewModel);
                songRequestDialog.show();
                return true;

            case R.id.leave_session:
                Log.d(LOG_TAG, "Left the party by pressing the button");
                finish();
                return true;

            case R.id.show_history:
                Log.d(LOG_TAG, "History button pressed from Playlist Activity");
                Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
                startActivity(historyIntent);
                return true;

            case R.id.host_info:
                Dialog hostInfoDialog = new Dialog(this);
                hostInfoDialog.setCancelable(true);
                DialogHostInfoBinding hostInfoBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(this), R.layout.dialog_host_info,
                        null, false);

                hostInfoDialog.setContentView(hostInfoBinding.getRoot());
                hostInfoBinding.setViewModel(viewModel);
                hostInfoDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void downVoteClicked(View v, int position) {
        SongModel songModel = viewModel.getSessionModel().getPlayQueue().get(position);
        toggleDownVote(songModel);
    }

    private void toggleDownVote(SongModel songModel) {
        if (songModel != null) {
            if (songModel.getSkipVoteCasted()) {
                responseHandler.sendRemoveSkipSongVote(songModel);
            } else {
                responseHandler.sendCastSkipSongVote(songModel);
            }
        }
    }

    @Override
    public void addSongClicked(View v) {
        String youtubeUrl = viewModel.getSearchLink();
        viewModel.setSearchLink("");
        songRequestDialog.dismiss();
        if (!SongResolveHandler.resolveAndRequestSong(this, youtubeUrl)){
            showError("Invalid URL");
        }
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
            audioService.pausePlaying();
        }
    }

    @Override
    public void downVoteCurrentlyClicked(View v) {
        toggleDownVote(viewModel.getSessionModel().getCurrentlyPlaying());
    }


    /**
     * Host Server Service Listener
     */
    private ServiceConnection hostServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(LOG_TAG, "HostServerService connected");
            hostServiceBound = true;
            hostService = ((HostServerService.LocalBinder) service).getService();
            hostService.startHostServices(new SimpleListener<Integer, String>(){

                @Override
                public void onEvent(Integer status, String error) {
                    Log.e(LOG_TAG, "There was an error setting up the HostServerService.");
                    Log.e(LOG_TAG, "Error code: " + status);
                    Log.e(LOG_TAG, error);
                    showError("Host services could not be set up properly.");
                    finish();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG_TAG, "HostServerService died");
            hostService = null;
        }

        @Override
        public void onBindingDied(ComponentName arg0) {
            Log.w(LOG_TAG, "HostServerService Binding died");
            hostService = null;
        }
    };
    /**
     * Audio Service Connection Listener
     */
    private ServiceConnection audioServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(LOG_TAG, "AudioService connected");
            audioServiceBound = true;
            audioService = ((AudioService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG_TAG, "AudioService died");
            audioService = null;
        }

        @Override
        public void onBindingDied(ComponentName arg0) {
            Log.w(LOG_TAG, "Binding died");
            audioService = null;
        }
    };
}
