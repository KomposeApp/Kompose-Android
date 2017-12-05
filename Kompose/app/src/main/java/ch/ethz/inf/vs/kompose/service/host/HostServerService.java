package ch.ethz.inf.vs.kompose.service.host;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;

import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

import static ch.ethz.inf.vs.kompose.MainActivity.SERVICE_NAME;
import static ch.ethz.inf.vs.kompose.MainActivity.SERVICE_TYPE;

/**
 * Android service that starts the server.
 * First the service is registered on the network, then an AsyncTask
 * that accepts connections is started.
 */
public class HostServerService extends Service {

    private final String LOG_TAG = "## HostServerService";

    private NsdManager nsdManager;
    private NsdManager.RegistrationListener nsdRegistrationListener;
    private ServerTask serverTask;
    private ServerSocket serverSocket;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "started");

        int actualPort;
        try {
            int hostPort = StateSingleton.getInstance().getPreferenceUtility().getHostPort();
            serverSocket = new ServerSocket(hostPort);

            // Required in case the port in preferences is 0
            actualPort = serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: Find a way to stop the MainActivity from advancing should this happen
            Log.e(LOG_TAG, "Failed to set up the ServerSocket");
            stopSelf();
            return START_STICKY;
        }

        // register network service
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);

        // Retrieve active session and components
        SessionModel activeSession = StateSingleton.getInstance().getActiveSession();
        if (activeSession == null) {
            //idk what to do here
            return Service.START_REDELIVER_INTENT;
        }

        // set connection details for "Host info" in PlaylistActivity
        try {
            activeSession.setConnectionDetails(new ServerConnectionDetails(
                    InetAddress.getByName(getIPAddress(true)), serverSocket.getLocalPort()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sessionName = activeSession.getName();
        String uuid = activeSession.getUUID().toString();
        String hostUuid = activeSession.getHostUUID().toString();
        String hostName = StateSingleton.getInstance().getPreferenceUtility().getUsername();

        //Safety restrictions
        sessionName = sessionName.substring(0, Math.min(255, sessionName.length()));
        uuid = uuid.substring(0, Math.min(255, uuid.length()));
        hostUuid = hostUuid.substring(0, Math.min(255, hostUuid.length()));

        // Set ServiceInfo attributes for NSD
        serviceInfo.setAttribute("session", sessionName);
        serviceInfo.setAttribute("uuid", uuid);
        serviceInfo.setAttribute("host_uuid", hostUuid);
        serviceInfo.setAttribute("host_name", hostName);

        // Prepare NSD sender
        Log.d(LOG_TAG, "using port: " + actualPort);
        serviceInfo.setPort(actualPort);
        nsdRegistrationListener = new ServerRegistrationListener();
        nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        if (nsdManager != null)
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener);
        else {
            //TODO: Failure case if this happens, same issue as above
        }

        // start server task
        serverTask = new ServerTask(this, serverSocket);
        serverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nsdManager != null) {
            Log.d(LOG_TAG, "Shutting down the NSD Sender");
            nsdManager.unregisterService(nsdRegistrationListener);
        }
        if (serverTask != null && !serverTask.isCancelled()) {
            Log.d(LOG_TAG, "Shutting down the Message Server");
            serverTask.cancel(true);
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Closing the ServerSocket failed.");
                e.printStackTrace();
            }
            serverTask = null;
        }
    }

    //TODO: Decide whether this can be removed or not
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(LOG_TAG, "task removed");
        stopSelf();
    }

    /**
     * Listener for the registration of the NSD Service
     */
    private class ServerRegistrationListener implements NsdManager.RegistrationListener {

        @Override
        public void onServiceRegistered(NsdServiceInfo arg0) {
            Log.d(LOG_TAG, "Service registered: " + arg0.getServiceName());
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(LOG_TAG, "Service registration failed: " + errorCode);
            //TODO: Error handling in conjunction with the activity
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
            Log.d(LOG_TAG, "Service unregistered: " + arg0.getServiceName());
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(LOG_TAG, "Service unregistration failed: " + errorCode);
        }
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
