package ch.ethz.inf.vs.kompose.model;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Android service that starts the server.
 * First the service is registered on the network, then an AsyncTask
 * that accepts connections is launched.
 */
public class AndroidServerService extends Service {

    private static final String LOG_TAG = "## AndroidServerService";
    private static final String SERVICE_NAME = "Kompose";
    private static final String SERVICE_TYPE = "_kompose._tcp";

    private ServerSocket serverSocket;
    private int localPort;
    private String serviceName;
    private NsdManager nsdManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Service started");
        try {
            serverSocket = new ServerSocket(0);
            localPort = serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // register network service
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);

        Log.d(LOG_TAG, "Using port: " + localPort);
        serviceInfo.setPort(localPort);

        NsdManager.RegistrationListener registrationListener = new ServerRegistrationListener();
        nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

        // start server task
        ServerTask serverTask = new ServerTask();
        serverTask.execute();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ServerRegistrationListener implements NsdManager.RegistrationListener {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            serviceName = NsdServiceInfo.getServiceName();
            Log.d(LOG_TAG, "Service registered: " + serviceName);
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(LOG_TAG, "Service registration failed: " + errorCode);
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        }
    }

    private class ServerTask extends AsyncTask<Void,Void,Void> {

        private static final String LOG_TAG = "## ServerTask";

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(LOG_TAG, "Server ready to receive connections");

            while (!this.isCancelled()) {
                try {
                    Socket connection = serverSocket.accept();
                    ServerMessageHandler severMessageHandler = new ServerMessageHandler(connection);
                    Thread msgHandler = new Thread(severMessageHandler);
                    msgHandler.start();
                } catch (Exception e) {
                }
            }

            return null;
        }
    }
}
