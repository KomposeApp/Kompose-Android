package ch.ethz.inf.vs.kompose.data.network;

import java.net.InetAddress;

import ch.ethz.inf.vs.kompose.data.json.Song;

/**
 * Created by git@famoser.ch on 20/11/2017.
 */

public class ConnectionDetails {

    public ConnectionDetails(InetAddress hostIP, int hostPort) {
        this.hostIP = hostIP;
        this.hostPort = hostPort;
    }

    private InetAddress hostIP;
    private int hostPort;


    public InetAddress getHostIP() {
        return hostIP;
    }

    public int getHostPort() {
        return hostPort;
    }
}
