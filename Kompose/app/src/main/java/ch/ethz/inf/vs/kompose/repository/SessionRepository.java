package ch.ethz.inf.vs.kompose.repository;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import ch.ethz.inf.vs.kompose.model.SessionModel;

public class SessionRepository {
    /**
     * creates a new session and broadcasts it on the network
     *
     * @param userName the username the client wants to use
     */
    public static SessionModel startSession(String userName) {
        // TODO
        return new SessionModel();
    }

    /**
     * gets all currently active sessions in the network
     *
     * @return collection of all active sessions
     */
    public static ObservableList<SessionModel> getActiveSessions() {
        // TODO
        return new ObservableArrayList<>();
    }

    /**
     * join one of the session previously retrieved by getActiveSessions
     *
     * @param userName the username the client wants to use
     */
    public static void joinSession(String userName) {
        // TODO
    }

    /**
     * gets all sessions which are persisted on storage
     *
     * @return collection of all saves sessions
     */
    public static ObservableList<SessionModel> getPastSessions() {
        // TODO
        return new ObservableArrayList<>();
    }

    /**
     * leaves the currently active session
     */
    public static void leaveSession() {
        // TODO
    }
}
