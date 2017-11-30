package ch.ethz.inf.vs.kompose.service.handler;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.data.network.ClientConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

public class IncomingMessageHandler implements Runnable {
    private static final String LOG_TAG = "## InMessageHandler";

    private Socket socket;
    private Message message;

    public IncomingMessageHandler(Socket socket) {
        this.socket = socket;
    }

    public IncomingMessageHandler(Message message) {
        this.message = message;
    }

    /**
     * Read raw String input and transform it to a JSON Message
     *
     * @param connection Socket to read the String from
     * @return JSON Message
     * @throws IOException Occurs in anything goes wrong when reading the input.
     */
    private Message readMessage(Socket connection) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String json = input.readLine();
        Log.d(LOG_TAG, "message read from stream: " + json.toString());

        Message message = JsonConverter.fromMessageJsonString(json.toString());
        input.close();
        return message;
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "Thread dispatched");
        if (socket != null) {
            try {
                message = readMessage(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Can be non-null if initialized with the second constructor
        if (message == null) {
            return;
        }

        SessionModel activeSessionModel = StateSingleton.getInstance().activeSession;

        MessageType messageType = MessageType.valueOf(message.getType());
        Log.d(LOG_TAG, "Message processing (" + messageType + ")");

        ClientModel clientModel = getClientModel(UUID.fromString(message.getSenderUuid()), activeSessionModel);
        if (clientModel != null && clientModel.getClientConnectionDetails() != null) {
            clientModel.getClientConnectionDetails().setLastRequestReceived(DateTime.now());
        }

        if (clientModel == null
                && messageType != MessageType.REGISTER_CLIENT
                && messageType != MessageType.SESSION_UPDATE) {
            //client unknown; therefore not allows to do request
            return;
        }

        boolean sessionHasChanged = false;
        switch (messageType) {
            case REGISTER_CLIENT:
                sessionHasChanged = registerClient(message, activeSessionModel);
                break;
            case UNREGISTER_CLIENT:
                sessionHasChanged = unregisterClient(message, activeSessionModel);
                break;
            case SESSION_UPDATE:
                sessionUpdate(message, activeSessionModel);
                break;
            case REQUEST_SONG:
                sessionHasChanged = requestSong(message, activeSessionModel);
                break;
            case CAST_SKIP_SONG_VOTE:
                sessionHasChanged = castSkipSongVote(message, activeSessionModel);
                break;
            case REMOVE_SKIP_SONG_VOTE:
                sessionHasChanged = removeSkipSongVote(message, activeSessionModel);
                break;
            case KEEP_ALIVE:
                //already handled by refreshClientTimeout before
                break;
            case FINISH_SESSION:
                finishSession();
                break;
            case ERROR:
                //not used so far
                break;
        }

        if (sessionHasChanged) {
            new OutgoingMessageHandler().updateAllClients(activeSessionModel);
        }
    }

    // TODO
    private void finishSession() {
    }

    private boolean registerClient(Message message, SessionModel sessionModel) {
        ClientModel client = new ClientModel(UUID.fromString(message.getSenderUuid()), sessionModel);
        client.setIsActive(true);
        client.setName(message.getSenderUsername());

        // if the message came from network, store the socket in the client model
        if (socket != null) {
            ClientConnectionDetails connectionDetails = new ClientConnectionDetails(
                    socket.getInetAddress(), message.getPort(), DateTime.now());
            client.setClientConnectionDetails(connectionDetails);
        }

        sessionModel.getClients().add(client);

        return true;
    }

    private boolean unregisterClient(Message message, SessionModel sessionModel) {
        ClientModel clientModel = getClientModel(UUID.fromString(message.getSenderUuid()), sessionModel);

        if (clientModel == null) {
            return false;
        }

        // close the client's socket
        clientModel.setIsActive(false);
        clientModel.setClientConnectionDetails(null);

        // remove the client's downvotes
        UUID clientUUID = clientModel.getUUID();
        for (SongModel songModel : sessionModel.getPlayQueue()) {
            for (DownVoteModel downVoteModel : songModel.getDownVotes()) {
                if (downVoteModel.getUuid().equals(clientUUID)) {
                    songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() - 1);
                    checkDownVoteCount(sessionModel, songModel);
                    break;
                }
            }
        }

        return true;
    }

    private boolean requestSong(Message message, final SessionModel sessionModel) {
        Song song = message.getSongDetails();
        song.setProposedByClientUuid(message.getSenderUuid());

        SongConverter songConverter = new SongConverter(sessionModel.getClients());
        final SongModel songModel = songConverter.convert(song);
        songModel.setSongStatus(SongStatus.IN_QUEUE);
        songModel.setOrder(sessionModel.getPlayQueue().size() + 1);

        Runnable uiTask = new Runnable() {
            @Override
            public void run() {

                sessionModel.getPlayQueue().add(songModel);
            }
        };
        new Handler(Looper.getMainLooper()).post(uiTask);
        return true;
    }

    private boolean castSkipSongVote(Message message, SessionModel activeSessionModel) {
        SongModel downVoteTarget = null;
        String requestedSongUUID = message.getSongDetails().getUuid();
        String senderUUID = message.getSenderUuid();

        // find the song in the session model
        for (SongModel song : activeSessionModel.getPlayQueue()) {
            String songUUID = song.getUUID().toString();
            if (requestedSongUUID.equals(songUUID)) {

                // check if song already downvoted by this client
                for (DownVoteModel downVote : song.getDownVotes()) {
                    String downVoteClientUUID = downVote.getClientModel().getUUID().toString();
                    if (downVoteClientUUID.equals(senderUUID)) {
                        return false;
                    }
                }

                downVoteTarget = song;
                break;
            }
        }

        if (downVoteTarget == null) {
            return false;
        }

        DownVoteModel downVoteModel = new DownVoteModel(UUID.randomUUID(),
                getClientModel(UUID.fromString(message.getSenderUuid()), activeSessionModel),
                downVoteTarget);

        downVoteTarget.setValidDownVoteCount(downVoteTarget.getValidDownVoteCount() + 1);
        downVoteTarget.getDownVotes().add(downVoteModel);
        checkDownVoteCount(activeSessionModel, downVoteTarget);

        return false;
    }

    private boolean removeSkipSongVote(Message message, SessionModel activeSessionModel) {
        // find the song in the session
        for (int i = 0; i < activeSessionModel.getPlayQueue().size(); i++) {
            SongModel songModel = activeSessionModel.getPlayQueue().get(i);
            String songUUID = songModel.getUUID().toString();
            String requestedSongUUID = message.getSongDetails().getUuid();
            if (songUUID.equals(requestedSongUUID)) {

                // find the corresponding DownVoteModel
                DownVoteModel downVoteModel;
                for (int j = 0; j < songModel.getDownVotes().size(); j++) {
                    downVoteModel = songModel.getDownVotes().get(j);
                    String downvoteClientUUID = downVoteModel.getClientModel().getUUID().toString();
                    String clientUUID = message.getSenderUuid();
                    if (downvoteClientUUID.equals(clientUUID)) {
                        songModel.getDownVotes().remove(j);
                        checkDownVoteCount(activeSessionModel, songModel);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // TODO
    private void sessionUpdate(Message message, final SessionModel activeSessionModel) {
//        Session receivedSession = message.getSession();
//
//        activeSession.setName(receivedSession.getName());
//        activeSession.setCreationDateTime(receivedSession.getCreationDateTime());
//        activeSession.setHostUuid(receivedSession.getHostUuid());
        Session receivedSession = message.getSession();

        SessionConverter converter = new SessionConverter();
        final SessionModel sessionModel = converter.convert(receivedSession);

        activeSessionModel.setName(sessionModel.getName());
        activeSessionModel.setCreationDateTime(sessionModel.getCreationDateTime());

        /*
         * The host does currently not include its IP/Port in the messages
         */
        // activeSessionModel.setConnectionDetails(sessionModel.getConnectionDetails());

        Runnable uiTask = new Runnable() {
            @Override
            public void run() {
                for (ClientModel updateClient : sessionModel.getClients()) {
                    boolean updated = false;
                    for (ClientModel activeClient : activeSessionModel.getClients()) {
                        if (updateClient.getUUID().equals(activeClient.getUUID())) {
                            updateClient(updateClient, activeClient);
                            updated = true;
                        }
                    }
                    if (!updated) {
                        activeSessionModel.getClients().add(updateClient);
                    }
                }

                for (SongModel updateSong : sessionModel.getPlayQueue()) {
                    boolean updated = false;
                    for (SongModel activeSong : activeSessionModel.getPlayQueue()) {
                        if (updateSong.getUUID().equals(activeSong.getUUID())) {
                            updateSong(updateSong, activeSong);
                            updated = true;
                        }
                    }
                    if (!updated) {
                        activeSessionModel.getPlayQueue().add(updateSong);
                    }
                }
            }
        };
        new Handler(Looper.getMainLooper()).post(uiTask);


        //todo: handle up/downvote changes
    }

    private void updateClient(ClientModel source, ClientModel target) {
        target.setName(source.getName());
        target.setClientConnectionDetails(source.getClientConnectionDetails());
        target.setIsActive(source.getIsActive());
    }

    private void updateSong(SongModel source, SongModel target) {
        target.setCreationDateTime(source.getCreationDateTime());
        target.setTitle(source.getTitle());
        target.setDownloadPath(source.getDownloadPath());
        target.setDownloadUrl(source.getDownloadUrl());
        target.setOrder(source.getOrder());
        target.setSongStatus(source.getSongStatus());
        target.setSourceUrl(source.getSourceUrl());
        target.setThumbnailUrl(source.getThumbnailUrl());
        target.setValidDownVoteCount(source.getValidDownVoteCount());
        target.setSkipVoteCasted(source.getSkipVoteCasted());
        target.setSecondsLength(source.getSecondsLength());
    }

    // find a ClientModel in a session by UUID
    private ClientModel getClientModel(UUID clientUUID, SessionModel sessionModel) {
        for (ClientModel client : sessionModel.getClients()) {
            if (client.getUUID().equals(clientUUID)) {
                return client;
            }
        }
        return null;
    }

    // update the song status according to how many downvotes it has
    private void checkDownVoteCount(SessionModel sessionModel, SongModel songModel) {
        int validClientCount = 0;
        for (ClientModel client : sessionModel.getClients()) {
            if (client.getIsActive()) {
                validClientCount += 1;
            }
        }

        sessionModel.setActiveDevices(validClientCount);

        int quorum = validClientCount / 2;
        if (songModel.getValidDownVoteCount() >= quorum) {
            //add to skipped if not played
            if (sessionModel.getPlayQueue().contains(songModel)) {
                sessionModel.getPlayQueue().remove(songModel);
                sessionModel.getSkippedSongs().add(songModel);
            }
        } else {
            //add to queue if not skipped and still allows to be added
            if (sessionModel.getSkippedSongs().contains(songModel) && songModel.getOrder() < sessionModel.getCurrentlyPlaying().getOrder()) {
                sessionModel.getSkippedSongs().remove(songModel);
                sessionModel.getPlayQueue().add(songModel);
            }
        }
    }
}
