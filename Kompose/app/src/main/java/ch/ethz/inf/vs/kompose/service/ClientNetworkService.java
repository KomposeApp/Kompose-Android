package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
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

import com.youview.tinydnssd.DiscoverResolver;
import com.youview.tinydnssd.MDNSDiscover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;

public class ClientNetworkService extends Service {

    private static final String LOG_TAG = "## ClientNetworkService";
    private static final String SERVICE_TYPE = "_kompose._tcp";
    private static final String SERVICE_TYPE_NSD = "_kompose._tcp.";

    private IBinder binder = new LocalBinder();
    private boolean initialized = false;
    private ServerSocket clientServerSocket;
    private int localPort = 0;

    private DiscoverResolver resolver;
    private NsdManager nsdManager;
    private ClientServiceListener clientServiceListener;

    private  ClientListenerTask clientListenerTask;

    /** Prepare for onStartCommand **/
    public void initSocketListener() {
        try {
            this.clientServerSocket = new ServerSocket(0);
            this.initialized = true;
            this.localPort = clientServerSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getClientPort() {
        return localPort;
    }

    /**
     * This starts the path where the ClientNetworkService begins listening for incoming JSON messages.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!initialized) {
            Log.e(LOG_TAG, "FATAL ERROR: Socket has not been properly initialized!");
            stopSelf();
            return START_STICKY;
        }

        clientListenerTask = new ClientListenerTask(clientServerSocket, localPort);
        clientListenerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_STICKY;
    }

    /**
     * This starts the path where the ClientNetworkService does NSD discovery.
     */
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

    /** Handles breakdown of client socket listener **/
    @Override
    public void onDestroy(){
        super.onDestroy();
        clientListenerTask.cancel(true);
        Log.d(LOG_TAG,"Service killed for real.");
    }

    /**
     * Add Network services to the provided ObservableList
     * Started when the service is BOUND
     * @param sessionModels List which the NetworkServices are to be added to
     */
    public void findNetworkServices(final ObservableList<SessionModel> sessionModels) {
        Log.d(LOG_TAG, "starting service discovery ...");

        // use workaround library for older android versions
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Log.d(LOG_TAG, "using workaround library for service discovery on outdated devices");

            resolver = new DiscoverResolver(this, SERVICE_TYPE,
                    new KomposeResolveListenerWorkaround(sessionModels));
            resolver.start();

        }
        // use standard android API for up-to-date versions
        else {
            Log.d(LOG_TAG, "using standard android NSD for service discovery");

            nsdManager = (NsdManager) this.getSystemService(NSD_SERVICE);
            clientServiceListener = new ClientServiceListener(new KomposeResolveListener(sessionModels), nsdManager);
            nsdManager.discoverServices(SERVICE_TYPE_NSD, NsdManager.PROTOCOL_DNS_SD, clientServiceListener);
        }
    }

    /** Handles breakdown of NSD listener **/
    @Override
    public boolean onUnbind(Intent intent){
        Log.d(LOG_TAG, "Service has been unbound!");

        // use workaround library for older android versions
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Log.d(LOG_TAG, "Breaking down NSD Service for outdated devices...");
            if (resolver!=null) resolver.stop();
        }
        // use standard android API for up-to-date versions
        else {
            Log.d(LOG_TAG, "Breaking down NSD Service for newer devices...");
            if (nsdManager!=null && clientServiceListener != null)
                nsdManager.stopServiceDiscovery(clientServiceListener);
        }

        //Set to true if we want to use onRebind() at some point
        return false;
    }

    /*
     * This is where the client listens for messages from the host.
     * Only started once we call startService().
     */
    private static class ClientListenerTask extends AsyncTask<Void, Void, Void> {

        private static final String LOG_TAG = "## ClientListenerTask";

        private ServerSocket serverSocket;
        private int localPort;

        ClientListenerTask(ServerSocket serverSocket, int port) {
            this.serverSocket = serverSocket;
            this.localPort = port;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(LOG_TAG, "started on port " + localPort);
            while (!isCancelled()) {
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

    /*
     * Workaround library for API < 24: https://github.com/youviewtv/tinydnssd
     * NSD Listener
     */
    private class KomposeResolveListenerWorkaround implements DiscoverResolver.Listener {

        //
        private ObservableList<SessionModel> sessionModels;

        KomposeResolveListenerWorkaround(ObservableList<SessionModel> sessionModels) {
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

    /*
     * Private classes for android API service discovery with NSD.
     * This only works correctly for API >= 24
     */

    private class ClientServiceListener implements NsdManager.DiscoveryListener {

        private NsdManager.ResolveListener resolveListener;
        private NsdManager nsdManager;

        ClientServiceListener(NsdManager.ResolveListener resolveListener, NsdManager nsdManager) {
            this.resolveListener = resolveListener;
            this.nsdManager = nsdManager;
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

            Log.d(LOG_TAG, "service found: " + serviceInfo.toString());
            if (!serviceInfo.getServiceType().equals(SERVICE_TYPE_NSD)) {
                Log.d(LOG_TAG, serviceInfo.getServiceType());
                return;
            }
            nsdManager.resolveService(serviceInfo, resolveListener);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "service lost: " + serviceInfo.getServiceName());
            if (!serviceInfo.getServiceType().equals(SERVICE_TYPE_NSD)) {
                Log.d(LOG_TAG, serviceInfo.getServiceType());
                return;
            }
            //serviceInfo.setAttribute(KEY_FOUND_STATE, LOST);
            //nsdManager.resolveService(serviceInfo, resolveListener);

        }
    }

    private class KomposeResolveListener implements NsdManager.ResolveListener {

        private ObservableList<SessionModel> sessionModels;

        KomposeResolveListener(ObservableList<SessionModel> sessionModels) {
            this.sessionModels = sessionModels;
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
            UUID hostUUID = UUID.fromString(new String(attributes.get("host_uuid")));
            String hostName = new String(attributes.get("host_name"));
            String sessionName = new String(attributes.get("session"));

            final SessionModel sessionModel = new SessionModel(sessionUUID, hostUUID);
            sessionModel.setName(sessionName);
            sessionModel.setHostName(hostName);
            sessionModel.setConnectionDetails(new ServerConnectionDetails(host, port));

            // the observable list callbacks must be called on the UI thread
            Runnable uiTask = new Runnable() {
                @Override
                public void run() {
                    for (SessionModel s: sessionModels){
                        if (sessionModel.getHostUUID().equals(s.getHostUUID())) return;
                    }
                    sessionModels.add(sessionModel);
                }
            };
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(uiTask);
        }
    }
}