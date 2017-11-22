package ch.ethz.inf.vs.kompose.service;

import android.databinding.ObservableArrayList;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class ClientNetworkService extends BaseService{

    private static final String LOG_TAG = "## ClientNetworkService";

    // TODO

    /**
     * Add Network services to the provided ObservableArrayList
     * @param list List which the NetworkServices are to be added to
     */
    public void findNetworkServices(ObservableArrayList<SessionModel> list) {
    }

    public void startClientSocketListener(Socket socket) {
        ClientListenerTask clientListenerTask = new ClientListenerTask(socket);
        clientListenerTask.execute();
    }

    public static class ClientListenerTask extends AsyncTask<Void, Void, Void> {

        private Socket socket;
        private static final String LOG_TAG = "## ClientListenerTask";

        private Message readMessage(Socket connection) throws IOException {
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder json = new StringBuilder();

            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                json.append(new String(buffer, 0, bytesRead));
            }
            Log.d(LOG_TAG, "message read from stream: " + json.toString());

            Message message = JsonConverter.fromMessageJsonString(json.toString());
            input.close();
            return message;
        }

        public ClientListenerTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Message msg = readMessage(socket);
                    if (MessageType.valueOf(msg.getType()) == MessageType.SESSION_UPDATE) {
                        // TODO
                        // call message handler
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}
