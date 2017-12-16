package ch.ethz.inf.vs.kompose.service.host;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;

public class ServerTask implements Runnable{

    private final String LOG_TAG = "##ServerTask";

    private WeakReference<Context> context;
    private ServerSocket serverSocket;

    ServerTask(Context context, ServerSocket socket) {
        this.context = new WeakReference<>(context);
        this.serverSocket = socket;
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "Server ready to receive connections");

        while (!Thread.interrupted()) {
            try {
                final Socket connection = serverSocket.accept();

                Log.d(LOG_TAG, "message received");
                IncomingMessageHandler messageHandler = new IncomingMessageHandler(context.get(), connection);
                Thread msgHandler = new Thread(messageHandler);
                msgHandler.start();
            } catch (IOException io) {
                Log.d(LOG_TAG, "An exception occurred: " + io.getMessage());
            }
        }
        Log.e(LOG_TAG, "HostServerService is now dead");
    }
}
