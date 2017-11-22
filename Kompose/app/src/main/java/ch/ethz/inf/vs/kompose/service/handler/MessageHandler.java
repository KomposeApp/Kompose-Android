package ch.ethz.inf.vs.kompose.service.handler;

import android.util.Log;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.ClientConverter;
import ch.ethz.inf.vs.kompose.converter.DownVoteConverter;
import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.data.json.DownVote;
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
import ch.ethz.inf.vs.kompose.service.SessionService;

public class MessageHandler implements Runnable {
    private static final String LOG_TAG = "## MessageHandler";

    private Socket socket;
    private Message message;
    private SessionService sessionService;

    public MessageHandler(SessionService sessionService, Socket socket) {
        this.sessionService = sessionService;
        this.socket = socket;
    }

    public MessageHandler(SessionService sessionService, Message message) {
        this.sessionService = sessionService;
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
        StringBuilder json = new StringBuilder();

        char[] buffer = new char[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            json.append(new String(buffer, 0, bytesRead));
        }
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

        Session activeSession = sessionService.getActiveSession();
        SessionModel activeSessionModel = sessionService.getActiveSessionModel();

        MessageType messageType = MessageType.valueOf(message.getType());
        Log.d(LOG_TAG, "Message processing (" + messageType + ")");

        ClientModel clientModel = getClientModel(UUID.fromString(message.getSenderUuid()), activeSessionModel);
        if (clientModel != null && clientModel.getClientConnectionDetails() != null) {
            clientModel.getClientConnectionDetails().setLastRequestReceived(DateTime.now());
        }

        if (clientModel == null && messageType != MessageType.REGISTER_CLIENT) {
            //client unknown; therefore not allows to do request
            return;
        }

        boolean sessionHasChanged = false;
        switch (messageType) {
            case REGISTER_CLIENT:
                sessionHasChanged = registerClient(message, activeSession, activeSessionModel);
                break;
            case UNREGISTER_CLIENT:
                sessionHasChanged = unregisterClient(message, activeSession, activeSessionModel);
                break;
            case SESSION_UPDATE:
                sessionUpdate(message, activeSession, activeSessionModel);
                break;
            case REQUEST_SONG:
                sessionHasChanged = requestSong(message, activeSession, activeSessionModel);
                break;
            case CAST_SKIP_SONG_VOTE:
                sessionHasChanged = castSkipSongVote(message, activeSession, activeSessionModel);
                break;
            case REMOVE_SKIP_SONG_VOTE:
                sessionHasChanged = removeSkipSongVote(message, activeSession, activeSessionModel);
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
            sessionService.sessionChanged();
        }

    }

    private void finishSession() {
        sessionService.leaveSession();
    }

    private boolean registerClient(Message message, Session session, SessionModel sessionModel) {
        Client client = new Client();
        client.setIsActive(true);
        client.setName(message.getSenderUsername());
        client.setUuid(message.getSenderUuid());

        List<Client> list = Arrays.asList(session.getClients());
        list.add(client);

        session.setClients((Client[]) list.toArray());

        ClientConverter clientConverter = new ClientConverter(sessionModel);
        ClientModel clientModel = clientConverter.convert(client);

        if (socket != null) {
            ClientConnectionDetails connectionDetails = new ClientConnectionDetails(socket, DateTime.now());
            clientModel.setClientConnectionDetails(connectionDetails);
        }

        sessionModel.getClients().add(clientModel);
        return true;
    }

    private boolean unregisterClient(Message message, Session session, SessionModel sessionModel) {
        Client foundClient = null;
        for (Client client :
                session.getClients()) {
            if (client.getUuid().equals(message.getSenderUsername())) {
                foundClient = client;
                break;
            }
        }

        if (foundClient == null) {
            return false;
        }

        foundClient.setIsActive(false);

        UUID clientUUID = UUID.fromString(foundClient.getUuid());
        ClientModel clientModel = getClientModel(clientUUID, sessionModel);
        if (clientModel != null) {
            clientModel.setIsActive(false);

            if (clientModel.getClientConnectionDetails() != null) {
                try {
                    clientModel.getClientConnectionDetails().getSocket().close();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "socket could not be closed");
                }
                clientModel.setClientConnectionDetails(null);
            }

            //remove downvote validity
            for (SongModel songModel : sessionModel.getSongs()) {
                for (DownVoteModel downVoteModel : songModel.getDownVotes()) {
                    if (downVoteModel.getUuid().equals(clientUUID)) {
                        songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() - 1);
                        checkDownVoteCount(sessionModel, songModel);
                        break;
                    }
                }
            }
        }
        return true;
    }

    private boolean requestSong(Message message, Session session, SessionModel sessionModel) {
        Song song = message.getSongDetails();
        song.setProposedByClientUuid(message.getSenderUuid());

        List<Song> list = Arrays.asList(session.getSongs());
        list.add(song);

        session.setSongs((Song[]) list.toArray());

        SongConverter songConverter = new SongConverter(sessionModel.getClients());
        SongModel songModel = songConverter.convert(song);
        songModel.setStatus(SongStatus.IN_QUEUE);
        songModel.setOrder(sessionModel.getSongs().size() + 1);

        sessionModel.getSongs().add(songModel);
        return true;
    }

    private boolean castSkipSongVote(Message message, Session activeSession, SessionModel activeSessionModel) {
        Song downVoteTarget = null;
        for (Song song : activeSession.getSongs()) {
            if (message.getSongDetails().getUuid().equals(song.getUuid())) {
                for (DownVote downVote : song.getDownVotes()) {
                    if (downVote.getClientUuid().equals(message.getSenderUuid())) {
                        //already cast the vote, liar liar chickeneier
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

        DownVote downVote = new DownVote();
        downVote.setClientUuid(message.getSenderUuid());
        downVote.setUuid(UUID.randomUUID().toString());

        List<DownVote> downVotes = Arrays.asList(downVoteTarget.getDownVotes());
        downVotes.add(downVote);
        downVoteTarget.setDownVotes((DownVote[]) downVotes.toArray());

        UUID songUUID = UUID.fromString(downVoteTarget.getUuid());
        UUID clientUUID = UUID.fromString(message.getSenderUuid());
        for (SongModel songModel : activeSessionModel.getSongs()) {
            if (songModel.getUuid().equals(songUUID)) {
                for (DownVoteModel downVoteModel : songModel.getDownVotes()) {
                    if (downVoteModel.getClientModel().getUuid().equals(clientUUID)) {
                        //ehm wat? this should have failed in the Session object.
                        return false;
                    }
                }
                DownVoteConverter downVoteConverter = new DownVoteConverter(activeSessionModel.getClients(), songModel);
                DownVoteModel model = downVoteConverter.convert(downVote);
                songModel.getDownVotes().add(model);
                songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() + 1);

                checkDownVoteCount(activeSessionModel, songModel);
                return true;
            }
        }
        return false;
    }


    private boolean removeSkipSongVote(Message message, Session activeSession, SessionModel activeSessionModel) {
        Song downVoteTarget = null;
        DownVote downVote = null;
        for (Song song : activeSession.getSongs()) {
            if (message.getSongDetails().getUuid().equals(song.getUuid())) {
                List<DownVote> downVotes = Arrays.asList(song.getDownVotes());
                for (int i = 0; i < downVotes.size(); i++) {
                    if (downVotes.get(i).getClientUuid().equals(message.getSenderUuid())) {
                        downVote = downVotes.get(i);
                        downVotes.remove(i);
                        break;
                    }
                }
                if (downVote != null) {
                    song.setDownVotes((DownVote[]) downVotes.toArray());
                    downVoteTarget = song;
                }
                break;
            }
        }

        if (downVoteTarget == null) {
            return false;
        }

        UUID songUUID = UUID.fromString(downVoteTarget.getUuid());
        UUID downVoteUUID = UUID.fromString(downVote.getUuid());

        for (SongModel songModel : activeSessionModel.getSongs()) {
            if (songModel.getUuid().equals(songUUID)) {
                for (int i = 0; i < songModel.getDownVotes().size(); i++) {
                    if (songModel.getDownVotes().get(i).getUuid().equals(downVoteUUID)) {
                        songModel.getDownVotes().remove(i);
                        checkDownVoteCount(activeSessionModel, songModel);
                        return true;
                    }
                }
                //ehm wat? this should have failed in the Session object.
                break;
            }
        }
        return false;
    }

    private void sessionUpdate(Message message, Session activeSession, SessionModel activeSessionModel) {
        Session receivedSession = message.getSession();

        activeSession.setSessionName(receivedSession.getSessionName());
        activeSession.setCreationDateTime(receivedSession.getCreationDateTime());
        activeSession.setHostUuid(receivedSession.getHostUuid());


        //this method receives the SESSION_UPDATE command from the server
        //the SESSION_UPDATE command contains the server approved session (in the message object)
        //use this server approved message now to update the activeSession object and the activeSessionModel
        //the hard part: do not replace ANY references. if the object already exists (identified with the UUID) you can not create a new one, but rather have to replace the content of the fields.
        //see the other implemenentations for reference
    }


    private ClientModel getClientModel(UUID clientUUID, SessionModel sessionModel) {
        for (ClientModel client :
                sessionModel.getClients()) {
            if (client.getUuid().equals(clientUUID)) {
                return client;
            }
        }
        return null;
    }

    private void checkDownVoteCount(SessionModel sessionModel, SongModel songModel) {
        if (songModel.getStatus() != SongStatus.IN_QUEUE && songModel.getStatus() != SongStatus.EXCLUDED_BY_POPULAR_VOTE) {
            return;
        }

        Integer validClientCount = 0;
        for (ClientModel client : sessionModel.getClients()) {
            if (client.getIsActive()) {
                validClientCount += 1;
            }
        }

        Integer quorum = validClientCount / 2;
        if (songModel.getValidDownVoteCount() >= quorum) {
            songModel.setStatus(SongStatus.EXCLUDED_BY_POPULAR_VOTE);
        } else {
            songModel.setStatus(SongStatus.IN_QUEUE);
        }
    }
}
