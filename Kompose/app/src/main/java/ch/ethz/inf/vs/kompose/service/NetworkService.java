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
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.patterns.SimpleObserver;

/**
 * Service that provides various network functionality.
 */
public class NetworkService {

    public static int RESPONSE_RECEIVED = 0x1;
    public static int RESPONSE_FAILURE = 0x2;

    public NetworkService() { }

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

    /**
     * Send a message to a host.
     *
     * @param msg The message to be sent.
     * @param hostIP The hosts IP address.
     * @param hostPort The hosts port.
     * @param responseObserver An observer that will get notified when a response is received.
     *                         If null, no response is expected.
     */
    public void sendMessage(Message msg,
                            InetAddress hostIP,
                            int hostPort,
                            SimpleObserver responseObserver) {
        AsyncSender asyncSender = new AsyncSender(msg, hostIP, hostPort, responseObserver);
        asyncSender.execute();
    }

    private static class AsyncSender extends AsyncTask<Void,Void,Void> {

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
