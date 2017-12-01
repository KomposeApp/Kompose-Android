package ch.ethz.inf.vs.kompose.service.handler;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.DownVote;
import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.data.network.ClientConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
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

        final SessionModel activeSessionModel = StateSingleton.getInstance().activeSession;

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

        Runnable sessionUIChanges = null;
        switch (messageType) {
            case REGISTER_CLIENT:
                sessionUIChanges = registerClient(message, activeSessionModel);
                break;
            case UNREGISTER_CLIENT:
                sessionUIChanges = unregisterClient(message, activeSessionModel);
                break;
            case SESSION_UPDATE:
                sessionUIChanges = sessionUpdate(message, activeSessionModel);
                break;
            case REQUEST_SONG:
                sessionUIChanges = requestSong(message, activeSessionModel);
                break;
            case CAST_SKIP_SONG_VOTE:
                sessionUIChanges = castSkipSongVote(message, activeSessionModel);
                break;
            case REMOVE_SKIP_SONG_VOTE:
                sessionUIChanges = removeSkipSongVote(message, activeSessionModel);
                break;
            case KEEP_ALIVE:
                //already handled by refreshClientTimeout before
                break;
            case FINISH_SESSION:
                sessionUIChanges = finishSession(activeSessionModel);
                break;
            case ERROR:
                //not used so far
                break;
        }

        if (sessionUIChanges != null) {
            // Queue this on the  UI thread to avoid a race condition where updateAllClients
            // would be called *before* the object actually gets updated in a previously posted
            // UI task (e.g. in `requestSong`)
            final Runnable finalSessionUIChanges = sessionUIChanges;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adaptLists(activeSessionModel);
                    finalSessionUIChanges.run();

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new OutgoingMessageHandler().updateAllClients(activeSessionModel);
                        }
                    });
                    t.run();
                }
            });
        }
    }

    private void adaptLists(SessionModel sessionModel) {
        boolean playingSet = false;
        for (SongModel songModel : sessionModel.getAllSongList()) {
            if (songModel.getSongStatus().equals(SongStatus.PLAYED) || songModel.getSongStatus().equals(SongStatus.SKIPPED_BY_POPULAR_VOTE) || songModel.getSongStatus().equals(SongStatus.SKIPPED_BY_ERROR)) {
                //in played queue
                sessionModel.getPastSongs().add(songModel);
                if (sessionModel.getPlayQueue().contains(songModel)) {
                    sessionModel.getPlayQueue().remove(songModel);
                }
                if (sessionModel.getPlayQueueWithDislikedSongs().contains(songModel)) {
                    sessionModel.getPlayQueueWithDislikedSongs().remove(songModel);
                }
            } else if (songModel.getSongStatus().equals(SongStatus.PLAYING)) {
                playingSet = true;
                sessionModel.setCurrentlyPlaying(songModel);
                if (sessionModel.getPlayQueue().contains(songModel)) {
                    sessionModel.getPlayQueue().remove(songModel);
                }
                if (sessionModel.getPlayQueueWithDislikedSongs().contains(songModel)) {
                    sessionModel.getPlayQueueWithDislikedSongs().remove(songModel);
                }
                if (sessionModel.getPastSongs().contains(songModel)) {
                    sessionModel.getPastSongs().remove(songModel);
                }
            } else if (songModel.getSongStatus().equals(SongStatus.IN_QUEUE) || songModel.getSongStatus().equals(SongStatus.REQUESTED)) {
                if (sessionModel.getPastSongs().contains(songModel)) {
                    sessionModel.getPastSongs().remove(songModel);
                }

                int quorum = sessionModel.getActiveDevices() / 2;
                if (songModel.getValidDownVoteCount() >= quorum) {
                    //add to skipped if not played
                    if (sessionModel.getPlayQueue().contains(songModel)) {
                        sessionModel.getPlayQueue().remove(songModel);
                    }
                    sessionModel.getPlayQueueWithDislikedSongs().add(songModel);
                } else {
                    sessionModel.getPlayQueue().add(songModel);
                    sessionModel.getPlayQueueWithDislikedSongs().add(songModel);
                }
            }
        }

        //no currently playing song found; ensure it is null
        if (!playingSet) {
            sessionModel.setCurrentlyPlaying(null);
        }

    }

    private Runnable finishSession(final SessionModel sessionModel) {
        return new Runnable() {
            @Override
            public void run() {
                sessionModel.setSessionStatus(SessionStatus.FINISHED);
            }
        };
    }

    private Runnable registerClient(Message message, final SessionModel sessionModel) {

        final ClientModel client = new ClientModel(UUID.fromString(message.getSenderUuid()), sessionModel);
        client.setIsActive(true);
        client.setName(message.getSenderUsername());

        // if the message came from network, store the socket in the client model
        if (socket != null) {
            ClientConnectionDetails connectionDetails = new ClientConnectionDetails(
                    socket.getInetAddress(), message.getPort(), DateTime.now());
            client.setClientConnectionDetails(connectionDetails);
        }

        return new Runnable() {
            @Override
            public void run() {

                sessionModel.getClients().add(client);
                setActiveDevices(sessionModel);
            }
        };
    }

    private Runnable unregisterClient(Message message, final SessionModel sessionModel) {
        final ClientModel clientModel = getClientModel(UUID.fromString(message.getSenderUuid()), sessionModel);

        if (clientModel == null) {
            return null;
        }

        return new Runnable() {
            @Override
            public void run() {
                clientModel.setIsActive(false);
                clientModel.setClientConnectionDetails(null);

                // remove the client's downvotes
                UUID clientUUID = clientModel.getUUID();
                for (SongModel songModel : sessionModel.getAllSongList()) {
                    for (DownVoteModel downVoteModel : songModel.getDownVotes()) {
                        if (downVoteModel.getUuid().equals(clientUUID)) {
                            songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() - 1);
                            break;
                        }
                    }
                }

                setActiveDevices(sessionModel);
            }
        };
    }

    private Runnable requestSong(Message message, final SessionModel sessionModel) {
        Song song = message.getSongDetails();
        song.setProposedByClientUuid(message.getSenderUuid());

        SongConverter songConverter = new SongConverter(sessionModel.getClients());
        final SongModel songModel = songConverter.convert(song);
        songModel.setSongStatus(SongStatus.IN_QUEUE);
        songModel.setOrder(sessionModel.getAllSongList().size() + 1);

        return new Runnable() {
            @Override
            public void run() {
                sessionModel.getAllSongList().add(songModel);
            }
        };
    }

    private Runnable castSkipSongVote(Message message, SessionModel activeSessionModel) {
        SongModel downVoteTarget = null;
        String requestedSongUUID = message.getSongDetails().getUuid();
        String senderUUID = message.getSenderUuid();
        UUID senderUUIDAsUUID = UUID.fromString(senderUUID);

        // find the song in the session model
        for (SongModel song : activeSessionModel.getAllSongList()) {
            String songUUID = song.getUUID().toString();
            if (requestedSongUUID.equals(songUUID)) {

                // check if song already downvoted by this client
                for (DownVoteModel downVote : song.getDownVotes()) {
                    if (downVote.getClientModel().getUUID().equals(senderUUIDAsUUID)) {
                        //already received downvote request
                        return null;
                    }
                }

                downVoteTarget = song;
                break;
            }
        }

        if (downVoteTarget == null) {
            return null;
        }

        final DownVoteModel downVoteModel = new DownVoteModel(UUID.randomUUID(),
                getClientModel(UUID.fromString(message.getSenderUuid()), activeSessionModel),
                downVoteTarget);

        downVoteTarget.setValidDownVoteCount(downVoteTarget.getValidDownVoteCount() + 1);

        final SongModel finalDownVoteTarget = downVoteTarget;
        return new Runnable() {
            @Override
            public void run() {
                finalDownVoteTarget.getDownVotes().add(downVoteModel);
            }
        };
    }

    private Runnable removeSkipSongVote(Message message, SessionModel activeSessionModel) {
        // find the song in the session
        for (int i = 0; i < activeSessionModel.getAllSongList().size(); i++) {
            final SongModel songModel = activeSessionModel.getAllSongList().get(i);
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
                        final DownVoteModel finalDownVoteModel = downVoteModel;
                        return new Runnable() {
                            @Override
                            public void run() {
                                songModel.getDownVotes().remove(finalDownVoteModel);
                            }
                        };
                    }
                }
            }
        }

        return null;
    }

    private Runnable sessionUpdate(Message message, final SessionModel activeSessionModel) {
        Session receivedSession = message.getSession();

        SessionConverter converter = new SessionConverter();
        final SessionModel sessionModel = converter.convert(receivedSession);


        return new Runnable() {
            @Override
            public void run() {
                activeSessionModel.setName(sessionModel.getName());
                activeSessionModel.setCreationDateTime(sessionModel.getCreationDateTime());


                for (ClientModel updateClient : sessionModel.getClients()) {
                    boolean found = false;
                    for (ClientModel activeClient : activeSessionModel.getClients()) {
                        if (updateClient.getUUID().equals(activeClient.getUUID())) {
                            updateClient(updateClient, activeClient);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        activeSessionModel.getClients().add(updateClient);
                    }
                }

                for (SongModel updateSong : sessionModel.getAllSongList()) {
                    boolean found = false;
                    for (SongModel activeSong : activeSessionModel.getAllSongList()) {
                        if (updateSong.getUUID().equals(activeSong.getUUID())) {
                            updateSong(updateSong, activeSong);
                            found = true;

                            List<DownVoteModel> activeDownVotes = new ArrayList<>(activeSong.getDownVotes());
                            for (DownVoteModel updateDownVote : updateSong.getDownVotes()) {
                                boolean downVoteFound = false;
                                for (int i = 0; i < activeDownVotes.size(); i++) {
                                    if (activeDownVotes.get(i).getUuid().equals(updateDownVote.getUuid())) {
                                        //found; remove it from the list
                                        activeDownVotes.remove(activeDownVotes.get(i));
                                        downVoteFound = true;
                                        break;
                                    }
                                }
                                if (!downVoteFound) {
                                    DownVoteModel model = new DownVoteModel(updateDownVote.getUuid(), updateDownVote.getClientModel(), updateDownVote.getDownVoteFor());
                                    activeSong.getDownVotes().add(model);
                                }
                            }

                            //remove all still contained down votes because they have not been found
                            for (DownVoteModel activeDownVote : activeDownVotes) {
                                activeSong.getDownVotes().remove(activeDownVote);
                            }

                        }
                    }
                    if (!found) {
                        activeSessionModel.getAllSongList().add(updateSong);
                    }
                }
            }
        };
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
    private void setActiveDevices(SessionModel sessionModel) {
        int validClientCount = 0;
        for (ClientModel client : sessionModel.getClients()) {
            if (client.getIsActive()) {
                validClientCount += 1;
            }
        }

        sessionModel.setActiveDevices(validClientCount);
    }
}
