package ch.ethz.inf.vs.kompose.service.client.listeners;

import android.databinding.ObservableList;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import ch.ethz.inf.vs.kompose.model.SessionModel;

import static ch.ethz.inf.vs.kompose.MainActivity.SERVICE_TYPE_NSD;

    /*
     * Used in conjunction with standard Android API service discovery with NSD.
     * This only works correctly for API >= 24
     */
    public class ClientServiceListener implements NsdManager.DiscoveryListener {

        private final String LOG_TAG = "##DiscoveryListener";

        private ObservableList<SessionModel> sessionModels;
        private NsdManager nsdManager;

        public ClientServiceListener(ObservableList<SessionModel> models, NsdManager nsdManager) {
            this.sessionModels = models;
            this.nsdManager = nsdManager;
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(LOG_TAG, "starting service discovery failed. Error Code: " + errorCode);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(LOG_TAG, "stopping service discovery failed. Error Code: " + errorCode);
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.d(LOG_TAG, "service discovery started");
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.d(LOG_TAG, "service discovery stopped");
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "service found: " + serviceInfo.toString());
            if (!serviceInfo.getServiceType().equals(SERVICE_TYPE_NSD)) {
                Log.d(LOG_TAG, serviceInfo.getServiceType());
                return;
            }
            nsdManager.resolveService(serviceInfo,
                    new KomposeResolveListener(sessionModels));
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.d(LOG_TAG, "service lost: " + serviceInfo.toString());
        }

    }
