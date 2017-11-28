package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;

public class HostNSDService extends Service {

    private static final String LOG_TAG = "## HostNSDService";
    public static final String FOUND_SERVICE = "AndroidServerService.FOUND_SERVICE";

    private static final String SERVICE_NAME = "Kompose";
    private static final String SERVICE_TYPE = "_kompose._tcp";

    private NsdManager nsdManager;
    private NsdManager.RegistrationListener nsdRegistrationListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return binder;}

    private IBinder binder = new HostNSDService.LocalBinder();

    public class LocalBinder extends Binder {
        public HostNSDService getService() {
            return HostNSDService.this;
        }
    }

    public void startBroadcast() throws RuntimeException{

        int localPort = StateSingleton.getInstance().hostPort;

        // register network service
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);

        SessionModel activeSession = StateSingleton.getInstance().activeSession;
        String sessionName = activeSession.getName();
        String uuid = activeSession.getUuid().toString();
        String hostUuid = activeSession.getHostUUID().toString();
        String hostName = PreferenceUtility.getCurrentUsername(this);

        sessionName = sessionName.substring(0, Math.min(255, sessionName.length()));
        uuid = uuid.substring(0, Math.min(255, uuid.length()));
        hostUuid = hostUuid.substring(0, Math.min(255, hostUuid.length()));

        serviceInfo.setAttribute("session", sessionName);
        serviceInfo.setAttribute("uuid", uuid);
        serviceInfo.setAttribute("host_uuid", hostUuid);
        serviceInfo.setAttribute("host_name", hostName);

        Log.d(LOG_TAG, "using port: " + localPort);
        serviceInfo.setPort(localPort);

        nsdRegistrationListener = new ServerRegistrationListener();
        nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);

        //TODO: Invalid port numbers can be specified here.
        if (nsdManager != null) {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener);
        } else{
            throw new RuntimeException("Failed to register the NSD Broadcasting Service");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Host NSD Service is being shut down");
        if (nsdManager != null) {
            nsdManager.unregisterService(nsdRegistrationListener);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(LOG_TAG, "task removed");
        //TODO: Find out what the hell this function does and what effects stopSelf() has on a bound service
        stopSelf();
    }

    private class ServerRegistrationListener implements NsdManager.RegistrationListener {

        @Override
        public void onServiceRegistered(NsdServiceInfo arg0) {
            Log.d(LOG_TAG, "Service registered: " + arg0.getServiceName());
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(LOG_TAG, "Failed to register the NSD Broadcasting Service. ErrorCode: " + errorCode);
            //TODO: Check whether this works correctly.
            nsdManager = null;
            stopSelf();
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
            Log.d(LOG_TAG, "Service unregistered: " + arg0.getServiceName());
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(LOG_TAG, "Service unregistration failed: " + errorCode);
        }
    }
}
