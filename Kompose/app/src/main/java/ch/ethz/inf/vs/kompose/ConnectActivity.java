package ch.ethz.inf.vs.kompose;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import java.net.SocketException;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityConnectBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.ClientNetworkService;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.view.adapter.JoinSessionAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.JoinSessionViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.ConnectViewModel;

public class ConnectActivity extends BaseActivity implements JoinSessionViewHolder.ClickListener {

    private static final String LOG_TAG = "## Connect Activity";
    private final ConnectViewModel viewModel = new ConnectViewModel();
    private ClientNetworkService clientNetworkService;
    private boolean clientNetworkServiceBound = false;

    /*
      Note on unbinding the service started here:
        * If it occurs before we join a room, it simply kills the service and the NSD Listener
        * If it occurs after we join a room, it will simply disconnect the service from this
          activity, and live on. This service will then be killed at some other point.
     */
    private ServiceConnection cNetServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(LOG_TAG, "ClientNetworkService bound");
            ClientNetworkService.LocalBinder binder = (ClientNetworkService.LocalBinder) service;
            clientNetworkService = binder.getService();
            clientNetworkServiceBound = true;
            clientNetworkService.findNetworkServices(viewModel.getSessionModels());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG_TAG, "ClientNetworkService died");
            clientNetworkService = null;
        }

        @Override
        public void onBindingDied(ComponentName arg0) {
            Log.w(LOG_TAG, "Binding with ClientNetworkService died");
            clientNetworkService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //get binding & bind viewmodel to view
        ActivityConnectBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_connect);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new JoinSessionAdapter(viewModel.getSessionModels(), getLayoutInflater(), this));
        binding.setTitle("create a shared playlist with friends");
        binding.setViewModel(viewModel);

        //bind client network service
        Intent intent = new Intent(this.getBaseContext(), ClientNetworkService.class);
        bindService(intent, cNetServiceConnection, BIND_AUTO_CREATE);

        if (MainActivity.DESIGN_MODE) {
            SampleService sampleService = new SampleService();
            viewModel.getSessionModels().add(sampleService.getSampleSession("design session"));
            viewModel.getSessionModels().add(sampleService.getSampleSession("design session 1"));
            viewModel.getSessionModels().add(sampleService.getSampleSession("design session 2"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientNetworkServiceBound) {
            unbindService(cNetServiceConnection);
            clientNetworkServiceBound = false;
        }
    }

    @Override
    public void joinButtonClicked(View v, int position) {
        Log.d(LOG_TAG, "pressed join button of item number " + position);

        SessionModel pressedSession = viewModel.getSessionModels().get(position);
        String clientName = viewModel.getClientName();

        // Client's name must not be empty
        if (clientName == null || clientName.trim().isEmpty()) {
            showError(getString(R.string.view_error_clientname));
            return;
        }
        clientName = clientName.trim();

        // Add ourselves to the current session locally
        ClientModel clientModel = new ClientModel(StateSingleton.getInstance().deviceUUID, pressedSession);
        clientModel.setName(clientName);
        clientModel.setIsActive(true);
        pressedSession.getClients().add(clientModel);

        StateSingleton.getInstance().activeSession = pressedSession;
        StateSingleton.getInstance().currentUsername  = clientName;
        try {
            if (!clientNetworkServiceBound && clientNetworkService == null)
                throw new IllegalStateException("Failed to properly set up Client Network Service");

            RegistrationListener listener = new RegistrationListener(this);
            clientNetworkService.initSocketListener();
            clientNetworkService.registerClientOnHost(listener, clientName);
        } catch (IllegalStateException | SocketException e) {
            e.printStackTrace();
            showError("Failed to set up connection.");
            StateSingleton.getInstance().activeSession = null;
        }
    }

    private class RegistrationListener implements SimpleListener<Boolean, Void> {

        private ConnectActivity connectActivity;

        public RegistrationListener(ConnectActivity connectActivity) {
            this.connectActivity = connectActivity;
        }

        @Override
        public void onEvent(Boolean success, Void v) {
            if (!success) {
                Log.e(LOG_TAG, "Failed to establish connection with host");
                if (!connectActivity.isDestroyed())
                    connectActivity.showError(getString(R.string.view_error_connection_failed));
                StateSingleton.getInstance().activeSession = null;
                return;
            }

            // In case we closed the activity while waiting for a connection, stop here.
            if (connectActivity.isDestroyed()) {
                StateSingleton.getInstance().activeSession = null;
                connectActivity.finish();
                return;
            }

            // start the client service again -- THIS IS INTENTIONAL
            // it will keep the service alive across different activities.
            Intent serverIntent = new Intent(connectActivity, ClientNetworkService.class);
            connectActivity.startService(serverIntent);

            Intent playlistIntent = new Intent(connectActivity, PlaylistActivity.class);
            playlistIntent.putExtra(MainActivity.KEY_CNETWORKSERVICE, serverIntent);

            connectActivity.startActivity(playlistIntent);
            connectActivity.finish();
        }
    }
}

