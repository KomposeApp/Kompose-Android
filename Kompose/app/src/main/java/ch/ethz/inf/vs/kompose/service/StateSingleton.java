package ch.ethz.inf.vs.kompose.service;

import android.databinding.ObservableList;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;

public class StateSingleton {

    private static final String LOG_TAG = "## SINGLETON HUB:";

    private final String DIRECTORY_ARCHIVE = "session_archive";
    private final int SOCKET_TIMEOUT = 5000;

    // Client specific fields
    public String username;
    public Socket hostConnection;
    public SessionModel activeSession;
    public SessionModel activeHistorySession;
    public UUID deviceUUID = UUID.randomUUID();
    public ObservableList<SessionModel> orderedPastSessions = new ObservableUniqueSortedList<>(new Comparator<SessionModel>() {
        @Override
        public int compare(SessionModel o1, SessionModel o2) {
            return o1.getCreationDateTime().compareTo(o2.getCreationDateTime());
        }
    });

    // Host specific fields
    public boolean deviceIsHost;

    /* * Initialization on-demand holder idiom for Singleton Pattern * */

    private StateSingleton() {}

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

    public int getFixedTimeout(){
        return SOCKET_TIMEOUT;
    }

    public String getDirectoryArchive(){
        return DIRECTORY_ARCHIVE;
    }
}
