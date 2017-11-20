package ch.ethz.inf.vs.kompose;

import android.bluetooth.BluetoothGattService;
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

import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.service.AndroidServerService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class ConnectActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Connect Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_placeholder);

        Intent gattServiceIntent = new Intent(this, AndroidServerService.class);
        boolean isBound = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Log.d(LOG_TAG, "finished creation, bound service: " + isBound);
    }

    public void connect(View v) {
        //TODO: Add connection logic before starting the next activity
        Log.d(LOG_TAG, "Connect button pressed");
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }


    AndroidServerService serverService;

    //service connection
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            serverService = (AndroidServerService) ((BaseService.LocalBinder) service).getService();
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
