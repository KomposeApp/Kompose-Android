package ch.ethz.inf.vs.kompose.service.host;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;

public class ServerTask extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = "## ServerTask";

    private Context context;
    private ServerSocket serverSocket;

    ServerTask(Context context, ServerSocket socket) {
        this.context = context;
        this.serverSocket = socket;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(LOG_TAG, "Server ready to receive connections");

        while (!this.isCancelled()) {
            try {
                final Socket connection = serverSocket.accept();

                Log.d(LOG_TAG, "message received");
                IncomingMessageHandler messageHandler = new IncomingMessageHandler(context, connection);
                Thread msgHandler = new Thread(messageHandler);
                msgHandler.start();
            } catch (IOException io) {
                Log.d(LOG_TAG, "An exception occurred: " + io.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v){
        //Cleanup in case we manage to cancel the Task without the Socket
        try {
            serverSocket.close();
            if (!serverSocket.isClosed()) {
                serverSocket.close();
                Log.d(LOG_TAG, "ServerSocket has been successfully closed");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed to close ServerSocket");
        }
    }
}
