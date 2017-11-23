package ch.ethz.inf.vs.kompose.service;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;

public class StateSingleton {

    public SessionModel activeSession;
    public UUID deviceUUID;
    public boolean deviceIsHost;

    private StateSingleton() {}

    private static class LazyHolder {
        static final StateSingleton INSTANCE = new StateSingleton();
    }

    public static StateSingleton getInstance() {
        return LazyHolder.INSTANCE;
    }
}
