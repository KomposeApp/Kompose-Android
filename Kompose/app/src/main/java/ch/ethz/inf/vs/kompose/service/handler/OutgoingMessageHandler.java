package ch.ethz.inf.vs.kompose.service.handler;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
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
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

/**
 * Service that provides various network functionality.
 */
public class OutgoingMessageHandler {

    private final String LOG_TAG = "##OutMessageHandler";
    private Context context;

    public OutgoingMessageHandler(Context context) {
        this.context = context;
    }

    private Message getBaseMessage(MessageType type) {
        Message msg = new Message();
        String uuid = StateSingleton.getInstance().getActiveClient().getUUID().toString();
        msg.setSenderUuid(uuid);
        msg.setType(type.toString());
        return msg;
    }

    public void sendRegisterClient(String username, int port) {
        Message msg = getBaseMessage(MessageType.REGISTER_CLIENT);
        msg.setSenderUsername(username);
        msg.setPort(port);
        sendMessageToHost(msg);
    }

    public void sendCastSkipSongVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(getSession().getClients());
        Song song = songConverter.convert(songModel);

        Message msg = getBaseMessage(MessageType.CAST_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendRemoveSkipSongVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(getSession().getClients());
        Song song = songConverter.convert(songModel);

        Message msg = getBaseMessage(MessageType.REMOVE_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendKeepAlive() {
        Message msg = getBaseMessage(MessageType.KEEP_ALIVE);
        sendMessageToHost(msg);
    }

    public void sendRequestSong(SongModel songModel) {
        SongConverter songConverter = new SongConverter(getSession().getClients());
        Song song = songConverter.convert(songModel);

        Message msg = getBaseMessage(MessageType.REQUEST_SONG);
        msg.setSongDetails(song);
        sendMessageToHost(msg);
    }

    public void sendUnRegisterClient() {
        if (getSession().getIsHost()) {
            Message msg = getBaseMessage(MessageType.FINISH_SESSION);
            sendMessageToClients(msg);
        } else {
            Message msg = getBaseMessage(MessageType.UNREGISTER_CLIENT);
            sendMessageToHost(msg);
        }
        getSession().setSessionStatus(SessionStatus.FINISHED);
    }

    public void sendSessionUpdate() {
        if (getSession().getIsHost()) {
            SessionModel sessionModel = getSession();
            if (sessionModel.getSessionStatus().equals(SessionStatus.WAITING)) {
                sessionModel.setSessionStatus(SessionStatus.ACTIVE);
            }

            Session session = new SessionConverter().convert(sessionModel);
            Message message = getBaseMessage(MessageType.SESSION_UPDATE);
            message.setSession(session);

            sendMessageToClients(message);
        } else {
            Log.e(LOG_TAG, "Tried to send session update while not host ");
        }
    }

    private void sendMessageToClients(Message message) {
        // send message to all clients, but not to itself
        for (ClientModel c : getSession().getClients()) {
            if (!c.getUUID().equals(StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID())
                    && c.getIsActive()) {
                Log.d(LOG_TAG, "sending message to: " + c.getName()
                        + " (" + c.getUUID().toString() + ")");
                InetAddress clientIP = c.getClientConnectionDetails().getIp();
                int clientPort = c.getClientConnectionDetails().getPort();
                SenderRunnable asyncSender = new SenderRunnable(message, clientIP, clientPort);
                new Thread(asyncSender).start();
            }
        }
    }

    public void sendError(String error) {
        Message msg = getBaseMessage(MessageType.ERROR);
        msg.setErrorMessage(error);
        if (getSession().getIsHost()) {
            sendMessageToClients(msg);
        } else{
            sendMessageToHost(msg);
        }
    }

    // send a message to the globally stored host via IP/port
    private void sendMessageToHost(Message message) {
        // if we are the host, call message handler directly
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
            Log.e(LOG_TAG, "tried to send message but no active connection");
        } else {
            InetAddress hostIP = connectionDetails.getHostIP();
            int hostPort = connectionDetails.getHostPort();
            SenderRunnable asyncSender = new SenderRunnable(message, hostIP, hostPort);
            new Thread(asyncSender).start();
        }
    }

    private SessionModel getSession() {
        return StateSingleton.getInstance().getActiveSession();
    }

    private class SenderRunnable implements Runnable{

        private final String LOG_TAG = "##AsyncSender";

        private InetAddress hostIP;
        private int hostPort;
        private Message message;

        SenderRunnable(Message msg, InetAddress ip, int port) {
            this.message = msg;
            this.hostIP = ip;
            this.hostPort = port;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(hostIP, hostPort);

                // send message
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.print(JsonConverter.toJsonString(message));
                printWriter.flush();

                printWriter.close();
                socket.close();

            } catch (ConnectException c){
                Log.d(LOG_TAG, "Connection refused");
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to send message. Reason: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
