package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;
import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;

/**
 * Android service that starts the server.
 * First the service is registered on the network, then an AsyncTask
 * that accepts connections is started.
 */
public class AndroidServerService extends Service {

    private static final String LOG_TAG = "## AndroidServerService";

    public static final String FOUND_SERVICE = "AndroidServerService.FOUND_SERVICE";
    private static final String SERVICE_NAME = "Kompose";
    private static final String SERVICE_TYPE = "_kompose._tcp";

    private ServerSocket serverSocket;
    private int localPort;
    private String serviceName;
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener nsdRegistrationListener;
    private ServerTask serverTask;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "started");

        try {
            serverSocket = new ServerSocket(PreferenceUtility.getCurrentPort(this));
            localPort = serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "SOCKET ERROR FOUND, FIX THIS DEVS");
            stopSelf();
            return START_STICKY;
        }

        // register network service
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);

        SessionModel activeSession = StateSingleton.getInstance().activeSession;
        String sessionName = activeSession.getName();
        String uuid = activeSession.getUUID().toString();
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
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener);

        // start server task
        serverTask = new ServerTask();
        serverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "destroyed");
        nsdManager.unregisterService(nsdRegistrationListener);
        serverTask.cancel(true);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(LOG_TAG, "task removed");
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ServerRegistrationListener implements NsdManager.RegistrationListener {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            serviceName = NsdServiceInfo.getServiceName();

            Intent intent = new Intent(FOUND_SERVICE);
            intent.putExtra("info", NsdServiceInfo);
            sendBroadcast(intent);

            Log.d(LOG_TAG, "Service registered: " + serviceName);
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(LOG_TAG, "Service registration failed: " + errorCode);
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
            Log.d(LOG_TAG, "Service unregistered: " + serviceName);
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(LOG_TAG, "Service unregistration failed: " + errorCode);
        }
    }

    private class ServerTask extends AsyncTask<Void, Void, Void> {

        private static final String LOG_TAG = "## ServerTask";

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(LOG_TAG, "Server ready to receive connections");

            while (!this.isCancelled()) {
                try {
                    final Socket connection = serverSocket.accept();

                    Log.d(LOG_TAG, "message received");

                    IncomingMessageHandler messageHandler = new IncomingMessageHandler(connection);
                    Thread msgHandler = new Thread(messageHandler);
                    msgHandler.start();
                } catch (Exception e) {
                    Log.d(LOG_TAG, "could not process message; exception occurred! " + e.toString());
                }
            }
            return null;
        }
    }
}
