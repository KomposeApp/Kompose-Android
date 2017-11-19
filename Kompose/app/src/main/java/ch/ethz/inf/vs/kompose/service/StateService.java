package ch.ethz.inf.vs.kompose.service;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;

public class StateService {

    public UUID deviceUUID;
    public String localUsername;
    public SessionModel liveSession;
    public boolean deviceIsHost;

    private StateService() {}

    private static class LazyHolder {
        static final StateService INSTANCE = new StateService();
    }

    public static StateService getInstance() {
        return LazyHolder.INSTANCE;
    }
}
