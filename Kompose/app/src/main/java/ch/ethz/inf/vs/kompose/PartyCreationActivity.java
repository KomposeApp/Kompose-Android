package ch.ethz.inf.vs.kompose;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.service.SessionService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class PartyCreationActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Party Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_creation_placeholder);


        Intent gattServiceIntent = new Intent(this, SessionService.class);
        boolean isBound = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Log.d(LOG_TAG, "finished creation, bound service: " + isBound);
    }

    public void confirmParty(View v) {
        //TODO: Add party creation process before starting next activity
        Log.d(LOG_TAG, "Confirmation button pressed");

/*
        // start a session as host
        EditText editText = (EditText) findViewById(R.id.party_name_text_entry);
        String partyName = editText.getText().toString();
        repository.startSession(partyName, partyName);

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
*/
    }


    SessionService serverService;

    //service connection
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            serverService = (SessionService) ((BaseService.LocalBinder) service).getService();
            serverService.startSession("my session", "client name");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        //register for events
        final IntentFilter intentFilter = new IntentFilter();

        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    //the receiver
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
        }
    };
}
