package ch.ethz.inf.vs.kompose.service;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.Message;

/**
 * Service that provides various network functionality.
 */
public class NetworkService {

    public Message readMessage(Socket connection) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder json = new StringBuilder();

        char[] buffer = new char[1024];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            json.append(new String(buffer, 0, bytesRead));
        }

        input.close();
        return JsonConverter.fromMessageJsonString(json.toString());
    }

    public void sendMessage(Message msg, InetAddress hostIP, int hostPort) {
        AsyncSender asyncSender = new AsyncSender(msg, hostIP, hostPort);
        asyncSender.execute();
    }

    private static class AsyncSender extends AsyncTask<Void,Void,Void> {

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
            return null;
        }
    }
}