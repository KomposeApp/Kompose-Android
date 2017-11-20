package ch.ethz.inf.vs.kompose.service;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Observer;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.data.network.ConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.patterns.SimpleObserver;

/**
 * Service that provides various network functionality.
 */
public class NetworkService {

    public static int RESPONSE_RECEIVED = 0x1;
    public static int RESPONSE_FAILURE = 0x2;
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

    public void sendRegisterClient(ConnectionDetails connectionInformation, String username, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.REGISTER_CLIENT);
        msg.setSenderUsername(username);

        sendMessage(msg, connectionInformation, simpleObserver);
    }

    public void sendCastSkipSongVote(ConnectionDetails connectionInformation, Song song, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.CAST_SKIP_SONG_VOTE);
        msg.setSongDetails(song);

        sendMessage(msg, connectionInformation, simpleObserver);
    }

    public void sendRemoveSkipSongVote(ConnectionDetails connectionInformation, Song song, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.REMOVE_SKIP_SONG_VOTE);
        msg.setSongDetails(song);

        sendMessage(msg, connectionInformation, simpleObserver);
    }

    public void sendKeepAlive(ConnectionDetails connectionInformation, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.KEEP_ALIVE);

        sendMessage(msg, connectionInformation, simpleObserver);
    }

    public void sendRequestSong(ConnectionDetails connectionInformation, Song song, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.REQUEST_SONG);
        msg.setSongDetails(song);

        sendMessage(msg, connectionInformation, simpleObserver);
    }


    public void sendUnRegisterClient(ConnectionDetails connectionInformation, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.UNREGISTER_CLIENT);

        sendMessage(msg, connectionInformation, simpleObserver);
    }

    public void sendSessionUpdate(ConnectionDetails connectionInformation, Session session, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.SESSION_UPDATE);
        msg.setSession(session);

        sendMessage(msg, connectionInformation, simpleObserver);
    }

    public void sendError(ConnectionDetails connectionInformation, String error, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.ERROR);
        msg.setErrorMessage(error);

        sendMessage(msg, connectionInformation, simpleObserver);
    }

    public void sendFinishSession(ConnectionDetails connectionInformation, SimpleObserver simpleObserver) {
        Message msg = getMessage(MessageType.FINISH_SESSION);

        sendMessage(msg, connectionInformation, simpleObserver);
    }

    /**
     * Send a message to a host.
     *
     * @param msg              The message to be sent.
     * @param preview          The connection info about the session
     * @param responseObserver An observer that will get notified when a response is received.
     *                         If null, no response is expected.
     */
    private void sendMessage(Message msg,
                             ConnectionDetails preview,
                             SimpleObserver responseObserver) {
        AsyncSender asyncSender = new AsyncSender(msg, preview.getHostIP(), preview.getHostPort(), responseObserver);
        asyncSender.execute();
    }

    private static class AsyncSender extends AsyncTask<Void, Void, Void> {

        InetAddress hostIP;
        int hostPort;
        Message message;
        SimpleObserver responseObserver;

        public AsyncSender(Message msg, InetAddress ip, int port, SimpleObserver responseObserver) {
            this.message = msg;
            this.hostIP = ip;
            this.hostPort = port;
            this.responseObserver = responseObserver;
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

                // await response
                if (responseObserver != null) {
                    socket.setSoTimeout(2000);
                    StringBuilder json = new StringBuilder();
                    char[] buffer = new char[1024];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        json.append(new String(buffer, 0, bytesRead));
                    }
                    Log.d("## NetworkService", "response from host: " + json.toString());
                    Message response = JsonConverter.fromMessageJsonString(json.toString());
                    responseObserver.notify(RESPONSE_RECEIVED, response);
                }

                input.close();
                socket.close();
            } catch (IOException e) {
                if (responseObserver != null) {
                    responseObserver.notify(RESPONSE_FAILURE, null);
                }
            }
            return null;
        }
    }
}
