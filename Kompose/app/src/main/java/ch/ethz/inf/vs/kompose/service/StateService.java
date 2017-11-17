package ch.ethz.inf.vs.kompose.service;

import java.net.InetAddress;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.ObservablePlaylist;

/**
 * Singleton that stores global state.
 */
public class StateService {

    private static StateService instance = null;

    public UUID deviceUUID;
    public InetAddress hostIP;
    public int hostPort;
    public ObservablePlaylist livePlaylist;

    private StateService() {}

    public static synchronized StateService getInstance() {
        if (instance == null) {
            instance = new StateService();
        }
        return instance;
    }
}
