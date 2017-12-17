package ch.ethz.inf.vs.kompose.service.client;


import android.content.Context;
import android.util.Log;

import ch.ethz.inf.vs.kompose.service.handler.OutgoingMessageHandler;


public class ClientKeepAliveSender implements Runnable {

    private static final String LOG_TAG = "#KeepAliver";
    private Context ctx;
    private final int SEND_DELAY = 9000;

    public ClientKeepAliveSender(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public void run() {

        while (!Thread.interrupted()) {
            Log.d(LOG_TAG, "Sending Keepalive Message");
            new OutgoingMessageHandler(ctx).sendKeepAlive();

            try {
                Thread.sleep(SEND_DELAY);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Keepaliver was interrupted");
                e.printStackTrace();
                break;
            }
        }
    }
}
