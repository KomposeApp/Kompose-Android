package ch.ethz.inf.vs.kompose.service.client;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import ch.ethz.inf.vs.kompose.service.SimpleListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;
import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;

public class ClientRegistrationTask implements Runnable {

    private final String LOG_TAG = "##RegistrationTask";
    private final int NUM_RETRIES = 1;
    private final int SOCKET_TIMEOUT = 2500;

    private ServerSocket clientServerSocket;
    private SimpleListener<Boolean, Void> callbackListener;
    private WeakReference<Context> context;
    private String clientName;

    public ClientRegistrationTask(Context ctx, String clientName, SimpleListener<Boolean, Void> callbackListener)
            throws IOException {
        this.context = new WeakReference<>(ctx);
        this.callbackListener = callbackListener;
        this.clientName = clientName;

        this.clientServerSocket = new ServerSocket(StateSingleton.getInstance().getPreferenceUtility().getClientPort());
        this.clientServerSocket.setSoTimeout(SOCKET_TIMEOUT);
    }


    @Override
    public void run() {

        boolean success = false;
        int counter = 0;

        while (counter < NUM_RETRIES && !Thread.interrupted()) {

            new OutgoingMessageHandler(context.get()).sendRegisterClient(clientName, clientServerSocket.getLocalPort());
            try {
                Socket connection = clientServerSocket.accept();
                Log.d(LOG_TAG, "message received");
                IncomingMessageHandler messageHandler = new IncomingMessageHandler(context.get(), connection);
                Thread msgHandler = new Thread(messageHandler);
                msgHandler.start();

                success = true;
            } catch (SocketTimeoutException to) {
                Log.d(LOG_TAG, "Timeout reached");
                counter++;
            } catch (IOException io){
                Log.w(LOG_TAG, "Something went severely wrong");
                io.printStackTrace();
            }
        }

        try {
            clientServerSocket.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to close Registration Socket");
            e.printStackTrace();
        }

        final boolean result = success;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                callbackListener.onEvent(result, null);
            }
        });
    }
}