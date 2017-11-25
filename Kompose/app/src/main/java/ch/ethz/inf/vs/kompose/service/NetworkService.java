package ch.ethz.inf.vs.kompose.service;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.service.handler.MessageHandler;

/**
 * Service that provides various network functionality.
 */
public class NetworkService {

    private static final String LOG_TAG = "## NetworkService";

    private Message getMessage(MessageType type) {
        Message msg = new Message();
        String uuid = StateSingleton.getInstance().getDeviceUUID().toString();
        msg.setSenderUuid(uuid);
        msg.setType(type.toString());
        return msg;
    }

    public void sendRegisterClient(String username) {
        Message msg = getMessage(MessageType.REGISTER_CLIENT);
        msg.setSenderUsername(username);
        sendMessage(msg);
    }

    public void sendCastSkipSongVote(Song song) {
        Message msg = getMessage(MessageType.CAST_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessage(msg);
    }

    public void sendRemoveSkipSongVote(Song song) {
        Message msg = getMessage(MessageType.REMOVE_SKIP_SONG_VOTE);
        msg.setSongDetails(song);
        sendMessage(msg);
    }

    public void sendKeepAlive() {
        Message msg = getMessage(MessageType.KEEP_ALIVE);
        sendMessage(msg);
    }

    public void sendRequestSong(Song song) {
        Message msg = getMessage(MessageType.REQUEST_SONG);
        msg.setSongDetails(song);
        sendMessage(msg);
    }

    public void sendUnRegisterClient() {
        Message msg = getMessage(MessageType.UNREGISTER_CLIENT);
        sendMessage(msg);
    }

    public void sendSessionUpdate(Session session) {
        Message msg = getMessage(MessageType.SESSION_UPDATE);
        msg.setSession(session);
        sendMessage(msg);
    }

    public void sendError(String error) {
        Message msg = getMessage(MessageType.ERROR);
        msg.setErrorMessage(error);
        sendMessage(msg);
    }

    public void sendFinishSession() {
        Message msg = getMessage(MessageType.FINISH_SESSION);
        sendMessage(msg);
    }


    private void sendMessage(Message message) {
        // if this device is host, call message handler directly
        if (StateSingleton.getInstance().deviceIsHost) {
            Thread handler = new Thread(new MessageHandler(message));
            handler.start();
            return;
        }

        // otherwise, send the message over network
        ServerConnectionDetails connectionDetails = StateSingleton.getInstance().activeSession.getConnectionDetails();
        if (connectionDetails == null) {
            Log.d(LOG_TAG, "tried to send message but no active connection");
        } else {
            AsyncSender asyncSender = new AsyncSender(message, connectionDetails.getHostIP(), connectionDetails.getHostPort());
            asyncSender.execute();
        }
    }

    private class AsyncSender extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "## AsyncSender";

        InetAddress hostIP;
        int hostPort;
        Message message;

        public AsyncSender(Message msg, InetAddress ip, int port) {
            this.message = msg;
            this.hostIP = ip;
            this.hostPort = port;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Socket socket = new Socket(hostIP, hostPort);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                // send message
                printWriter.print(JsonConverter.toJsonString(message));
                printWriter.flush();
                printWriter.close();

                input.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
