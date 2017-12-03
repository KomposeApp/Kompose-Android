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

import java.io.IOException;
import java.net.SocketException;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityConnectBinding;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.client.ClientNetworkService;
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
      Note on unbinding the ClientNetworkService:
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

        // Insert default username
        viewModel.setClientName(StateSingleton.getInstance().getPreferenceUtility().getCurrentUsername());

        //get binding & bind viewmodel to view
        ActivityConnectBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_connect);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new JoinSessionAdapter(viewModel.getSessionModels(), getLayoutInflater(), this));
        binding.setViewModel(viewModel);

        //bind client network service
        Intent intent = new Intent(this.getBaseContext(), ClientNetworkService.class);
        bindService(intent, cNetServiceConnection, BIND_AUTO_CREATE);

        //TODO: Remove once done testing
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

        //TODO: Is adding ourselves (the client) to the session required for the app to function properly?
        //TODO: Does the client even need to know about what other clients there are?
        // Add ourselves to the current session locally
        UUID deviceUUID = StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID();
        ClientModel clientModel = new ClientModel(deviceUUID, pressedSession);
        clientModel.setName(clientName);
        clientModel.setIsActive(true);
        pressedSession.getClients().add(clientModel);

        StateSingleton.getInstance().setActiveSession(pressedSession);
        //TODO: Check whether storing the active client is really a necessity (prefer doing everything through preferences)
        StateSingleton.getInstance().setActiveClient(clientModel);
        try {
            if (!clientNetworkServiceBound && clientNetworkService == null)
                throw new IllegalStateException("Failed to properly set up Client Network Service");

            RegistrationListener listener = new RegistrationListener(this);
            clientNetworkService.initSocketListener();
            clientNetworkService.registerClientOnHost(listener, clientName);
            //TODO: DESIGN: Display visual elements while registration is ongoing
        } catch(SocketException s){
            s.printStackTrace();
            try {
                clientNetworkService.closeClientSocket();
            } catch (IOException e) {
                Log.w(LOG_TAG, "Cleaning up the Client Socket failed.");
                e.printStackTrace();
            }
            showError("Failed to set up connection.");
            StateSingleton.getInstance().setActiveSession(null);
        }
        catch (IllegalStateException | IOException io) {
            io.printStackTrace();
            showError("Failed to set up connection.");
            StateSingleton.getInstance().setActiveSession(null);
        }
    }

    /**
     * Listener which allows us to start the next Activity.
     * Always make sure this is started on the main thread.
     * Needs to close the ServerSocket in the client in case the connection fails
     */
    private class RegistrationListener implements SimpleListener<Boolean, Void> {

        private ConnectActivity connectActivity;

        private RegistrationListener(ConnectActivity connectActivity) {
            this.connectActivity = connectActivity;
        }

        @Override
        public void onEvent(Boolean success, Void v) {
            try {
                //TODO: Check whether closing the socket works as intended
                if (!success) {
                    Log.e(LOG_TAG, "Failed to establish connection with host");
                    if (!connectActivity.isDestroyed()) {
                        connectActivity.showError(getString(R.string.view_error_connection_failed));
                    }
                    StateSingleton.getInstance().setActiveSession(null);
                    clientNetworkService.closeClientSocket();
                    return;
                }

                // In case we closed the activity while waiting for a connection, stop here.
                if (connectActivity.isDestroyed()) {
                    StateSingleton.getInstance().setActiveSession(null);
                    clientNetworkService.closeClientSocket();
                    return;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Closing the Client Server Socket failed");
            }
            // start the client service again -- THIS IS INTENTIONAL
            // it will keep the service alive across different activities.
            Intent serverIntent = new Intent(connectActivity, ClientNetworkService.class);
            connectActivity.startService(serverIntent);

            // Initialize Playlist
            Intent playlistIntent = new Intent(connectActivity, PlaylistActivity.class);
            playlistIntent.putExtra(MainActivity.KEY_NETWORKSERVICE, serverIntent);
            connectActivity.startActivity(playlistIntent);
            connectActivity.finish();
        }
    }
}

