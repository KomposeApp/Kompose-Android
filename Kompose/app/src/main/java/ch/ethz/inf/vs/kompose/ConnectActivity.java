package ch.ethz.inf.vs.kompose;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.databinding.ActivityConnectBinding;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.ClientNetworkService;
import ch.ethz.inf.vs.kompose.service.SampleService;
import ch.ethz.inf.vs.kompose.view.adapter.JoinSessionAdapter;
import ch.ethz.inf.vs.kompose.view.viewholder.JoinSessionViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.ConnectViewModel;

public class ConnectActivity extends AppCompatActivity implements JoinSessionViewHolder.ClickListener {

    private static final String LOG_TAG = "## Connect Activity";
    private ClientNetworkService clientNetworkService;
    private boolean clientNetworkServiceBound = false;

    private final ConnectViewModel viewModel = new ConnectViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //get binding & bind viewmodel to view
        ActivityConnectBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_connect);

        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new JoinSessionAdapter(viewModel.getSessionModels(), getLayoutInflater(), this));

        binding.setViewModel(viewModel);


        //bind client network service
        Intent intent = new Intent(this, ClientNetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        if (MainActivity.DESIGN_MODE) {
            SampleService sampleService = new SampleService();
            viewModel.getSessionModels().add(sampleService.getSampleSession("my session"));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ClientNetworkService.LocalBinder binder = (ClientNetworkService.LocalBinder) service;
            clientNetworkService = binder.getService();
            clientNetworkServiceBound = true;
            clientNetworkService.findNetworkServices(viewModel.getSessionModels());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            clientNetworkServiceBound = false;
        }
    };


//    /**
//     * join the active session as a client with the specified name
//     *
//     * @param clientName the name to use
//     */
//    private void joinActiveSession(String clientName) {
//        activeClient = new ClientModel(getDeviceUUID(), activeSessionModel);
//        activeClient.setName(clientName);
//        activeClient.setIsActive(true);
//
//        activeSessionModel.getClients().add(activeClient);
//
//        SessionConverter sessionConverter = new SessionConverter();
//        activeSession = sessionConverter.convert(activeSessionModel);
//
//        broadcastConnectionChanged();
//    }
//
//    /**
//     * gets all currently active sessions in the network
//     *
//     * @return collection of all active sessions
//     */
//    public ObservableList<SessionModel> getActiveSessions() {
//        ObservableArrayList<SessionModel> observableArrayList = new ObservableArrayList<>();
//        getClientNetworkService().findNetworkServices(observableArrayList);
//        return observableArrayList;
//    }
//
//    /**
//     * join one of the session previously retrieved by getActiveSessions
//     *
//     * @param session the session you want to join
//     */
//    public void joinSession(SessionModel session, String clientName) {
//        isHost = false;
//
//        activeSessionModel = session;
//        joinActiveSession(clientName);
//
//        getNetworkService().sendRegisterClient(clientName);
//    }

    public void connect(View v) {
//        String username = PreferenceManager.getDefaultSharedPreferences(this)
//                .getString(BasePreferencesService.KEY_USERNAME, BasePreferencesService.DEFAULT_USERNAME);
//        getSessionService().joinSession(null, username);
//
//        Log.d(LOG_TAG, "Connect button pressed");
//        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
//        startActivity(playlistIntent);
//        this.finish();
    }

    @Override
    public void joinButtonClicked(View v, int position) {
        Log.d(LOG_TAG, "pressed join button of item number " + position);
        SessionModel pressedSession = viewModel.getSessionModels().get(position);
        String clientName = viewModel.getClientName();
    }
}
