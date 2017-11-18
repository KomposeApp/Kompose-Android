package ch.ethz.inf.vs.kompose.repository;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.io.IOException;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.PlayListModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.AndroidServerService;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;
import ch.ethz.inf.vs.kompose.service.StorageService;

public class SessionRepository {

    private Context context;
    private StateService stateService;
    private NetworkService networkService;

    public SessionRepository(Context context,
                             StateService stateService,
                             NetworkService networkService) {
        this.context = context;
        this.stateService = stateService;
        this.networkService = networkService;
    }

    /**
     * creates a new session and register the host service on the network
     */
    public SessionModel startSession(String sessionName) {
        // start server service
        Intent serviceIntent = new Intent(context, AndroidServerService.class);
        context.startService(serviceIntent);

        SessionModel sessionModel = new SessionModel(UUID.randomUUID(),null, 0);
        sessionModel.setHostUUID(stateService.deviceUUID);
        sessionModel.setSessionName(sessionName);
        sessionModel.setPlaylist(new PlayListModel());

        stateService.liveSession = sessionModel;

        return sessionModel;
    }

    /**
     * join one of the session previously retrieved by getActiveSessions
     *
     * @param session the session you want to join
     */
    public void joinSession(SessionModel session) {
        Message msg = new Message();
        msg.setType(MessageType.REGISTER_CLIENT.toString());
        msg.setSenderUsername(stateService.localUsername);
        msg.setSenderUuid(stateService.deviceUUID.toString());

        networkService.sendMessage(msg, session.getHostIP(), session.getHostPort());
    }

    /**
     * leaves the currently active session
     */
    public void leaveSession() {
        Message msg = new Message();
        msg.setType(MessageType.UNREGISTER_CLIENT.toString());
        msg.setSenderUsername(stateService.localUsername);
        msg.setSenderUuid(stateService.deviceUUID.toString());

        networkService.sendMessage(msg, stateService.liveSession.getHostIP(),
                stateService.liveSession.getHostPort());
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
        // TODO: is this fast enough?
        StorageService storageService = new StorageService(context);
        String[] pastSessionStrings = storageService.retrieveAllFiles("session_archive");
        ObservableList<SessionModel> sessions = new ObservableArrayList<>();
        for (int i = 0; i < pastSessionStrings.length; i++) {
            try {
                SessionModel sessionModel = SessionConverter.convert(
                        JsonConverter.fromSessionJsonString(pastSessionStrings[i]));
                sessions.add(sessionModel);
            } catch (IOException e) { }
        }
        return sessions;
    }
}
