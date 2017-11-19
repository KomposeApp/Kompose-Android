package ch.ethz.inf.vs.kompose.service;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;

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

    @Override
    public void run() {
        try {
            Log.d(LOG_TAG, "Thread dispatched");
            Message msg = networkService.readMessage(socket);
            Log.d(LOG_TAG, "Message received (" + msg.getType() + ")");

            // TODO
            MessageType messageType = MessageType.valueOf(msg.getType());
            switch (messageType) {
                case REQUEST_INFORMATION:
                    OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                    Message response = new Message();
                    response.setType(MessageType.SESSION_UPDATE.toString());
                    response.setSession(SessionConverter.convert(StateService.getInstance().liveSession));
                    break;
                case REGISTER_CLIENT:
                    break;
                case UNREGISTER_CLIENT:
                    break;
                case SESSION_UPDATE:
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
}
