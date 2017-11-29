package ch.ethz.inf.vs.kompose.service.handler;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
     * @param type What kind of message this is
     * @return Message data object
     */
    private Message getBaseMessage(MessageType type) {
        Message msg = new Message();
        String uuid = StateSingleton.getInstance().getDeviceUUID().toString();
        msg.setSenderUuid(uuid);
        msg.setType(type.toString());
        return msg;
    }

    public void sendRegisterClient(String username, SimpleListener socketRetriever) {
        Message msg = getBaseMessage(MessageType.REGISTER_CLIENT);
        msg.setSenderUsername(username);
        sendMessage(msg, socketRetriever);
    }

    public void sendCastSkipSongVote(Song song) {
        Message msg = getBaseMessage(MessageType.CAST_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessage(msg);
    }

    public void sendRemoveSkipSongVote(Song song) {
        Message msg = getBaseMessage(MessageType.REMOVE_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessage(msg);
    }

    public void sendKeepAlive() {
        Message msg = getBaseMessage(MessageType.KEEP_ALIVE);
        sendMessage(msg);
    }

    public void sendRequestSong(Song song) {
        Message msg = getBaseMessage(MessageType.REQUEST_SONG);
        msg.setSongDetails(song);
        sendMessage(msg);
    }

    public void sendUnRegisterClient() {
        Message msg = getBaseMessage(MessageType.UNREGISTER_CLIENT);
        sendMessage(msg);
    }

    public void sendSessionUpdate(Session session) {
        Message msg = getBaseMessage(MessageType.SESSION_UPDATE);
        msg.setSession(session);
        sendMessage(msg);
    }

    public void sendError(String error) {
        Message msg = getBaseMessage(MessageType.ERROR);
        msg.setErrorMessage(error);
        sendMessage(msg);
    }

    public void sendFinishSession() {
        Message msg = getBaseMessage(MessageType.FINISH_SESSION);
        sendMessage(msg);
    }

    public void updateAllClients(SessionModel sessionModel) {
        SessionConverter sessionConverter = new SessionConverter();
        Session session = sessionConverter.convert(sessionModel);
        Message message = getBaseMessage(MessageType.SESSION_UPDATE);
        message.setSession(session);

        // send message to all clients, but not to itself
        for (ClientModel c : sessionModel.getClients()) {
            if (!c.getUuid().equals(StateSingleton.getInstance().deviceUUID)) {
                Log.d(LOG_TAG, "sending session update to: " + c.getName()
                        + " (" + c.getUuid().toString() + ")");
                Socket socket = c.getClientConnectionDetails().getSocket();
                AsyncSender asyncSender = new AsyncSender(message, socket);
                asyncSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        }
    }

    // send a message to the globally stored host via IP/port
    private void sendMessage(Message message) {
        // if this device is host, call message handler directly
        if (StateSingleton.getInstance().deviceIsHost) {
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

    // send a message to the globally stored host via IP/port and retrieve the openend socket
    // afterwards
    private void sendMessage(Message message, SimpleListener socketRetriever) {
        // if this device is host, call message handler directly
        if (StateSingleton.getInstance().deviceIsHost) {
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
                    connectionDetails.getHostIP(), connectionDetails.getHostPort(),
                    socketRetriever);
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

        AsyncSender(Message message, Socket socket) {
            this.message = message;
            this.socket = socket;
        }

        AsyncSender(Message message,
                           InetAddress ip,
                           int port,
                           SimpleListener socketRetriever) {
            this.message = message;
            this.hostIP = ip;
            this.hostPort = port;
            this.socketRetriever = socketRetriever;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                boolean closeSocket = false;

                // open a new socket if none given
                if (socket == null) {
                    socket = new Socket(hostIP, hostPort);
                    closeSocket = true;
                }

                if (socketRetriever != null) {
                    closeSocket = false;
                }

                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                // send message
                printWriter.print(JsonConverter.toJsonString(message));
                printWriter.flush();
                printWriter.close();

                //TODO: ???

                input.close();

                // only close the socket if a new one was created
                if (closeSocket) {
                    socket.close();
                }

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
