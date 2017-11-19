package ch.ethz.inf.vs.kompose.service;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.data.Message;

/**
 * Asynchronous handler that handles a message the server received.
 */
public class ServerMessageHandler implements Runnable {

    private static final String LOG_TAG = "## ServerMessageHandler";

    private Socket socket;
    private NetworkService networkService;

    public ServerMessageHandler(Socket socket, NetworkService networkService) {
        this.socket = socket;
        this.networkService = networkService;
    }

    // TODO
    @Override
    public void run() {
        try {
            Log.d(LOG_TAG, "Thread dispatched");
            Message msg = networkService.readMessage(socket);
            Log.d(LOG_TAG, "Message received (" + msg.getType() + ")");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
