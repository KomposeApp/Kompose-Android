package ch.ethz.inf.vs.kompose;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import java.net.Socket;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.databinding.ActivityConnectBinding;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.ClientNSDService;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.uncategorized.ClientRegistrationTask;
import ch.ethz.inf.vs.kompose.view.adapter.JoinSessionAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.JoinSessionViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.ConnectViewModel;

public class ConnectActivity extends BaseActivity implements JoinSessionViewHolder.ClickListener {

    private static final String LOG_TAG = "## Connect Activity";
    private final ConnectViewModel viewModel = new ConnectViewModel();
    private boolean clientNetworkServiceBound = false;
    private ClientRegistrationTask responseHandler = null;

    private ServiceConnection cNetServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(LOG_TAG, "ClientNSDService bound");
            ClientNSDService.LocalBinder binder = (ClientNSDService.LocalBinder) service;
            ClientNSDService clientNetworkService = binder.getService();
            clientNetworkServiceBound = true;
            clientNetworkService.findNetworkServices(viewModel.getSessionModels());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG_TAG, "ClientNSDService disconnected");
        }

        @Override
        public void onBindingDied(ComponentName arg0) {
            Log.w(LOG_TAG, "Binding with ClientNSDService died");
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
        binding.setViewModel(viewModel);

        //bind client network service --> Will start listening for hosts to connect to
        Intent intent = new Intent(this, ClientNSDService.class);
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
        if (responseHandler != null){
            responseHandler.cancel(true);
            responseHandler = null;
        }
    }

    @Override
    public void joinButtonClicked(View v, int position) {

        Log.d(LOG_TAG, "pressed join button of item number " + position);
        SessionModel pressedSession = viewModel.getSessionModels().get(position);
        String clientName = viewModel.getClientName();

        //Check whether the client's name is empty or null
        if (clientName == null || clientName.trim().isEmpty()) {
            showError(getString(R.string.choose_client_name));
            return;
        }

        // Remove trailing whitespace from username and set it in the singleton
        StateSingleton.getInstance().username = clientName.trim();

        // Set our active session. Note: Connection details have already been prepared beforehand.
        StateSingleton.getInstance().activeSession = pressedSession;

        Socket hostConnection = null;
        try {
            // Setting up the ServerSocket on the Client.
            responseHandler = new ClientRegistrationTask();

            // Send join request to the host.
            Log.d(LOG_TAG, "Sending a join request to the host");
            NetworkService networkService = new NetworkService();
            networkService.sendRegisterClient(clientName);

            // Listen for responses from the host. If we get a matching response, proceed to the playlist.
            responseHandler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            hostConnection = responseHandler.get();
            if (hostConnection == null) throw new Exception("Host connection was null.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to establish a connection with the host.");

            StateSingleton.getInstance().username = null;
            StateSingleton.getInstance().activeSession = null;
            Log.e(LOG_TAG, "Setting up the ServerSocket failed in ClientRegistrationTask.  Reason: " + e.getMessage());
            return;
        }

        //Reset responsehandler
        responseHandler = null;

        // In case we closed the activity while waiting for a connection, stop here.
        if (this.isDestroyed()){
            finish();
        }

        StateSingleton.getInstance().hostConnection = hostConnection;
        StateSingleton.getInstance().deviceIsHost = false;
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }
}
