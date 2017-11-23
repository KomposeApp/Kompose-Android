package ch.ethz.inf.vs.kompose.service;

import ch.ethz.inf.vs.kompose.model.SessionModel;

public class StateSingleton {

    public SessionModel activeSession;

    private StateSingleton() {}

    private static class LazyHolder {
        static final StateSingleton INSTANCE = new StateSingleton();
    }

    public static StateSingleton getInstance() {
        return LazyHolder.INSTANCE;
    }
}
