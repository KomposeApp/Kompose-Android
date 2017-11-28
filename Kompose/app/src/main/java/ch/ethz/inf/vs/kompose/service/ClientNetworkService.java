package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Intent;
import android.databinding.ObservableList;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.youview.tinydnssd.DiscoverResolver;
import com.youview.tinydnssd.MDNSDiscover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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

    // Type by which our Kompose service will be identified
    private static final String SERVICE_TYPE = "_kompose._tcp";

    private IBinder binder = new LocalBinder();

    private Socket updateSocket;
    private boolean initialized = false;

    /**
     * Add Network services to the provided ObservableList
     * Uses a third-party resolver library called "DiscoverResolver"
     * in order to circumvent a bug with NSD in Android 6.0.
     * @param sessionModels List which the NetworkServices are to be added to
     */
    public void findNetworkServices(final ObservableList<SessionModel> sessionModels) {
        Log.d(LOG_TAG, "starting service discovery ...");

        DiscoverResolver resolver = new DiscoverResolver(this, SERVICE_TYPE,
                new KomposeResolveListener(sessionModels));
        resolver.start();
        //TODO: Stop this task again
    }

    public void initialize(Socket socket) {
        this.updateSocket = socket;
        this.initialized = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!initialized) {
            Log.e(LOG_TAG, "not initialized, not starting socket listener");
            return START_STICKY;
        }

        startClientSocketListener(updateSocket);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d(LOG_TAG, "ClientNetworkService has been unbound!");
        //Set to true if we want to use onRebind()
        return false;
    }

    private void startClientSocketListener(Socket socket) {
        ClientListenerTask clientListenerTask = new ClientListenerTask(socket);
        clientListenerTask.execute();
    }


    private static class ClientListenerTask extends AsyncTask<Void, Void, Void> {

        private static final String LOG_TAG = "## ClientListenerTask";
        private Socket socket;

        ClientListenerTask(Socket socket) {
            this.socket = socket;
        }

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

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    public class LocalBinder extends Binder {
        public ClientNetworkService getService() {
            return ClientNetworkService.this;
        }
    }

    private class KomposeResolveListener implements DiscoverResolver.Listener {

        private ObservableList<SessionModel> sessionModels;

        KomposeResolveListener(ObservableList<SessionModel> sessionModels) {
            this.sessionModels = sessionModels;
        }

        @Override
        public void onServicesChanged(Map<String, MDNSDiscover.Result> services) {
            Log.d(LOG_TAG, "mDNS service changed");

            final List<SessionModel> newSessions = new ArrayList<>();

            // Find the new services
            for (MDNSDiscover.Result r : services.values()) {
                UUID sessionUUID = UUID.fromString(r.txt.dict.get("uuid"));
                UUID hostUUID = UUID.fromString(r.txt.dict.get("host_uuid"));
                String hostName = r.txt.dict.get("host_name");
                String sessionName = r.txt.dict.get("session");

                int port = r.srv.port;
                InetAddress host;
                try {
                    host = InetAddress.getByName(r.a.ipaddr);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    continue;
                }

                // check if it is a newly found session
                boolean foundBefore = false;
                for (SessionModel s : sessionModels) {
                    if (s.getUuid().equals(sessionUUID)) {
                        foundBefore = true;
                        break;
                    }
                }

                if (!foundBefore) {
                    SessionModel sessionModel = new SessionModel(sessionUUID, hostUUID);
                    sessionModel.setName(sessionName);
                    sessionModel.setHostName(hostName);
                    sessionModel.setConnectionDetails(new ServerConnectionDetails(host, port));
                    newSessions.add(sessionModel);
                }
            }

            // Add the newly found services to the ObservableList
            // Note: the observable list callbacks must be called on the UI thread
            Runnable uiTask = new Runnable() {
                @Override
                public void run() {
                    sessionModels.addAll(newSessions);
                }
            };
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(uiTask);
        }
    }
}
