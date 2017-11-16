package ch.ethz.inf.vs.kompose.model;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.IBinder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AndroidServerService extends Service {

    private Context context;
    private ServerSocket serverSocket;
    private String serviceName;
    private NsdManager nsdManager;

    public AndroidServerService(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) { }

        // register network service
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("Kompose");
        serviceInfo.setServiceType("Kompose._tcp");
        serviceInfo.setPort(serverSocket.getLocalPort());

        NsdManager.RegistrationListener registrationListener = new ServerRegistrationListener();
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
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
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        }
    }

    private class ServerTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

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
