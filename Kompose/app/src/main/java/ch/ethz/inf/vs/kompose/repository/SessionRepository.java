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
import ch.ethz.inf.vs.kompose.data.Session;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.PlayListModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.AndroidServerService;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;
import ch.ethz.inf.vs.kompose.service.StorageService;

public class SessionRepository {

    private Context context;
    private NetworkService networkService;

    public SessionRepository(Context context,
                             NetworkService networkService) {
        this.context = context;
        this.networkService = networkService;
    }

    public void startSeverService() {
        if (StateService.getInstance().deviceIsHost) {
            Intent serviceIntent = new Intent(context, AndroidServerService.class);
            context.startService(serviceIntent);
        }
    }

    public void stopServerService() {
        if (StateService.getInstance().deviceIsHost) {
            Intent serviceIntent = new Intent(context, AndroidServerService.class);
            context.stopService(serviceIntent);
        }
    }

    /**
     * creates a new session and register the host service on the network
     */
    public SessionModel startSession(String sessionName) {
        SessionModel sessionModel = new SessionModel(UUID.randomUUID(), StateService.getInstance().deviceUUID, null, 0);
        sessionModel.setSessionName(sessionName);

        StateService.getInstance().liveSession = sessionModel;
        StateService.getInstance().deviceIsHost = true;

        startSeverService();
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
        msg.setSenderUsername(StateService.getInstance().localUsername);
        msg.setSenderUuid(StateService.getInstance().deviceUUID.toString());

        networkService.sendMessage(msg, session.getHostIP(), session.getHostPort(), null);
    }

    /**
     * leaves the currently active session
     */
    public void leaveSession() {
        Message msg = new Message();
        msg.setType(MessageType.UNREGISTER_CLIENT.toString());
        msg.setSenderUsername(StateService.getInstance().localUsername);
        msg.setSenderUuid(StateService.getInstance().deviceUUID.toString());

        networkService.sendMessage(msg, StateService.getInstance().liveSession.getHostIP(),
                StateService.getInstance().liveSession.getHostPort(), null);
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
