package ch.ethz.inf.vs.kompose.service.handler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

/**
 * Service that provides various network functionality.
 */
public class OutgoingMessageHandler {

    private static final String LOG_TAG = "## OutMessageHandler";

    private Context context;

    public OutgoingMessageHandler(Context context) {
        this.context = context;
    }

    /**
     * Retrieves the base structure for a message
     *
     * @param type What kind of message this is
     * @return Message data object
     */
    private Message getBaseMessageHost(MessageType type) {
        Message msg = new Message();
        String uuid = StateSingleton.getInstance().getActiveClient().getUUID().toString();
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

    public void sendCastSkipSongVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(getSession().getClients());
        Song song = songConverter.convert(songModel);

        Message msg = getBaseMessageHost(MessageType.CAST_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendRemoveSkipSongVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(getSession().getClients());
        Song song = songConverter.convert(songModel);

        Message msg = getBaseMessageHost(MessageType.REMOVE_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendKeepAlive() {
        Message msg = getBaseMessageHost(MessageType.KEEP_ALIVE);
        sendMessageToHost(msg);
    }

    public void sendRequestSong(SongModel songModel) {
        songModel.setSongStatus(SongStatus.REQUESTED);
        SongConverter songConverter = new SongConverter(getSession().getClients());
        Song song = songConverter.convert(songModel);

        Message msg = getBaseMessageHost(MessageType.REQUEST_SONG);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendUnRegisterClient() {
        getSession().setSessionStatus(SessionStatus.FINISHED);
        if (getSession().getIsHost()) {
            Message msg = getBaseMessageHost(MessageType.FINISH_SESSION);
            sendMessageToHost(msg);
        } else {
            Message msg = getBaseMessageHost(MessageType.UNREGISTER_CLIENT);
            sendMessageToHost(msg);
        }
    }

    public void sendSessionUpdate() {
        if (getSession().getIsHost()) {
            SessionConverter sessionConverter = new SessionConverter();
            Session session = sessionConverter.convert(getSession());
            Message message = getBaseMessageHost(MessageType.SESSION_UPDATE);
            message.setSession(session);

            sendMessageToClients(message);
            new StorageHandler(context).persist(message.getSession());
        } else {
            Log.d(LOG_TAG, "tried to send session update but not host ");
        }
    }

    private void sendMessageToClients(Message message) {// send message to all clients, but not to itself
        for (ClientModel c : getSession().getClients()) {
            if (!c.getUUID().equals(StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID())) {
                Log.d(LOG_TAG, "sending session update to: " + c.getName()
                        + " (" + c.getUUID().toString() + ")");
                InetAddress clientIP = c.getClientConnectionDetails().getIp();
                int clientPort = c.getClientConnectionDetails().getPort();
                AsyncSender asyncSender = new AsyncSender(message, clientIP, clientPort);
                asyncSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    public void sendError(String error) {
        Message msg = getBaseMessageHost(MessageType.ERROR);
        msg.setErrorMessage(error);
        sendMessageToHost(msg);
    }

    // send a message to the globally stored host via IP/port
    private void sendMessageToHost(Message message) {
        // if this device is host, call message handler directly
        if (StateSingleton.getInstance().getActiveSession().getIsHost()) {
            Log.d(LOG_TAG, "device is host, don't send message to network");
            Thread handler = new Thread(new IncomingMessageHandler(context, message));
            handler.start();
            return;
        }

        // otherwise, send the message over network
        ServerConnectionDetails connectionDetails = StateSingleton.getInstance().getActiveSession()
                .getConnectionDetails();
        if (connectionDetails == null) {
            Log.d(LOG_TAG, "tried to send message but no active connection");
        } else {
            AsyncSender asyncSender = new AsyncSender(message,
                    connectionDetails.getHostIP(), connectionDetails.getHostPort());
            asyncSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private SessionModel getSession() {
        return StateSingleton.getInstance().getActiveSession();
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
