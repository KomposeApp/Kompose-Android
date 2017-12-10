package ch.ethz.inf.vs.kompose.service.client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableList;
import android.net.nsd.NsdManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.youview.tinydnssd.DiscoverResolver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.client.listeners.ClientServiceListener;
import ch.ethz.inf.vs.kompose.service.client.listeners.KomposeResolveListenerWorkaround;
import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;

import static ch.ethz.inf.vs.kompose.MainActivity.SERVICE_TYPE;
import static ch.ethz.inf.vs.kompose.MainActivity.SERVICE_TYPE_NSD;

public class ClientNetworkService extends Service {

    private final String LOG_TAG = "##ClientNetworkService";

    private IBinder binder = new LocalBinder();
    private boolean initialized = false;
    private int actualLocalPort = 0;

    private ServerSocket clientServerSocket;

    private DiscoverResolver resolver;
    private NsdManager nsdManager;
    private ClientServiceListener clientServiceListener;

    private ClientListenerTask clientListenerTask;

    /**
     * Prepare for onStartCommand
     **/
    public void initSocketListener() throws IOException {
        int clientPort = StateSingleton.getInstance().getPreferenceUtility().getClientPort();
        this.clientServerSocket = new ServerSocket(clientPort);
        this.initialized = true;

        // Necessary in case we choose a random port (i.e. clientPort == 0)
        this.actualLocalPort = clientServerSocket.getLocalPort();
    }

    /**
     * Accessor method to close the Client Socket from the activity in case anything goes wrong.
     * @throws IOException In case anything goes wrong
     */
    public void closeClientSocket() throws IOException {
        if (clientServerSocket != null && !clientServerSocket.isClosed()){
            clientServerSocket.close();
        }
    }

    /**
     * Returns the actual Client Port as initialized by the ServerSocket.
     * Differs from the one stored in preferences when using port 0.
     */
    public int getActualClientPort() {
        return actualLocalPort;
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

        clientListenerTask = new ClientListenerTask(this, clientServerSocket, actualLocalPort);
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

    /**
     * Handles breakdown of client socket listener
     **/
    @Override
    public void onDestroy() {
        super.onDestroy();

        // cancel client task
        if (clientListenerTask != null && !clientListenerTask.isCancelled()){
            Log.d(LOG_TAG, "Shutting down the Message Receiver");
            clientListenerTask.cancel(true);
        }

        // close the socket
        if (clientServerSocket != null && !clientServerSocket.isClosed()){
            try {
                Log.d(LOG_TAG, "Closing client server socket");
                closeClientSocket();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to close client socket");
                e.printStackTrace();
            }
        }

        Log.d(LOG_TAG, "Service killed for real.");
    }

    /**
     * Add wait for sessions to be added to the provided ObservableList
     * Started when the service is BOUND
     * @param sessionModels Used by the listview to display the available sessions
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
            clientServiceListener = new ClientServiceListener(sessionModels, nsdManager);
            nsdManager.discoverServices(SERVICE_TYPE_NSD, NsdManager.PROTOCOL_DNS_SD, clientServiceListener);
        }
    }

    /**
     * Handles breakdown of NSD listener
     **/
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "Service has been unbound!");

        // break down workaround library for older android versions
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Log.d(LOG_TAG, "Breaking down NSD Service for outdated devices...");
            if (resolver != null) resolver.stop();
        }
        // break down standard android API for up-to-date versions
        else {
            Log.d(LOG_TAG, "Breaking down NSD Service for newer devices...");
            if (nsdManager != null && clientServiceListener != null)
                nsdManager.stopServiceDiscovery(clientServiceListener);
        }

        //Set to true if we want to use onRebind() at some point
        return false;
    }

    /**
     * Sends the register message to the host and waits for a response.
     * Currently only a single attempt is made.
     * @param callbackListener Callback listener with which we will update the corresponding activity
     * @param clientName Name of the client to register on the host
     * @throws SocketException Thrown should opening the socket fail for some reason.
     */
    public void registerClientOnHost(SimpleListener<Boolean, Void> callbackListener,
                                     String clientName) throws SocketException {
        // Send join request to the host.
        // Listen for responses from the host. If we get a matching response, proceed to the playlist.
        Log.d(LOG_TAG, "Sending a join request to the host");
        new OutgoingMessageHandler(this).sendRegisterClient(clientName, clientServerSocket.getLocalPort());
        new ClientRegistrationTask(this, clientServerSocket, callbackListener)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /*
     * This is where the client listens for messages from the host.
     * Only started once we call startService() in ClientNetworkService.
     */
    private static class ClientListenerTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "##ClientListenerTask";

        private ServerSocket serverSocket;
        private int localPort;
        private Context context;

        ClientListenerTask(Context context, ServerSocket serverSocket, int port) {
            this.context = context;
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
                    IncomingMessageHandler messageHandler = new IncomingMessageHandler(context, connection);
                    Thread msgHandler = new Thread(messageHandler);
                    msgHandler.start();
                } catch (IOException io) {
                    Log.d(LOG_TAG, "An exception occured: " + io.getMessage());
                }
            }
            return null;
        }
    }

}
