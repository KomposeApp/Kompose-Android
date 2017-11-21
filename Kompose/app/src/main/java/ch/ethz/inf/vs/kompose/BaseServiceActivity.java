package ch.ethz.inf.vs.kompose;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ch.ethz.inf.vs.kompose.service.ClientService;
import ch.ethz.inf.vs.kompose.service.SessionService;
import ch.ethz.inf.vs.kompose.service.SongService;
import ch.ethz.inf.vs.kompose.service.YoutubeService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;


public abstract class BaseServiceActivity extends AppCompatActivity {

    private final static String LOG_TAG = "## BaseServiceActivity ";

    protected SessionService getSessionService() {
        return sessionService;
    }

    protected ClientService getClientService() {
        return clientService;
    }

    protected SongService getSongService() {
        return songService;
    }

    protected YoutubeService getYoutubeService() {
        return youtubeService;
    }

    protected void bindBaseService(Class service) {
        Intent gattServiceIntent = new Intent(this, service);
        boolean isBound = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        String serviceDescription = service.getName() + " to " + this.getClass().getName();
        if (isBound) {
            Log.d(LOG_TAG, "successfully bound " + serviceDescription);
        } else {
            Log.d(LOG_TAG, "failed to bind " + serviceDescription);
        }
    }

    private SessionService sessionService;
    private YoutubeService youtubeService;
    private ClientService clientService;
    private SongService songService;

    //service connection
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BaseService baseService = ((BaseService.LocalBinder) service).getService();
            if (baseService instanceof SessionService) {
                sessionService = (SessionService) baseService;
            } else if (baseService instanceof ClientService) {
                clientService = (ClientService) baseService;
            } else if (baseService instanceof SongService) {
                songService = (SongService) baseService;
            } else if (baseService instanceof YoutubeService) {
                youtubeService = (YoutubeService) baseService;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private boolean registeredReceiver = false;
    private BaseServiceActivity.IntentActionCallbackReceiver callbackReceiver;
    private String[] intentActions;

    protected void subscribeToIntentActions(String[] intentActions, BaseServiceActivity.IntentActionCallbackReceiver callbackReceiver) {
        this.callbackReceiver = callbackReceiver;
        this.intentActions = intentActions;

        refreshIntentActions();
    }

    protected void refreshIntentActions() {
        if (intentActions != null && callbackReceiver != null) {
            //register for events
            final IntentFilter intentFilter = new IntentFilter();
            for (String action : intentActions) {
                intentFilter.addAction(action);
            }
            registerReceiver(broadcastReceiver, intentFilter);
            registeredReceiver = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshIntentActions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (registeredReceiver) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    //relay the intent callback to the correct receiver
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            callbackReceiver.intentActionReceived(intent.getAction(), intent);
        }
    };

    public interface IntentActionCallbackReceiver {
        void intentActionReceived(String action, Intent intent);
    }
}
