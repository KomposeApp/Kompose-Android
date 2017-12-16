package ch.ethz.inf.vs.kompose.service.client.listeners;

import android.databinding.ObservableList;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

/*
 * Standard Android API service discovery with NSD.
 * This only works correctly for API >= 24
 */
public class KomposeResolveListener implements NsdManager.ResolveListener {

    private final String LOG_TAG = "##Resolver";

    private ObservableList<SessionModel> sessionModels;

    KomposeResolveListener(ObservableList<SessionModel> sessionModels) {
        this.sessionModels = sessionModels;
    }

    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.e(LOG_TAG, "Resolve failed. Error Code: " + errorCode);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        Log.d(LOG_TAG, "Resolve Succeeded.");
        Log.d(LOG_TAG, "Session info: " + serviceInfo.toString());

        Map<String, byte[]> attributes = serviceInfo.getAttributes();
        UUID hostUUID = UUID.fromString(new String(attributes.get("host_uuid")));

        if (hostUUID.equals(StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID())){
            Log.d(LOG_TAG, "Session host is us, skipping...");
            return;
        }

        UUID sessionUUID = UUID.fromString(new String(attributes.get("uuid")));
        final SessionModel sessionModel = new SessionModel(sessionUUID, hostUUID);

        int port = serviceInfo.getPort();
        InetAddress host = serviceInfo.getHost();
        String hostName = new String(attributes.get("host_name"));
        String sessionName = new String(attributes.get("session"));

        sessionModel.setName(sessionName);
        sessionModel.setHostName(hostName);
        sessionModel.setConnectionDetails(new ServerConnectionDetails(host, port));

        // the observable list callbacks must be called on the UI thread
        Runnable uiTask = new Runnable() {
            @Override
            public void run() {
                for (SessionModel s : sessionModels) {
                    if (s.getUUID().equals(sessionModel.getUUID())) return;
                }
                sessionModels.add(sessionModel);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(uiTask);
    }
}