package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;

public class ClientRegistrationTask extends AsyncTask<Void, Void, Boolean> {

    private static final String LOG_TAG = "## RegistrationTask";
    private static final int TEMPORARY_SOCKET_TIMEOUT = 5000;

    private ServerSocket clientServerSocket;
    private UUID sessionUUID;
    private SimpleListener<Boolean, Void> callbackListener;
    private Context context;

    public ClientRegistrationTask(Context context, ServerSocket clientServerSocket,
                                  SimpleListener<Boolean, Void> callbackListener) throws SocketException {
        //Retrieve connection details of active session:
        SessionModel session = StateSingleton.getInstance().activeSession;
        if (session == null || session.getUUID() == null) {
            throw new IllegalStateException("Session or its UUID were null.");
        }
        this.context = context;
        this.sessionUUID = session.getUUID();
        this.clientServerSocket = clientServerSocket;
        this.callbackListener = callbackListener;

        clientServerSocket.setSoTimeout(TEMPORARY_SOCKET_TIMEOUT);
    }

    /**
     * Wait for a connection from the host, then accept and verify whether it is a proper response to the registration.
     * Yes I know, the try/catch blocks are ugly as sin, but alas, it's Java Sockets.
     *
     * @return Connection to the host
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