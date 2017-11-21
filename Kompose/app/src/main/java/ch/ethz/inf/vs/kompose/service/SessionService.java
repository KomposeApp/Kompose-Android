package ch.ethz.inf.vs.kompose.service;

import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.io.IOException;
import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.ClientConverter;
import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.data.network.ConnectionDetails;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;
import ch.ethz.inf.vs.kompose.service.base.BasePreferencesService;

public class SessionService extends BasePreferencesService {

    @Override
    public void onCreate() {
        super.onCreate();
        bindBaseService(NetworkService.class);
        bindBaseService(StorageService.class);
    }

    public static final String CONNECTION_CHANGED_EVENT = "SessionService.CONNECTION_CHANGED_EVENT";

    private SessionModel activeSession;
    private ClientModel activeClient;
    private ObservableList<SessionModel> pastSessions = new ObservableUniqueSortedList<>(new Comparator<SessionModel>() {
        @Override
        public int compare(SessionModel o1, SessionModel o2) {
            return o1.getCreationDateTime().compareTo(o2.getCreationDateTime());
        }
    });

    private boolean isHost = false;

    /**
     * join the active session as a client with the specified name
     *
     * @param clientName the name to use
     */
    private void joinActiveSession(String clientName) {
        activeClient = new ClientModel(getDeviceUUID(), activeSession);
        activeClient.setName(clientName);
        activeClient.setIsActive(true);

        activeSession.getClients().add(activeClient);
    }

    /**
     * inform all other services that the connection has changed
     */
    private void broadcastConnectionChanged() {
        //todo: is this needed
        Intent intent = new Intent(CONNECTION_CHANGED_EVENT);
        if (activeSession == null) {
            intent.putExtra("is_active", false);
        } else {
            intent.putExtra("is_active", true);

            ClientConverter converter = new ClientConverter(activeSession);
            Client client = converter.convert(activeClient);

            ConnectionDetails connectionDetails = activeSession.getConnectionDetails();

            intent.putExtra("active_client", client);
            intent.putExtra("is_host", isHost);
            intent.putExtra("connection_details", connectionDetails);
        }

        sendBroadcast(intent);
    }

    /**
     * creates a new session and register the host service on the network
     */
    public SessionModel startSession(String sessionName, String clientName) {
        isHost = true;

        activeSession = new SessionModel(UUID.randomUUID(), getDeviceUUID());
        activeSession.setSessionName(sessionName);
        joinActiveSession(clientName);

        bindBaseService(AndroidServerService.class);
        Intent serverIntent = new Intent(this, AndroidServerService.class);
        startService(serverIntent);

        broadcastConnectionChanged();

        return activeSession;
    }

    /**
     * join one of the session previously retrieved by getActiveSessions
     *
     * @param session the session you want to join
     */
    public void joinSession(SessionModel session, String clientName) {
        isHost = false;

        activeSession = session;
        joinActiveSession(clientName);

        broadcastConnectionChanged();

        getNetworkService().sendRegisterClient(clientName);
    }

    /**
     * leaves the currently active session
     */
    public void leaveSession(SessionModel sessionModel) {
        if (isHost) {
            getNetworkService().sendFinishSession();
        } else {
            getNetworkService().sendUnRegisterClient();
        }

        isHost = false;
        activeSession = null;
        activeClient = null;
        broadcastConnectionChanged();
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
        String[] pastSessionStrings = getStorageService().retrieveAllFiles("session_archive");
        for (int i = 0; i < pastSessionStrings.length; i++) {
            try {
                SessionConverter sessionConverter = new SessionConverter();
                SessionModel sessionModel = sessionConverter.convert(
                        JsonConverter.fromSessionJsonString(pastSessionStrings[i])
                );
                pastSessions.add(sessionModel);
            } catch (IOException e) {
            }
        }
        return pastSessions;
    }
}
