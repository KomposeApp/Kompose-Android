package ch.ethz.inf.vs.kompose.service.base;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import ch.ethz.inf.vs.kompose.service.ClientService;
import ch.ethz.inf.vs.kompose.service.DownloadService;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.SessionService;
import ch.ethz.inf.vs.kompose.service.SongService;
import ch.ethz.inf.vs.kompose.service.StorageService;

/**
 * Created by git@famoser.ch on 20/11/2017.
 */

public abstract class BaseService extends Service {
    private final static String LOG_TAG = "## BaseService";

    protected NetworkService getNetworkService() {
        return networkService;
    }

    protected StorageService getStorageService() {
        return storageService;
    }

    protected DownloadService getDownloadService() {
        return downloadService;
    }

    protected SessionService getSessionService() {
        return sessionService;
    }

    protected ClientService getClientService() {
        return clientService;
    }

    protected SongService getSongService() {
        return songService;
    }


    public class LocalBinder extends Binder {
        public BaseService getService() {
            return BaseService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private final IBinder mBinder = new LocalBinder();

    protected void bindService(Class service) {
        Intent gattServiceIntent = new Intent(this, service);
        boolean isBound = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Log.d(LOG_TAG, "finished creation, bound service: " + isBound);
    }

    private boolean registeredReceiver = false;
    private IntentActionCallbackReceiver callbackReceiver;

    protected void subscribeToIntentActions(String[] intentActions, IntentActionCallbackReceiver callbackReceiver) {
        this.callbackReceiver = callbackReceiver;

        //register for events
        final IntentFilter intentFilter = new IntentFilter();
        for (String action : intentActions) {
            intentFilter.addAction(action);
        }
        registerReceiver(broadcastReceiver, intentFilter);
        registeredReceiver = true;
    }


    private NetworkService networkService;
    private StorageService storageService;
    private DownloadService downloadService;
    private SessionService sessionService;
    private ClientService clientService;
    private SongService songService;

    //service connection
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BaseService baseService = ((BaseService.LocalBinder) service).getService();
            if (baseService instanceof NetworkService) {
                networkService = (NetworkService) baseService;
            } else if (baseService instanceof StorageService) {
                storageService = (StorageService) baseService;
            } else if (baseService instanceof DownloadService) {
                downloadService = (DownloadService) baseService;
            } else if (baseService instanceof SessionService) {
                sessionService = (SessionService) baseService;
            } else if (baseService instanceof ClientService) {
                clientService = (ClientService) baseService;
            } else if (baseService instanceof SongService) {
                songService = (SongService) baseService;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (registeredReceiver) {
            unregisterReceiver(broadcastReceiver);
        }
        unbindService(mServiceConnection);
    }

    //the receiver
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
