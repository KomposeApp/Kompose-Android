package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableList;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.handler.MessageHandler;

public class ClientNetworkService extends Service {

    private static final String LOG_TAG = "## ClientNetworkService";
    private static final String SERVICE_TYPE = "_kompose._tcp.";

    private IBinder binder = new LocalBinder();
    private NsdManager nsdManager;

    private ObservableList<SessionModel> sessionModels;

    /**
     * Add Network services to the provided ObservableList
     * @param list List which the NetworkServices are to be added to
     */
    public void findNetworkServices(ObservableList<SessionModel> list) {
        Log.d(LOG_TAG, "starting service discovery");
        this.sessionModels = list;
        nsdManager = (NsdManager) this.getSystemService(NSD_SERVICE);
        ClientServiceListener clientServiceListener = new ClientServiceListener();
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
                        MessageHandler messageHandler = new MessageHandler(msg);
                        Thread msgHandler = new Thread(messageHandler);
                        msgHandler.start();
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    private class ClientServiceListener implements NsdManager.DiscoveryListener {

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

            Log.d(LOG_TAG, "service found: " + serviceInfo.getServiceName());
            if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                Log.d(LOG_TAG, serviceInfo.getServiceType());
                return;
            }

            nsdManager.resolveService(serviceInfo, resolveListener);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "service lost: " + serviceInfo.getServiceName());
        }
    }

    NsdManager.ResolveListener resolveListener = new KomposeResolveListener(this);

    private class KomposeResolveListener implements NsdManager.ResolveListener {

        private Context context;

        KomposeResolveListener(Context context) {
            this.context = context;
        }

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(LOG_TAG, "Resolve failed" + errorCode);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "Resolve Succeeded. " + serviceInfo);

            int port = serviceInfo.getPort();
            InetAddress host = serviceInfo.getHost();

            Map<String,byte[]> attributes = serviceInfo.getAttributes();
            UUID sessionUUID = UUID.fromString(new String(attributes.get("uuid")));
            //todo: retrieve host name directly?
            UUID hostUUID = UUID.fromString(new String(attributes.get("host_uuid")));

            final SessionModel sessionModel = new SessionModel(sessionUUID, hostUUID);
            sessionModel.setName(new String(attributes.get("session")));
            sessionModel.setConnectionDetails(new ServerConnectionDetails(host, port));

            // the observable list callbacks must be called on the UI thread
            Runnable uiTask = new Runnable() {
                @Override
                public void run() {
                    sessionModels.add(sessionModel);
                }
            };
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(uiTask);
        }
    }
}
