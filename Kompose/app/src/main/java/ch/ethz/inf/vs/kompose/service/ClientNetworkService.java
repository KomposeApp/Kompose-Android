package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.SessionModel;

public class ClientNetworkService extends Service {

    private static final String LOG_TAG = "## ClientNetworkService";
    private static final String SERVICE_TYPE = "_kompose._tcp";

    private IBinder binder = new LocalBinder();

    /**
     * Add Network services to the provided ObservableArrayList
     * @param list List which the NetworkServices are to be added to
     */
    public void findNetworkServices(ObservableArrayList<SessionModel> list) {
        NsdManager nsdManager = (NsdManager) this.getSystemService(NSD_SERVICE);
        ClientServiceListener clientServiceListener = new ClientServiceListener(list);
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, clientServiceListener);
    }

    public void startClientSocketListener(Socket socket) {
        ClientListenerTask clientListenerTask = new ClientListenerTask(socket);
        clientListenerTask.execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public ClientNetworkService getService() {
            return ClientNetworkService.this;
        }
    }

    private static class ClientListenerTask extends AsyncTask<Void, Void, Void> {

        private Socket socket;
        private static final String LOG_TAG = "## ClientListenerTask";

        private Message readMessage(Socket connection) throws IOException {
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder json = new StringBuilder();

            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                json.append(new String(buffer, 0, bytesRead));
            }
            Log.d(LOG_TAG, "message read from stream: " + json.toString());

            Message message = JsonConverter.fromMessageJsonString(json.toString());
            input.close();
            return message;
        }

        ClientListenerTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Message msg = readMessage(socket);
                    if (MessageType.valueOf(msg.getType()) == MessageType.SESSION_UPDATE) {
                        // TODO
                        // call message handler
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    private class ClientServiceListener implements NsdManager.DiscoveryListener {

        private ObservableArrayList<SessionModel> sessionList;

        ClientServiceListener(ObservableArrayList<SessionModel> sessionList) {
            this.sessionList = sessionList;
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.d(LOG_TAG, "starting service discovery failed");
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.d(LOG_TAG, "stopping service discovery failed");
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.d(LOG_TAG, "service discovery started");
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.d(LOG_TAG, "service discovery stopped");
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "service found: "
                    + serviceInfo.getServiceName() + " ("
                    + serviceInfo.getHost() + ","
                    + serviceInfo.getPort() + ")");
            Map<String,byte[]> attributes = serviceInfo.getAttributes();
            SessionModel sessionModel = new SessionModel(
                    UUID.fromString(new String(attributes.get("uuid"))),
                    UUID.fromString(new String(attributes.get("host_uuid")))
            );
            sessionModel.setSessionName(new String(attributes.get("session")));
            sessionList.add(sessionModel);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "service lost: " + serviceInfo.getServiceName());
        }
    }
}
