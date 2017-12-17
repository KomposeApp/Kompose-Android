package ch.ethz.inf.vs.kompose.service.host;


import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.handler.IncomingMessageHandler;

public class BumperTask implements Runnable {

    private final String LOG_TAG = "##BumperTask";
    private final int KICK_DELAY = 20000;
    private final int GRACE_PERIOD = 30000;

    private final Context ctx;

    public BumperTask(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public void run() {

        SessionModel activeSession = StateSingleton.getInstance().getActiveSession();
        UUID deviceUUID = StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID();

        while(!Thread.interrupted()) {
            Log.d(LOG_TAG, "Checking for clients to kick out of Session...");
            for(ClientModel clientModel : new ArrayList<>(activeSession.getClients())){

                if (clientModel.getClientConnectionDetails() == null){
                    if (!clientModel.getUUID().equals(deviceUUID))
                        Log.w(LOG_TAG, "WARNING: Connection Details of Client " + clientModel.getName() + " were null!");
                    continue;
                }

                DateTime lastMessageDate = clientModel.getClientConnectionDetails().getLastRequestReceived();
                if (!clientModel.getUUID().equals(deviceUUID) &&
                        (lastMessageDate.compareTo(DateTime.now().minus(GRACE_PERIOD))< 0)){
                    Log.d(LOG_TAG, "Kicking Client with the name: " + clientModel.getName() +
                            "who has the UUID : " + clientModel.getUUID());
                    Message message = new Message();
                    message.setType(MessageType.UNREGISTER_CLIENT.toString());
                    message.setSenderUsername(clientModel.getName());
                    message.setSenderUuid(clientModel.getUUID().toString());
                    Thread handler = new Thread(new IncomingMessageHandler(ctx, message));
                    handler.start();
                } else{
                    Log.d(LOG_TAG, "Client with the name: " + clientModel.getName() +
                            "who has the UUID : " + clientModel.getUUID() + " may live");
                }

            }

            try {
                Thread.sleep(KICK_DELAY);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Client Bumper was interrupted");
                e.printStackTrace();
                break;
            }
        }
    }
}
