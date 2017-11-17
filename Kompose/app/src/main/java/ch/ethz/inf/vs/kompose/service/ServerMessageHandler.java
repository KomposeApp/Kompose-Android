package ch.ethz.inf.vs.kompose.service;

import java.io.IOException;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.data.Message;

/**
 * Asynchronous handler that handles a message the server received.
 */
public class ServerMessageHandler implements Runnable {

    private Socket socket;

    public ServerMessageHandler(Socket socket) {
        this.socket = socket;
    }

    // TODO
    @Override
    public void run() {
        try {
            Message msg = NetworkService.readMessage(socket);
        } catch (IOException e) { }
    }
}
