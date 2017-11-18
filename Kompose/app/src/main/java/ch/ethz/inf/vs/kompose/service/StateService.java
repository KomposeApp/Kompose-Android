package ch.ethz.inf.vs.kompose.service;

import java.net.InetAddress;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.PlayListModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;

public class StateService {

    public UUID deviceUUID;
    public String localUsername;
    public SessionModel liveSession;
}
