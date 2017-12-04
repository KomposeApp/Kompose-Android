package ch.ethz.inf.vs.kompose.service.client;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;

public class ClientRegistrationTask extends AsyncTask<Void, Void, Boolean> {

    private static final String LOG_TAG = "## RegistrationTask";
    private static final int TEMPORARY_SOCKET_TIMEOUT = 5000;

    private ServerSocket clientServerSocket;
    private SimpleListener<Boolean, Void> callbackListener;
    private Context context;

    ClientRegistrationTask(Context context, ServerSocket clientServerSocket,
                           SimpleListener<Boolean, Void> callbackListener) throws SocketException {

        this.context = context;
        this.clientServerSocket = clientServerSocket;
        this.callbackListener = callbackListener;

        // Set a temporary timeout for the ServerSocket
        clientServerSocket.setSoTimeout(TEMPORARY_SOCKET_TIMEOUT);
    }

    /**
     * Wait for a response from the host. If we got one, return true.
     * TODO: Maybe add better security somehow, e.g. UUID check, MessageType check
     * @return true iff response received
     */
    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Socket connection = clientServerSocket.accept();
            Log.d(LOG_TAG, "message received");
            IncomingMessageHandler messageHandler = new IncomingMessageHandler(context, connection);
            Thread msgHandler = new Thread(messageHandler);
            msgHandler.start();
            return true;
        } catch (IOException io) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        try {
            clientServerSocket.setSoTimeout(0);
        } catch (SocketException e) {
            e.printStackTrace();
            callbackListener.onEvent(false, null);
        }
        callbackListener.onEvent(success, null);
    }
}