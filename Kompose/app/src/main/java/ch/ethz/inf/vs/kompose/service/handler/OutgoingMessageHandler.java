package ch.ethz.inf.vs.kompose.service.handler;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

/**
 * Service that provides various network functionality.
 */
public class OutgoingMessageHandler {

    private static final String LOG_TAG = "## OutMessageHandler";

    /**
     * Retrieves the base structure for a message
     *
     * @param type What kind of message this is
     * @return Message data object
     */
    private Message getBaseMessageHost(MessageType type) {
        Message msg = new Message();
        String uuid = StateSingleton.getInstance().activeClient.getUUID().toString();
        msg.setSenderUuid(uuid);
        msg.setType(type.toString());
        return msg;
    }

    public void sendRegisterClient(String username, int port) {
        Message msg = getBaseMessageHost(MessageType.REGISTER_CLIENT);
        msg.setSenderUsername(username);
        msg.setPort(port);
        sendMessageToHost(msg);
    }

    public void sendCastSkipSongVote(Song song) {
        Message msg = getBaseMessageHost(MessageType.CAST_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendRemoveSkipSongVote(Song song) {
        Message msg = getBaseMessageHost(MessageType.REMOVE_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendKeepAlive() {
        Message msg = getBaseMessageHost(MessageType.KEEP_ALIVE);
        sendMessageToHost(msg);
    }

    public void sendRequestSong(Song song) {
        Message msg = getBaseMessageHost(MessageType.REQUEST_SONG);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendUnRegisterClient(SessionModel sessionModel) {
        if (sessionModel.getIsHost()) {
            Message msg = getBaseMessageHost(MessageType.UNREGISTER_CLIENT);
            sendMessageToHost(msg);
        }
    }

    public void sendSessionUpdate(SessionModel sessionModel) {
        if (sessionModel.getIsHost()) {
            SessionConverter sessionConverter = new SessionConverter();
            Session session = sessionConverter.convert(sessionModel);
            Message message = getBaseMessageHost(MessageType.SESSION_UPDATE);
            message.setSession(session);

            // send message to all clients, but not to itself
            for (ClientModel c : sessionModel.getClients()) {
                if (!c.getUUID().equals(StateSingleton.getInstance().deviceUUID)) {
                    Log.d(LOG_TAG, "sending session update to: " + c.getName()
                            + " (" + c.getUUID().toString() + ")");
                    InetAddress clientIP = c.getClientConnectionDetails().getIp();
                    int clientPort = c.getClientConnectionDetails().getPort();
                    AsyncSender asyncSender = new AsyncSender(message, clientIP, clientPort);
                    asyncSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        } else {
            Log.d(LOG_TAG, "tried to send session update but not host ");
        }
    }

    public void sendError(String error) {
        Message msg = getBaseMessageHost(MessageType.ERROR);
        msg.setErrorMessage(error);
        sendMessageToHost(msg);
    }

    public void sendFinishSession() {
        Message msg = getBaseMessageHost(MessageType.FINISH_SESSION);
        sendMessageToHost(msg);
    }

    // send a message to the globally stored host via IP/port
    private void sendMessageToHost(Message message) {
        // if this device is host, call message handler directly
        if (StateSingleton.getInstance().activeSession.getIsHost()) {
            Log.d(LOG_TAG, "device is host, don't send message to network");
            Thread handler = new Thread(new IncomingMessageHandler(message));
            handler.start();
            return;
        }

        // otherwise, send the message over network
        ServerConnectionDetails connectionDetails = StateSingleton.getInstance().activeSession
                .getConnectionDetails();
        if (connectionDetails == null) {
            Log.d(LOG_TAG, "tried to send message but no active connection");
        } else {
            AsyncSender asyncSender = new AsyncSender(message,
                    connectionDetails.getHostIP(), connectionDetails.getHostPort());
            asyncSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private static class AsyncSender extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "## AsyncSender";

        private InetAddress hostIP;
        private int hostPort;
        private Message message;
        private Socket socket;

        SimpleListener socketRetriever;

        AsyncSender(Message msg, InetAddress ip, int port) {
            this.message = msg;
            this.hostIP = ip;
            this.hostPort = port;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket(hostIP, hostPort);

                // send message
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.print(JsonConverter.toJsonString(message));
                printWriter.flush();

                printWriter.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            if (socketRetriever != null) {
                socketRetriever.onEvent(0, socket);
            }
        }
    }
}
