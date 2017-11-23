package ch.ethz.inf.vs.kompose;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Observable;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.ClientNetworkService;

public class ConnectActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Connect Activity";
    private ClientNetworkService clientNetworkService;
    private boolean clientNetworkServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_placeholder);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ClientNetworkService.class);
        bindService(intent, connection, this.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ClientNetworkService.LocalBinder binder = (ClientNetworkService.LocalBinder) service;
            clientNetworkService = binder.getService();
            clientNetworkServiceBound = true;

            ObservableArrayList<SessionModel> networkSessions = new ObservableArrayList<>();
            clientNetworkService.findNetworkServices(networkSessions);

            // TODO
            // bind to view
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            clientNetworkServiceBound = false;
        }
    };

    public void connect(View v) {

//        //TODO: resolve pressed session and set client name
//        String username = PreferenceManager.getDefaultSharedPreferences(this)
//                .getString(BasePreferencesService.KEY_USERNAME, BasePreferencesService.DEFAULT_USERNAME);
//        getSessionService().joinSession(null, username);
//
//        Log.d(LOG_TAG, "Connect button pressed");
//        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
//        startActivity(playlistIntent);
//        this.finish();
    }
}
