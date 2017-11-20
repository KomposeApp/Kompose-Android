package ch.ethz.inf.vs.kompose.repository;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.io.IOException;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;
import ch.ethz.inf.vs.kompose.service.StorageService;

public class SessionRepository {

    private NetworkService networkService;
    private StorageService storageService;
    private StateService stateService;

    public SessionRepository(NetworkService networkService, StorageService storageService, StateService stateService) {
        this.networkService = networkService;
        this.storageService = storageService;
        this.stateService = stateService;
    }

    /**
     * creates a new session and register the host service on the network
     */
    public SessionModel startSession(String sessionName, String clientName) {
        SessionModel sessionModel = new SessionModel(UUID.randomUUID(), stateService.getDeviceUUID());
        sessionModel.setSessionName(sessionName);

        ClientModel clientModel = new ClientModel(stateService.getDeviceUUID(), sessionModel);
        clientModel.setName(clientName);
        clientModel.setIsActive(true);

        sessionModel.getClients().add(clientModel);

        //todo: start session
        return sessionModel;
    }

    /**
     * join one of the session previously retrieved by getActiveSessions
     *
     * @param session the session you want to join
     */
    public void joinSession(SessionModel session, String deviceName) {
        stateService.setLiveSession(session);
        networkService.sendRegisterClient(session.getConnectionDetails(), deviceName);
    }

    /**
     * leaves the currently active session
     */
    public void leaveSession(SessionModel sessionModel) {

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
     * gets all sessions which are persisted on storage
     *
     * @return collection of all saves sessions
     */
    public ObservableList<SessionModel> getPastSessions() {
        String[] pastSessionStrings = storageService.retrieveAllFiles("session_archive");
        ObservableList<SessionModel> sessions = new ObservableArrayList<>();
        for (int i = 0; i < pastSessionStrings.length; i++) {
            try {
                SessionConverter sessionConverter = new SessionConverter();
                SessionModel sessionModel = sessionConverter.convert(
                        JsonConverter.fromSessionJsonString(pastSessionStrings[i])
                );
                sessions.add(sessionModel);
            } catch (IOException e) {
            }
        }
        return sessions;
    }
}
