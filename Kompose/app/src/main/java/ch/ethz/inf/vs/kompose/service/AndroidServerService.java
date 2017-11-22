package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.service.base.BaseService;
import ch.ethz.inf.vs.kompose.service.base.BasePreferencesService;
import ch.ethz.inf.vs.kompose.service.handler.MessageHandler;

/**
 * Android service that starts the server.
 * First the service is registered on the network, then an AsyncTask
 * that accepts connections is launched.
 */
public class AndroidServerService extends BasePreferencesService {

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
        bindBaseService(SessionService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "started");

        try {
            serverSocket = new ServerSocket(getCurrentPort());
            localPort = serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // register network service
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);

        String sessionName = getSessionService().getActiveSessionModel().getSessionName();
        String uuid = getSessionService().getActiveSessionModel().getUuid().toString();
        String hostUuid = getSessionService().getActiveSessionModel().getHostUUID().toString();

        sessionName = sessionName.substring(0, 255);
        uuid = uuid.substring(0, 255);
        hostUuid = hostUuid.substring(0, 255);

        serviceInfo.setAttribute("session", sessionName);
        serviceInfo.setAttribute("uuid", uuid);
        serviceInfo.setAttribute("host_uuid", hostUuid);

        Log.d(LOG_TAG, "Using port: " + localPort);
        serviceInfo.setPort(localPort);

        nsdRegistrationListener = new ServerRegistrationListener();
        nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener);

        // start server task
        serverTask = new ServerTask();
        serverTask.execute();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nsdManager.unregisterService(nsdRegistrationListener);
        serverTask.cancel(true);
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

                    MessageHandler messageHandler = new MessageHandler(getSessionService(), connection);
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
