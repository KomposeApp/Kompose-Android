package ch.ethz.inf.vs.kompose.service.handler;

import android.content.Context;
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
    private final String LOG_TAG = "## InMessageHandler";
    public final String SESSION_UPDATED_EVENT = "IncomingMessageHandler.SESSION_UPDATED_EVENT";

    private Socket socket;
    private Message message;
    private Context context;

    private IncomingMessageHandler(Context context) {
        this.context = context;
    }

    public IncomingMessageHandler(Context context, Socket socket) {
        this(context);
        this.socket = socket;
    }

    public IncomingMessageHandler(Context context, Message message) {
        this(context);
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

        final SessionModel activeSessionModel = StateSingleton.getInstance().getActiveSession();
        final ClientModel myClientModel = StateSingleton.getInstance().getActiveClient();

        final MessageType messageType = MessageType.valueOf(message.getType());
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
                sessionUIChanges = sessionUpdate(message, activeSessionModel, myClientModel);
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
                    finalSessionUIChanges.run();
                    adaptLists(activeSessionModel);

                    if (messageType != MessageType.SESSION_UPDATE && activeSessionModel.getIsHost()) {
                        //if the processed request was not session update, we update the session
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new OutgoingMessageHandler(context).sendSessionUpdate();
                            }
                        });
                        t.run();
                    }

                    //only persist if not host; host already saves after the session update (so we don't need to convert twice)
                    boolean isHost = StateSingleton.getInstance().getActiveSession().getIsHost();
                    if (!isHost && messageType == MessageType.SESSION_UPDATE) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new StorageHandler(context).persist(message.getSession());
                            }
                        });
                        t.run();
                    }
                }
            });
        }
    }

    private void adaptLists(SessionModel sessionModel) {
        boolean playingSet = false;
        int quorum = sessionModel.getActiveDevices() / 2;
        for (SongModel songModel : sessionModel.getAllSongs()) {
            if (songModel.getSongStatus().equals(SongStatus.PLAYED) || songModel.getSongStatus().equals(SongStatus.SKIPPED_BY_POPULAR_VOTE) || songModel.getSongStatus().equals(SongStatus.SKIPPED_BY_ERROR)) {
                //in played queue
                sessionModel.getPastSongs().add(songModel);
                if (sessionModel.getPlayQueue().contains(songModel)) {
                    sessionModel.getPlayQueue().remove(songModel);
                }
                if (sessionModel.getPlayQueueWithDislikedSongs().contains(songModel)) {
                    sessionModel.getPlayQueueWithDislikedSongs().remove(songModel);
                }
            } else if (songModel.getSongStatus().equals(SongStatus.PLAYING) || songModel.getSongStatus().equals(SongStatus.PAUSED)) {
                playingSet = true;
                if (sessionModel.getPlayQueue().contains(songModel)) {
                    sessionModel.getPlayQueue().remove(songModel);
                }
                if (sessionModel.getPlayQueueWithDislikedSongs().contains(songModel)) {
                    sessionModel.getPlayQueueWithDislikedSongs().remove(songModel);
                }
                if (sessionModel.getPastSongs().contains(songModel)) {
                    sessionModel.getPastSongs().remove(songModel);
                }
                if (songModel.getValidDownVoteCount() > quorum) {
                    songModel.setSongStatus(SongStatus.SKIPPED_BY_POPULAR_VOTE);
                    sessionModel.setCurrentlyPlaying(null);
                } else {
                    sessionModel.setCurrentlyPlaying(songModel);
                }
            } else if (songModel.getSongStatus().equals(SongStatus.IN_QUEUE) || songModel.getSongStatus().equals(SongStatus.REQUESTED)) {
                if (sessionModel.getPastSongs().contains(songModel)) {
                    sessionModel.getPastSongs().remove(songModel);
                }

                if (songModel.getValidDownVoteCount() > quorum) {
                    songModel.setSongStatus(SongStatus.SKIPPED_BY_POPULAR_VOTE);
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
                for (SongModel songModel : sessionModel.getAllSongs()) {
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

        for (final SongModel songModel1 : sessionModel.getAllSongs()) {
            if (songModel1.getUUID().equals(songModel.getUUID())) {
                return new Runnable() {
                    @Override
                    public void run() {
                        updateSong(songModel, songModel1);
                    }
                };
            }
        }

        songModel.setSongStatus(SongStatus.IN_QUEUE);
        songModel.setOrder(sessionModel.getAllSongs().size() + 1);

        return new Runnable() {
            @Override
            public void run() {
                sessionModel.getAllSongs().add(songModel);
            }
        };
    }

    private Runnable castSkipSongVote(Message message, SessionModel activeSessionModel) {
        SongModel downVoteTarget = null;
        String requestedSongUUID = message.getSongDetails().getUuid();
        String senderUUID = message.getSenderUuid();
        UUID senderUUIDAsUUID = UUID.fromString(senderUUID);

        // find the song in the session model
        for (SongModel song : activeSessionModel.getAllSongs()) {
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
                finalDownVoteTarget.setSkipVoteCasted(true);
                finalDownVoteTarget.getDownVotes().add(downVoteModel);
            }
        };
    }

    private Runnable removeSkipSongVote(Message message, SessionModel activeSessionModel) {
        // find the song in the session
        for (int i = 0; i < activeSessionModel.getAllSongs().size(); i++) {
            final SongModel songModel = activeSessionModel.getAllSongs().get(i);
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
                                songModel.setSkipVoteCasted(false);
                                songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() - 1);
                                songModel.getDownVotes().remove(finalDownVoteModel);
                            }
                        };
                    }
                }
            }
        }

        return null;
    }

    private Runnable sessionUpdate(Message message, final SessionModel activeSessionModel, final ClientModel myClientModel) {
        Session receivedSession = message.getSession();

        SessionConverter converter = new SessionConverter();
        final SessionModel sessionModel = converter.convert(receivedSession);


        return new Runnable() {
            @Override
            public void run() {
                activeSessionModel.setName(sessionModel.getName());
                activeSessionModel.setCreationDateTime(sessionModel.getCreationDateTime());
                activeSessionModel.setSessionStatus(sessionModel.getSessionStatus());


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

                for (SongModel updateSong : sessionModel.getAllSongs()) {
                    boolean found = false;
                    for (SongModel activeSong : activeSessionModel.getAllSongs()) {
                        if (updateSong.getUUID().equals(activeSong.getUUID())) {
                            updateSong(updateSong, activeSong);
                            found = true;

                            List<DownVoteModel> activeDownVotes = new ArrayList<>(activeSong.getDownVotes());
                            for (DownVoteModel updateDownVote : updateSong.getDownVotes()) {
                                boolean downVoteFound = false;
                                for (int i = 0; i < activeDownVotes.size(); i++) {
                                    if (activeDownVotes.get(i).getUuid().equals(updateDownVote.getUuid())) {
                                        //found; remove it from the list
                                        DownVoteModel downVoteModel = activeDownVotes.get(i);
                                        if (downVoteModel.getClientModel().getUUID().equals(myClientModel.getUUID())) {
                                            updateSong.setSkipVoteCasted(false);
                                        }
                                        activeDownVotes.remove(downVoteModel);
                                        downVoteFound = true;
                                        break;
                                    }
                                }
                                if (!downVoteFound) {
                                    DownVoteModel model = new DownVoteModel(updateDownVote.getUuid(), updateDownVote.getClientModel(), updateDownVote.getDownVoteFor());
                                    if (model.getClientModel().getUUID().equals(myClientModel.getUUID())) {
                                        updateSong.setSkipVoteCasted(true);
                                    }
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
                        activeSessionModel.getAllSongs().add(updateSong);
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
