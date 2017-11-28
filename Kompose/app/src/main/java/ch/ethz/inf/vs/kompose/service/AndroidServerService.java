package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.service.handler.MessageHandler;

/**
 * This class is home to the the host ServerSocket.
 * All requests from clients pass through here.
 */

public class AndroidServerService extends Service {

    private static final String LOG_TAG = "## AndroidServerService";

    private ServerTask serverTask;

    private IBinder binder = new AndroidServerService.LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return binder; }

    public class LocalBinder extends Binder {
        public AndroidServerService getService() {
            return AndroidServerService.this;
        }
    }

    public void acceptConnections() throws IOException{
        // start server task
        serverTask = new ServerTask();
        serverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Service destroyed");
        serverTask.cancel(true);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(LOG_TAG, "task removed");
        stopSelf();
    }

    private static class ServerTask extends AsyncTask<Void, Void, Void> {

        private static final String LOG_TAG = "## ServerTask";

        private ServerSocket srvSocket;

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(LOG_TAG, "Server ready to receive connections");

            try {
                srvSocket = new ServerSocket(StateSingleton.getInstance().hostPort);
            } catch (IOException e) {
                //TODO: Better error handling
                e.printStackTrace();
            }

            while (!this.isCancelled()) {
                try {

                    final Socket connection = srvSocket.accept();

                    Log.d(LOG_TAG, "message received");

                    MessageHandler messageHandler = new MessageHandler(connection);
                    Thread msgHandler = new Thread(messageHandler);
                    msgHandler.start();
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Exception occurred during .accept(), retrying...");
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
            return null;
        }


        //TODO: Put a callback into this so that we can stop the service when it closes
        @Override
        protected void onPostExecute(Void v){
            try {
                srvSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Failed to close server socket listening on port " + srvSocket.getLocalPort());
            }
        }
    }
}
