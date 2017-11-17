package ch.ethz.inf.vs.kompose.model;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Singleton that stores global state.
 */
public class GlobalState {

    private static GlobalState instance = null;

    public UUID deviceUUID;
    public InetAddress hostIP;
    public int hostPort;

    private GlobalState() {}

    public static synchronized GlobalState getInstance() {
        if (instance == null) {
            instance = new GlobalState();
        }
        return instance;
    }
}
