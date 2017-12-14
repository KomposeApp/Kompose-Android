package ch.ethz.inf.vs.kompose.service.client;

import android.app.Service;
import android.content.Intent;
import android.databinding.ObservableList;
import android.net.nsd.NsdManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.youview.tinydnssd.DiscoverResolver;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.client.listeners.ClientServiceListener;
import ch.ethz.inf.vs.kompose.service.client.listeners.KomposeResolveListenerWorkaround;

public class NSDListenerService extends Service {

    private final String LOG_TAG = "##NSDListenerService";

    private IBinder binder = new LocalBinder();

    public static final String SERVICE_NAME = "Kompose";
    public static final String SERVICE_TYPE = "_kompose._tcp";
    public static final String SERVICE_TYPE_NSD = "_kompose._tcp.";

    private DiscoverResolver resolver;
    private NsdManager nsdManager;
    private ClientServiceListener clientServiceListener;


    /**
     * This starts the path where the NSDListenerService does NSD discovery.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public NSDListenerService getService() {
            return NSDListenerService.this;
        }
    }

    /**
     * Add wait for sessions to be added to the provided ObservableList
     * Started when the service is BOUND
     * @param sessionModels Used by the listview to display the available sessions
     */
    public void findNetworkServices(final ObservableList<SessionModel> sessionModels) {
        Log.d(LOG_TAG, "starting service discovery ...");

        // use workaround library for older android versions
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Log.d(LOG_TAG, "using workaround library for service discovery on outdated devices");

            resolver = new DiscoverResolver(this, SERVICE_TYPE,
                    new KomposeResolveListenerWorkaround(sessionModels));
            resolver.start();

        }
        // use standard android API for up-to-date versions
        else {
            Log.d(LOG_TAG, "using standard android NSD for service discovery");

            nsdManager = (NsdManager) this.getSystemService(NSD_SERVICE);
            clientServiceListener = new ClientServiceListener(sessionModels, nsdManager);
            nsdManager.discoverServices(SERVICE_TYPE_NSD, NsdManager.PROTOCOL_DNS_SD, clientServiceListener);
        }
    }

    /**
     * Handles breakdown of NSD listener
     **/
    @Override
    public boolean onUnbind(Intent intent) {

        // break down workaround library for older android versions
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Log.d(LOG_TAG, "Breaking down NSD Service for outdated devices...");
            if (resolver != null) resolver.stop();
        }
        // break down standard android API for up-to-date versions
        else {
            Log.d(LOG_TAG, "Breaking down NSD Service for newer devices...");
            if (nsdManager != null && clientServiceListener != null)
                nsdManager.stopServiceDiscovery(clientServiceListener);
        }

        Log.d(LOG_TAG, "Service has been unbound!");
        return false;
    }

}
