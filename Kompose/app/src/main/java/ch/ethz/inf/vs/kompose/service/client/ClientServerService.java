package ch.ethz.inf.vs.kompose.service.client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;


public class ClientServerService extends Service {

    private final String LOG_TAG = "##ClientServerService";
    private Thread clientListenerTask;
    private ServerSocket clientSocket;

    private IBinder binder = new ClientServerService.LocalBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public ClientServerService getService() {
            return ClientServerService.this;
        }
    }


    public void startClientListener() throws IOException {

        clientSocket = new ServerSocket(StateSingleton.getInstance().getPreferenceUtility().getClientPort());

        clientListenerTask = new Thread(new ClientListenerTask(this, clientSocket));
        clientListenerTask.start();
    }


    /**
     * Handles breakdown of client socket listener
     **/
    @Override
    public boolean onUnbind(Intent intent) {

        // cancel client task
        if (clientListenerTask != null && !clientListenerTask.isInterrupted()){
            Log.d(LOG_TAG, "Shutting down the Message Receiver");
            clientListenerTask.interrupt();
        }

        // close the socket
        if (clientSocket != null && !clientSocket.isClosed()){
            try {
                clientSocket.close();

                Log.d(LOG_TAG, "Client serversocket closed");
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to close client socket");
                e.printStackTrace();
            }
        }

        Log.d(LOG_TAG, "Service unbound.");
        return false;
    }



    /*
     * This is where the client listens for messages from the host.
     * Only started once we call startService() in NSDListenerService.
     */
    private class ClientListenerTask implements Runnable {

        private final String LOG_TAG = "##ClientListenerTask";

        private ServerSocket serverSocket;
        private Context context;

        ClientListenerTask(Context context, ServerSocket serverSocket) throws IOException {
            this.context = context;
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            Log.d(LOG_TAG, "started on port " + serverSocket.getLocalPort());
            while (!Thread.interrupted()) {
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
        }
    }
}
