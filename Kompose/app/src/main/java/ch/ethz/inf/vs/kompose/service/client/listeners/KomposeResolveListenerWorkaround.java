package ch.ethz.inf.vs.kompose.service.client.listeners;

import android.databinding.ObservableList;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.youview.tinydnssd.DiscoverResolver;
import com.youview.tinydnssd.MDNSDiscover;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

/*
* Workaround library for API < 24: https://github.com/youviewtv/tinydnssd
* NSD Listener
*/
public class KomposeResolveListenerWorkaround implements DiscoverResolver.Listener {

    private final String LOG_TAG = "##WorkaroundResolver";

    // Observable list obtained from View
    private ObservableList<SessionModel> sessionModels;

    public KomposeResolveListenerWorkaround(ObservableList<SessionModel> sessionModels) {
        this.sessionModels = sessionModels;
    }

    @Override
    public void onServicesChanged(Map<String, MDNSDiscover.Result> services) {
        Log.d(LOG_TAG, "mDNS service changed");

        UUID ourDeviceUUID = StateSingleton.getInstance().getPreferenceUtility().retrieveDeviceUUID();
        final List<SessionModel> newSessions = new ArrayList<>();

        // Find the new services
        for (MDNSDiscover.Result r : services.values()) {
            UUID sessionUUID = UUID.fromString(r.txt.dict.get("uuid"));
            UUID hostUUID = UUID.fromString(r.txt.dict.get("host_uuid"));
            String hostName = r.txt.dict.get("host_name");
            String sessionName = r.txt.dict.get("session");

            if (hostUUID.equals(ourDeviceUUID)){
                Log.d(LOG_TAG, "Session host is us, skipping...");
                continue;
            }

            int port = r.srv.port;
            InetAddress host;
            try {
                host = InetAddress.getByName(r.a.ipaddr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                continue;
            }

            SessionModel sessionModel = new SessionModel(sessionUUID, hostUUID);
            sessionModel.setName(sessionName);
            sessionModel.setHostName(hostName);
            sessionModel.setConnectionDetails(new ServerConnectionDetails(host, port));
            newSessions.add(sessionModel);
        }

        // Replace all found services in the ObservableList
        // Note: the observable list callbacks must be called on the UI thread
        Runnable uiTask = new Runnable() {
            @Override
            public void run() {
                sessionModels.clear();
                sessionModels.addAll(newSessions);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(uiTask);
    }
}
