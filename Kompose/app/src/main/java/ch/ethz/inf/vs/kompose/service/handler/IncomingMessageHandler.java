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
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

public class IncomingMessageHandler implements Runnable {
    private final String LOG_TAG = "##InMessageHandler";
    public final String SESSION_UPDATED_EVENT = "IncomingMessageHandler.SESSION_UPDATED_EVENT";

    private Socket socket;
    private Message message;
    private Context context;

    /**
     * Constructor for when we read the message from a socket connection.
     * @param context Application Context
     * @param socket Socket to read the message from
     */
    public IncomingMessageHandler(Context context, Socket socket) {
        this.context = context;
        this.socket = socket;
    }

    /**
     * Constructor for when we already have the message, and now need to process it.
     * @param context Application Context
     * @param message Message to process -- originating from ourselves.
     */
    public IncomingMessageHandler(Context context, Message message) {
        this.context = context;
        this.message = message;
    }

    /**
     * Read raw String input and transform it to a JSON Message
     * @param connection Socket to read the String from
     * @return JSON Message
     * @throws IOException Occurs in anything goes wrong when reading the input.
     */
    private Message readMessage(Socket connection) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String json = input.readLine();
        Log.d("#JSON:", "message read from stream: " + json);

        Message message = JsonConverter.fromMessageJsonString(json);
        input.close();
        return message;
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "Message handler dispatched");
        if (socket != null) {
            try {
                message = readMessage(socket);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to read message from socket");
                e.printStackTrace();
            }
        }
        // Can be non-null if initialized with the second constructor
        if (message == null) {
            Log.e(LOG_TAG, "Did not receive a valid message");
            return;
        }

        final SessionModel activeSessionModel = StateSingleton.getInstance().getActiveSession();
        final ClientModel myClientModel = StateSingleton.getInstance().getActiveClient();
        
        final MessageType messageType;
        ClientModel sender;
        try {
             messageType = MessageType.valueOf(message.getType());
             sender = getClientModel(UUID.fromString(message.getSenderUuid()), activeSessionModel);
        }catch (IllegalArgumentException | NullPointerException e){
            Log.w(LOG_TAG, "Received a message with correct format but malformed data.");
            return;
        }

        Log.d(LOG_TAG, "Received a message of type: (" + messageType + ")");
        
        if (sender != null && sender.getClientConnectionDetails() != null) {
            sender.getClientConnectionDetails().setLastRequestReceived(DateTime.now());
        }

        // If we don't know who the sender is, and he doesn't want to register (or update the session), exit
        if (sender == null
                && messageType != MessageType.REGISTER_CLIENT
                && messageType != MessageType.SESSION_UPDATE) {
            Log.w(LOG_TAG, "Unknown sender, invalid messagetype");
            return;
        }

        Runnable sessionUIChanges = null;
        switch (messageType) {
            case REGISTER_CLIENT:
                if (activeSessionModel.getIsHost())
                    sessionUIChanges = registerClient(message, activeSessionModel);
                break;
            case UNREGISTER_CLIENT:
                if (activeSessionModel.getIsHost())
                    sessionUIChanges = unregisterClient(message, activeSessionModel);
                break;
            case SESSION_UPDATE:
                if (!activeSessionModel.getIsHost())
                    sessionUIChanges = sessionUpdate(message, activeSessionModel, myClientModel);
                break;
            case REQUEST_SONG:
                if (activeSessionModel.getIsHost())
                    sessionUIChanges = handleSongRequest(message, activeSessionModel);
                break;
            case CAST_SKIP_SONG_VOTE:
                if (activeSessionModel.getIsHost())
                    sessionUIChanges = addDownVoteToSong(message, activeSessionModel, myClientModel);
                break;
            case REMOVE_SKIP_SONG_VOTE:
                if (activeSessionModel.getIsHost())
                    sessionUIChanges = removeSkipSongVote(message, activeSessionModel, myClientModel);
                break;
            case KEEP_ALIVE:
                //TODO
                //already handled by refreshClientTimeout before
                break;
            case FINISH_SESSION:
                if (!activeSessionModel.getIsHost())
                    sessionUIChanges = finishSession(activeSessionModel);
                break;
            case ERROR:
                //TODO
                //not used so far
                break;
        }

        if (sessionUIChanges != null) {
            // Queue this on the  UI thread to avoid a race condition where updateAllClients
            // would be called *before* the object actually gets updated in a previously posted
            // UI task (e.g. in `handleSongRequest`)
            final Runnable finalSessionUIChanges = sessionUIChanges;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    finalSessionUIChanges.run();
                    adaptLists(activeSessionModel);

                    if (messageType != MessageType.SESSION_UPDATE && activeSessionModel.getIsHost()) {
                        new OutgoingMessageHandler(context).sendSessionUpdate();
                    }
                }
            });
        }
    }



    /**
     * Serves to update the ObservableLists. Must be ran in the main thread.
     * @param sessionModel Session model with which we update the lists
     */
    private void adaptLists(SessionModel sessionModel) {

        if (!(Looper.myLooper() == Looper.getMainLooper())){
            throw new IllegalStateException("adaptLists was not called on the UI thread");
        }

        boolean hasSongPlaying = false;
        ObservableUniqueSortedList<SongModel> playQueueWithDisliked = sessionModel.getPlayQueueWithDislikedSongs();
        ObservableUniqueSortedList<SongModel> pastSongs = sessionModel.getPastSongs();
        ObservableUniqueSortedList<SongModel> playQueue = sessionModel.getPlayQueue();

        int quorum = sessionModel.getActiveDevices() / 2;
        for (SongModel songModel : sessionModel.getAllSongs()) {
            SongStatus currentStatus = songModel.getSongStatus();
            if (currentStatus.equals(SongStatus.FINISHED) || currentStatus.equals(SongStatus.SKIPPED)) {
                pastSongs.add(songModel);
                checkAndRemove(playQueue, songModel);
                checkAndRemove(playQueueWithDisliked, songModel);
            }
            else if (currentStatus.equals(SongStatus.PLAYING) || currentStatus.equals(SongStatus.PAUSED)) {
                hasSongPlaying = true;
                checkAndRemove(playQueue, songModel);
                checkAndRemove(playQueueWithDisliked, songModel);
                checkAndRemove(pastSongs, songModel);
                if (songModel.getValidDownVoteCount() > quorum) {
                    songModel.setSongStatus(SongStatus.SKIPPED);
                    sessionModel.setCurrentlyPlaying(null);
                } else {
                    sessionModel.setCurrentlyPlaying(songModel);
                }
            }
            else if (currentStatus.equals(SongStatus.IN_QUEUE) ||
                    currentStatus.equals(SongStatus.REQUESTED)) {

                checkAndRemove(pastSongs, songModel);
                if (songModel.getValidDownVoteCount() > quorum) {
                    songModel.setSongStatus(SongStatus.SKIPPED);
                    //add to skipped if not played
                    checkAndRemove(playQueue, songModel);
                    playQueueWithDisliked.add(songModel);
                } else {
                    playQueue.add(songModel);
                    playQueueWithDisliked.add(songModel);
                }
            }
        }

        //no currently playing song found; ensure it is null
        if (!hasSongPlaying) {
            sessionModel.setCurrentlyPlaying(null);
        }

    }

    /**
     * Set the session status to finished.
     * @param sessionModel Current Session
     * @return Runnable to be executed on the main thread
     */
    private Runnable finishSession(final SessionModel sessionModel) {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "SESSION FINISHED");
                sessionModel.setSessionStatus(SessionStatus.FINISHED);
            }
        };
    }

    /**
     * Register the given client in the current Session
     * @param message Message containing client details.
     * @param sessionModel Session to register the client in
     * @return Runnable to be executed on the main thread
     */
    private Runnable registerClient(Message message, final SessionModel sessionModel) {

        final ClientModel client = new ClientModel(UUID.fromString(message.getSenderUuid()), sessionModel);
        client.setIsActive(true);
        client.setName(message.getSenderUsername());

        // if the message came from network, store the connectionDetails in the client
        if (socket != null) {
            ClientConnectionDetails connectionDetails = new ClientConnectionDetails(
                    socket.getInetAddress(), message.getPort(), DateTime.now());
            client.setClientConnectionDetails(connectionDetails);
        } else{
            return null;
        }

        return new Runnable() {
            @Override
            public void run() {
                if (!sessionModel.getClients().contains(client)) {
                    sessionModel.getClients().add(client);
                    setActiveDevices(sessionModel);
                    Log.d(LOG_TAG, "Added client " + client.getName() + " to the active clients.");
                }
            }
        };
    }

    /**
     * Unregister the given client in the current Session
     * @param message Message containing client details.
     * @param sessionModel Session to deregister the client from
     * @return Runnable to be executed on the main thread
     */
    private Runnable unregisterClient(Message message, final SessionModel sessionModel) {
        final ClientModel clientModel = getClientModel(UUID.fromString(message.getSenderUuid()), sessionModel);

        if (clientModel == null) {
            return null;
        }

        return new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Removing client " + clientModel.getName() + " from active clients.");
                clientModel.setIsActive(false);
                clientModel.setClientConnectionDetails(null);
                setActiveDevices(sessionModel);

                // remove the client's downvotes
                UUID clientUUID = clientModel.getUUID();
                for (SongModel songModel : sessionModel.getAllSongs()) {
                    for (DownVoteModel downVoteModel : songModel.getDownVotes()) {
                        if (downVoteModel.getUuid().equals(clientUUID)) {
                            songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() - 1);
                            songModel.getDownVotes().remove(downVoteModel);
                            Log.d(LOG_TAG, "Removing downvote of client " +
                                    downVoteModel.getClientModel().getName() +
                                    "from song " + songModel.getTitle());
                            break;
                        }
                    }
                }
                sessionModel.getClients().remove(clientModel);
            }
        };
    }

    /**
     * Handle song requests and add them to the list of songs.
     * @param message Message containing song details
     * @param sessionModel Host session
     * @return Runnable to be executed on the main thread
     */
    private Runnable handleSongRequest(Message message, final SessionModel sessionModel) {
        Song song = message.getSongDetails();
        song.setProposedByClientUuid(message.getSenderUuid());

        final SongModel newSong = new SongConverter(sessionModel.getClients()).convert(song);
        newSong.setSongStatus(SongStatus.IN_QUEUE);
        newSong.setOrder(sessionModel.getAllSongs().size() + 1);

        return new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Adding new Song: " + newSong.getTitle());
                sessionModel.getAllSongs().add(newSong);
            }
        };
    }

    /**
     * Add a downvote for the specified song.
     * @param message Message containing song and downvote info
     * @param activeSessionModel Host session
     * @param clientModel Client model of the host
     * @return Runnable to be executed on the main thread
     */
    private Runnable addDownVoteToSong(Message message, SessionModel activeSessionModel, final ClientModel clientModel) {
        SongModel downVoteTarget = null;
        String requestedSongUUID = message.getSongDetails().getUuid();
        UUID senderUUID = UUID.fromString(message.getSenderUuid());

        // find the song in the session model
        for (SongModel song : activeSessionModel.getAllSongs()) {
            String songUUID = song.getUUID().toString();
            if (requestedSongUUID.equals(songUUID)) {
                // check if song already downvoted by this client
                for (DownVoteModel downVote : song.getDownVotes()) {
                    if (downVote.getClientModel().getUUID().equals(senderUUID)) {
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

        final SongModel finalDownVoteTarget = downVoteTarget;
        return new Runnable() {
            @Override
            public void run() {
                //Since the host doesn't get Session updates, it must set its own skipvote flag
                if (clientModel.getUUID().equals(downVoteModel.getClientModel().getUUID())) {
                    finalDownVoteTarget.setSkipVoteCasted(true);
                }
                finalDownVoteTarget.setValidDownVoteCount(finalDownVoteTarget.getValidDownVoteCount() + 1);
                finalDownVoteTarget.getDownVotes().add(downVoteModel);
                Log.d(LOG_TAG, "Downvote cast for the song: " + finalDownVoteTarget.getTitle());
            }
        };
    }

    /**
     * Redact a downvote for the specified song
     * @param message Message containing the downvote details
     * @param activeSessionModel Host session
     * @param clientModel Client model of the host
     * @return Runnable to be executed on the main thread
     */
    private Runnable removeSkipSongVote(Message message, SessionModel activeSessionModel, final ClientModel clientModel) {
        // find the song in the session
        for (final SongModel songModel : activeSessionModel.getAllSongs()) {
            String songUUID = songModel.getUUID().toString();
            String requestedSongUUID = message.getSongDetails().getUuid();
            if (songUUID.equals(requestedSongUUID)) {
                // find the corresponding DownVoteModel
                for (DownVoteModel downVoteModel : new ArrayList<>(songModel.getDownVotes())) {
                    String downvoteClientUUID = downVoteModel.getClientModel().getUUID().toString();
                    String clientUUID = message.getSenderUuid();
                    if (downvoteClientUUID.equals(clientUUID)) {
                        final DownVoteModel finalDownVoteModel = downVoteModel;
                        return new Runnable() {
                            @Override
                            public void run() {
                                if (clientModel.getUUID().equals(finalDownVoteModel.getClientModel().getUUID())) {
                                    songModel.setSkipVoteCasted(false);
                                }
                                songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() - 1);
                                songModel.getDownVotes().remove(finalDownVoteModel);
                            }
                        };
                    }
                }
                break;
            }
        }

        return null;
    }

    /**
     * Process a session update. Should only occur for clients.
     * @param message Message containing the session, and all its associated information
     * @param activeSessionModel Client Session Model
     * @param myClientModel Current Client Model
     * @return Runnable to be executed on the main thread
     */
    private Runnable sessionUpdate(Message message, final SessionModel activeSessionModel, final ClientModel myClientModel) {
        Session receivedSession = message.getSession();

        SessionConverter converter = new SessionConverter();
        final SessionModel receivedSessionModel = converter.convert(receivedSession);

        return new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Performing session update...");
                activeSessionModel.setName(receivedSessionModel.getName());
                activeSessionModel.rectifyUUID(receivedSessionModel.getUUID());
                activeSessionModel.setHostUUID(receivedSessionModel.getHostUUID());
                activeSessionModel.setHostName(receivedSessionModel.getHostName());
                activeSessionModel.setCreationDateTime(receivedSessionModel.getCreationDateTime());
                activeSessionModel.setSessionStatus(receivedSessionModel.getSessionStatus());


                for (ClientModel updateClient : receivedSessionModel.getClients()) {
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

                for (SongModel updateSong : receivedSessionModel.getAllSongs()) {
                    boolean found = false;
                    for (SongModel activeSong : activeSessionModel.getAllSongs()) {
                        if (updateSong.getUUID().equals(activeSong.getUUID())) {
                            updateSong(updateSong, activeSong, activeSessionModel);
                            found = true;
                            boolean downvoteCasted = false;

                            List<DownVoteModel> activeDownVotes = new ArrayList<>(activeSong.getDownVotes());
                            for (DownVoteModel updateDownVote : updateSong.getDownVotes()) {
                                boolean downVoteFound = false;
                                for (DownVoteModel downVoteModel : activeDownVotes) {
                                    if (downVoteModel.getUuid().equals(updateDownVote.getUuid())) {
                                        //found; remove it from the list
                                        if (downVoteModel.getClientModel().getUUID().equals(myClientModel.getUUID())) {
                                            downvoteCasted = true;
                                        }
                                        activeDownVotes.remove(downVoteModel);
                                        downVoteFound = true;
                                        break;
                                    }
                                }
                                if (!downVoteFound) {
                                    DownVoteModel model = new DownVoteModel(updateDownVote.getUuid(), updateDownVote.getClientModel(), updateDownVote.getDownVoteFor());
                                    if (model.getClientModel().getUUID().equals(myClientModel.getUUID())) {
                                        downvoteCasted = true;
                                    }
                                    activeSong.getDownVotes().add(model);
                                }
                            }
                            activeSong.setSkipVoteCasted(downvoteCasted);

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

                setActiveDevices(activeSessionModel);
            }
        };
    }

    private void updateClient(ClientModel source, ClientModel target) {
        target.setName(source.getName());
        target.setClientConnectionDetails(source.getClientConnectionDetails());
        target.setIsActive(source.getIsActive());
    }

    private void updateSong(SongModel source, SongModel target, SessionModel sessionModel) {
        target.setCreationDateTime(source.getCreationDateTime());
        target.setTitle(source.getTitle());
        target.setDownloadPath(source.getDownloadPath());
        target.setDownloadUrl(source.getDownloadUrl());
        if (source.getOrder() != target.getOrder()) {
            //remove from all list so they are forcibly readded
            if (sessionModel.getAllSongs().contains(target)) {
                sessionModel.getAllSongs().remove(target);
                sessionModel.getAllSongs().add(target);
            }
            checkAndRemove(sessionModel.getPlayQueue(), target);
            checkAndRemove(sessionModel.getPastSongs(), target);
            checkAndRemove(sessionModel.getPlayQueueWithDislikedSongs(), target);
            target.setOrder(source.getOrder());
        }
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

    // Updates the Session model to correctly reflect the number of active clients
    private void setActiveDevices(SessionModel sessionModel) {
        int validClientCount = 0;
        for (ClientModel client : sessionModel.getClients()) {
            if (client.getIsActive()) {
                validClientCount += 1;
            }
        }

        sessionModel.setActiveDevices(validClientCount);
    }

    /**
     * Small helper method.
     * Checks if a songModel is contained in the provided list, and if so, removes it.
     * @param sortedList List to check
     * @param songModel SongModel to remove
     */
    private void checkAndRemove(ObservableUniqueSortedList<SongModel> sortedList, SongModel songModel){
        if (sortedList.contains(songModel)) {
            sortedList.remove(songModel);
        }
    }
}
