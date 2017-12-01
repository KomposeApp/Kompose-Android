package ch.ethz.inf.vs.kompose.service;

import android.databinding.ObservableList;

import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.comparators.SessionComparator;
import ch.ethz.inf.vs.kompose.model.comparators.UniqueModelComparator;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;

public class StateSingleton {

    private final String LOG_TAG = "## SINGLETON HUB:";
    private final String DIRECTORY_ARCHIVE = "session_archive";

    // Client specific fields
    public SessionModel activeSession;
    public String currentUsername;
    public SessionModel activeHistorySession;
    public UUID deviceUUID = UUID.randomUUID();

    /* * Initialization on-demand holder idiom for Singleton Pattern * */

    private StateSingleton() {
    }

    private static class LazyHolder {
        static final StateSingleton INSTANCE = new StateSingleton();
    }

    public static StateSingleton getInstance() {
        return LazyHolder.INSTANCE;
    }

    /* *********************************************************************** */

    public UUID getDeviceUUID() {
        return deviceUUID;
    }
}
