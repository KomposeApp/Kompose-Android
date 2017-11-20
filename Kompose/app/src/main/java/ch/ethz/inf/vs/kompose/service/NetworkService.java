package ch.ethz.inf.vs.kompose.service;

import android.content.Intent;
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
import ch.ethz.inf.vs.kompose.data.network.ConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

/**
 * Service that provides various network functionality.
 */
public class NetworkService extends BaseService {

    private final String LOG_TAG = "## NetworkService";
    public static final String RESPONSE_RECEIVED = "NetworkService.RESPONSE_RECEIVED";
    public static final String RESPONSE_FAILURE = "NetworkService.RESPONSE_FAILURE";
    private StateService stateService;

    public NetworkService(StateService stateService) {
        this.stateService = stateService;
    }

    public Message readMessage(Socket connection) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder json = new StringBuilder();

        char[] buffer = new char[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            json.append(new String(buffer, 0, bytesRead));
        }
        Log.d("## NetworkService", "message read from stream: " + json.toString());

        Message message = JsonConverter.fromMessageJsonString(json.toString());
        input.close();
        return message;
    }

    private Message getMessage(MessageType type) {
        Message msg = new Message();
        msg.setSenderUuid(stateService.getDeviceUUID().toString());
        msg.setType(type.toString());
        return msg;
    }

    public void sendRegisterClient(ConnectionDetails connectionInformation, String username) {
        Message msg = getMessage(MessageType.REGISTER_CLIENT);
        msg.setSenderUsername(username);

        sendMessage(msg, connectionInformation);
    }

    public void sendCastSkipSongVote(ConnectionDetails connectionInformation, Song song) {
        Message msg = getMessage(MessageType.CAST_SKIP_SONG_VOTE);
        msg.setSongDetails(song);

        sendMessage(msg, connectionInformation);
    }

    public void sendRemoveSkipSongVote(ConnectionDetails connectionInformation, Song song) {
        Message msg = getMessage(MessageType.REMOVE_SKIP_SONG_VOTE);
        msg.setSongDetails(song);

        sendMessage(msg, connectionInformation);
    }

    public void sendKeepAlive(ConnectionDetails connectionInformation) {
        Message msg = getMessage(MessageType.KEEP_ALIVE);

        sendMessage(msg, connectionInformation);
    }

    public void sendRequestSong(ConnectionDetails connectionInformation, Song song) {
        Message msg = getMessage(MessageType.REQUEST_SONG);
        msg.setSongDetails(song);

        sendMessage(msg, connectionInformation);
    }

    public void sendUnRegisterClient(ConnectionDetails connectionInformation) {
        Message msg = getMessage(MessageType.UNREGISTER_CLIENT);

        sendMessage(msg, connectionInformation);
    }

    public void sendSessionUpdate(ConnectionDetails connectionInformation, Session session) {
        Message msg = getMessage(MessageType.SESSION_UPDATE);
        msg.setSession(session);

        sendMessage(msg, connectionInformation);
    }

    public void sendError(ConnectionDetails connectionInformation, String error) {
        Message msg = getMessage(MessageType.ERROR);
        msg.setErrorMessage(error);

        sendMessage(msg, connectionInformation);
    }

    public void sendFinishSession(ConnectionDetails connectionInformation) {
        Message msg = getMessage(MessageType.FINISH_SESSION);

        sendMessage(msg, connectionInformation);
    }

    /**
     * Send a message to a host.
     *
     * @param msg     The message to be sent.
     * @param preview The connection info about the session
     *                If null, no response is expected.
     */
    private void sendMessage(Message msg,
                             ConnectionDetails preview) {
        AsyncSender asyncSender = new AsyncSender(msg, preview.getHostIP(), preview.getHostPort());
        asyncSender.execute();
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
            Intent intent = null;
            try {
                Socket socket = new Socket(hostIP, hostPort);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                // send message
                printWriter.print(JsonConverter.toJsonString(message));
                printWriter.flush();
                printWriter.close();

                // await response

                socket.setSoTimeout(2000);
                StringBuilder json = new StringBuilder();
                char[] buffer = new char[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    json.append(new String(buffer, 0, bytesRead));
                }
                Log.d(LOG_TAG, "response from host: " + json.toString());

                intent = new Intent(NetworkService.RESPONSE_RECEIVED);
                intent.putExtra("json", json.toString());


                input.close();
                socket.close();
            } catch (IOException e) {
                Log.d(LOG_TAG, "exception occurred " + e.toString());
            } finally {
                if (intent == null) {
                    intent = new Intent(NetworkService.RESPONSE_FAILURE);
                }
            }

            sendBroadcast(intent);

            return null;
        }
    }
}
