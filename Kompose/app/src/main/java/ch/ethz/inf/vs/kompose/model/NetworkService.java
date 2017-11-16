package ch.ethz.inf.vs.kompose.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.data.Converter;
import ch.ethz.inf.vs.kompose.data.Message;

public class NetworkService {
    public static Message readMessage(Socket connection) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String json = "";

        char[] buffer = new char[1024];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            json += new String(buffer, 0, bytesRead);
        }

        input.close();
        Message msg = Converter.fromJsonString(json);
        return msg;
    }
}
