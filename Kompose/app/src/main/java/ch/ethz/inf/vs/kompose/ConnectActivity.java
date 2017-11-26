package ch.ethz.inf.vs.kompose;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import ch.ethz.inf.vs.kompose.databinding.ActivityConnectBinding;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.ClientNetworkService;
import ch.ethz.inf.vs.kompose.view.adapter.ClientAdapter;
import ch.ethz.inf.vs.kompose.view.adapter.SessionSelectAdapter;
import ch.ethz.inf.vs.kompose.view.adapter.recycler.ClickListeners;
import ch.ethz.inf.vs.kompose.view.viewmodel.ConnectViewModel;

public class ConnectActivity extends AppCompatActivity implements ClickListeners {

    private static final String LOG_TAG = "## Connect Activity";
    private ClientNetworkService clientNetworkService;
    private boolean clientNetworkServiceBound = false;

    private final ConnectViewModel viewModel = new ConnectViewModel(new ObservableArrayList<SessionModel>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);


        ActivityConnectBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_connect);


        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(new SessionSelectAdapter(viewModel.getSessionModels(), getLayoutInflater(), this));

        binding.setViewModel(viewModel);


        //bind client network service
        Intent intent = new Intent(this, ClientNetworkService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
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
    public void recyclerViewListClicked(View v, int position) {
        //ignore
    }

    @Override
    public void buttonClicked(View v, int position) {
        //join button clicked
        SessionModel pressedSession = viewModel.getSessionModels().get(position);
        String clientName = viewModel.getClientName();
        //todo: join session
    }
}
