package ch.ethz.inf.vs.kompose.service.base;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Network;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import ch.ethz.inf.vs.kompose.service.DownloadService;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.SessionService;
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


    public void bindService(Class service) {
        Intent gattServiceIntent = new Intent(this, service);
        boolean isBound = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Log.d(LOG_TAG, "finished creation, bound service: " + isBound);
    }


    private NetworkService networkService;
    private StorageService storageService;
    private DownloadService downloadService;
    private SessionService sessionService;

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
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onDestroy() {
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
