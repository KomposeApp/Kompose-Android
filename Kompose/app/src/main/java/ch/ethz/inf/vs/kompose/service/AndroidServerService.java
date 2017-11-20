package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.network.ConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

/**
 * Android service that starts the server.
 * First the service is registered on the network, then an AsyncTask
 * that accepts connections is launched.
 */
public class AndroidServerService extends BaseService {

    private static final String LOG_TAG = "## AndroidServerService";
    private static final String SERVICE_NAME = "Kompose";
    private static final String SERVICE_TYPE = "_http._tcp";

    public static final String FOUND_SERVICE = "AndroidServerService.FOUND_SERVICE";

    private ServerSocket serverSocket;
    private int localPort;
    private String serviceName;
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener nsdRegistrationListener;
    private ServerTask serverTask;

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

        nsdRegistrationListener = new ServerRegistrationListener();
        nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener);

        // start server task
        serverTask = new ServerTask();
        serverTask.execute();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
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
            Log.d(LOG_TAG, "Server ready to receive connections");

            while (!this.isCancelled()) {
                try {
                    final Socket socket = serverSocket.accept();
                    Log.d(LOG_TAG, "message received");

                    Thread msgHandler = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                Log.d(LOG_TAG, "Thread dispatched");
                                Message msg = readMessage(socket);
                                Log.d(LOG_TAG, "Message received (" + msg.getType() + ")");

                                // TODO

                                MessageType messageType = MessageType.valueOf(msg.getType());
                                switch (messageType) {
                                    case REGISTER_CLIENT:
                                        break;
                                    case UNREGISTER_CLIENT:
                                        break;
                                    case SESSION_UPDATE:
                                        OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                                        //send stuff
                                        break;
                                    case REQUEST_SONG:
                                        break;
                                    case CAST_SKIP_SONG_VOTE:
                                        break;
                                    case REMOVE_SKIP_SONG_VOTE:
                                        break;
                                    case KEEP_ALIVE:
                                        break;
                                    case FINISH_SESSION:
                                        break;
                                    case ERROR:
                                        break;
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    msgHandler.start();
                } catch (Exception e) {
                    Log.d(LOG_TAG, "could not process message; exception occurred! " + e.toString());
                }
            }

            return null;
        }
    }
}
