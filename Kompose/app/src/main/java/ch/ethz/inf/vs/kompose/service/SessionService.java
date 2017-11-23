package ch.ethz.inf.vs.kompose.service;

import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.io.IOException;
import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;
import ch.ethz.inf.vs.kompose.preferences.BasePreferencesService;

public class SessionService {

    public static final String CONNECTION_CHANGED_EVENT = "SessionService.CONNECTION_CHANGED_EVENT";
    private static final String DIRECTORY_ARCHIVE = "session_archive";

    private SessionModel activeSessionModel;

    private ClientModel activeClient;

    private Session activeSession;

    private boolean isHost = false;

    /** Sessions are sorted by date first created **/
    private ObservableList<SessionModel> pastSessions = new ObservableUniqueSortedList<>(new Comparator<SessionModel>() {
        @Override
        public int compare(SessionModel o1, SessionModel o2) {
            return o1.getCreationDateTime().compareTo(o2.getCreationDateTime());
        }
    });

    /**
     * join the active session as a client with the specified name
     *
     * @param clientName the name to use
     */
    private void joinActiveSession(String clientName) {
        activeClient = new ClientModel(getDeviceUUID(), activeSessionModel);
        activeClient.setName(clientName);
        activeClient.setIsActive(true);

        activeSessionModel.getClients().add(activeClient);

        SessionConverter sessionConverter = new SessionConverter();
        activeSession = sessionConverter.convert(activeSessionModel);

        broadcastConnectionChanged();
    }

    /**
     * creates a new session and register the host service on the network
     */
    public SessionModel startSession(String sessionName, String clientName) {
        isHost = true;

        activeSessionModel = new SessionModel(UUID.randomUUID(), getDeviceUUID());
        activeSessionModel.setSessionName(sessionName);
        joinActiveSession(clientName);

        Intent serverIntent = new Intent(this, AndroidServerService.class);
        startService(serverIntent);

        return activeSessionModel;
    }

    /**
     * join one of the session previously retrieved by getActiveSessions
     *
     * @param session the session you want to join
     */
    public void joinSession(SessionModel session, String clientName) {
        isHost = false;

        activeSessionModel = session;
        joinActiveSession(clientName);

        getNetworkService().sendRegisterClient(clientName);
    }

    /**
     * leaves the currently active session
     */
    public void leaveSession() {
        if (isHost) {
            getNetworkService().sendFinishSession();
        } else {
            getNetworkService().sendUnRegisterClient();
        }

        isHost = false;
        activeSessionModel = null;
        activeClient = null;

        broadcastConnectionChanged();
    }

    public void sessionChanged() {
        if (isHost) {
            getNetworkService().sendSessionUpdate(activeSession);
        }
    }

    /**
     * gets all currently active sessions in the network
     *
     * @return collection of all active sessions
     */
    public ObservableList<SessionModel> getActiveSessions() {
        ObservableArrayList<SessionModel> observableArrayList = new ObservableArrayList<>();
        getClientNetworkService().findNetworkServices(observableArrayList);
        return observableArrayList;
    }

    /**
     * gets all sessions which are persisted on storage
     *
     * @return collection of all saves sessions
     */
    public ObservableList<SessionModel> getPastSessions() {
        String[] pastSessionStrings = getStorageService().retrieveAllFiles(DIRECTORY_ARCHIVE);
        for (String pastSession : pastSessionStrings) {
            try {
                SessionConverter sessionConverter = new SessionConverter();
                SessionModel sessionModel = sessionConverter.convert(
                        JsonConverter.fromSessionJsonString(pastSession)
                );
                pastSessions.add(sessionModel);
            } catch (IOException e) {
            }
        }
        return pastSessions;
    }

    public Session getActiveSession() {
        return activeSession;
    }

    public SessionModel getActiveSessionModel() {
        return activeSessionModel;
    }

    public ClientModel getActiveClient() {
        return activeClient;
    }

    public boolean isHost() {
        return isHost;
    }
}
