package ch.ethz.inf.vs.kompose.service;

import java.net.InetAddress;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.PlayListModel;

public class StateService {

    public UUID deviceUUID;
    public InetAddress hostIP;
    public int hostPort;
    public PlayListModel livePlaylist;
}
