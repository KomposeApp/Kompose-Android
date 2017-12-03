package ch.ethz.inf.vs.kompose.service.client;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;

    /*
     * This is where the client listens for messages from the host.
     * Only started once we call startService() in ClientNetworkService.
     */
public class ClientListenerTask extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = "## ClientListenerTask";

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
                io.printStackTrace();
                Log.e(LOG_TAG, "could not process message; exception occurred: " + io.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.w(LOG_TAG, "Cleaning up the ServerSocket failed");
            e.printStackTrace();
        }
    }
}
