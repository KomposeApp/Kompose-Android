package ch.ethz.inf.vs.kompose.repository;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import ch.ethz.inf.vs.kompose.model.SessionModel;

public class SessionRepository {

    /**
     * creates a new session and broadcasts it on the network
     */
    public SessionModel startSession() {
        // TODO
        return new SessionModel();
    }

    /**
     * gets all currently active sessions in the network
     *
     * @return collection of all active sessions
     */
    public ObservableList<SessionModel> getActiveSessions() {
        // TODO
        return new ObservableArrayList<>();
    }

    /**
     * join one of the session previously retrieved by getActiveSessions
     *
     * @param session the session you want to join
     */
    public void joinSession(SessionModel session) {
        // TODO
    }

    /**
     * gets all sessions which are persisted on storage
     *
     * @return collection of all saves sessions
     */
    public ObservableList<SessionModel> getPastSessions() {
        // TODO
        return new ObservableArrayList<>();
    }

    /**
     * leaves the currently active session
     */
    public void leaveSession() {
        // TODO
    }
}
