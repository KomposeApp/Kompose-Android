package ch.ethz.inf.vs.kompose.service;

import android.content.Context;
import android.databinding.ObservableList;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;

public class StateSingleton {

    private static final String LOG_TAG = "## SINGLETON HUB:";

    public static final String CONNECTION_CHANGED_EVENT = "SessionService.CONNECTION_CHANGED_EVENT";
    private final String DIRECTORY_ARCHIVE = "session_archive";

    // Client specific fields
    public SessionModel activeSession;
    public UUID deviceUUID;
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

    public void generateDeviceUUID(Context ctx){

        if (deviceUUID != null){
            Log.d(LOG_TAG, "Device UUID already exists, skipping...");
            return;
        }
        deviceUUID = UUID.randomUUID();
    }

    @Nullable
    public UUID retrieveDeviceUUID(Context ctx){
        if (deviceUUID == null){
            Log.e(LOG_TAG, "ALERT: Device UUID has not been set");
        }
        return deviceUUID;
    }

}
