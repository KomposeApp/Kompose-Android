package ch.ethz.inf.vs.kompose.service.handler;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.SessionService;
import ch.ethz.inf.vs.kompose.service.SongService;

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
        try {
            Log.d(LOG_TAG, "Thread dispatched");
            if (socket != null) {
                message = readMessage(socket);
            }
            if (message == null) {
                return;
            }

            MessageType messageType = MessageType.valueOf(message.getType());
            Log.d(LOG_TAG, "Message processing (" + messageType + ")");


            Session activeSession = sessionService.getActiveSession();
            SessionModel activeSessionModel = sessionService.getActiveSessionModel();

            switch (messageType) {
                case REGISTER_CLIENT:
                    registerClient(message, activeSession, activeSessionModel);
                    break;
                case UNREGISTER_CLIENT:
                    unregisterClient(message, activeSession, activeSessionModel);
                    break;
                case SESSION_UPDATE:
                    sessionUpdate(message, activeSession, activeSessionModel);
                    break;
                case REQUEST_SONG:
                    requestSong(message, activeSession, activeSessionModel);
                    break;
                case CAST_SKIP_SONG_VOTE:
                    castSkipSongVote(message, activeSession, activeSessionModel);
                    break;
                case REMOVE_SKIP_SONG_VOTE:
                    removeSkipSongVote(message, activeSession, activeSessionModel);
                    break;
                case KEEP_ALIVE:
                    break;
                case FINISH_SESSION:
                    break;
                case ERROR:
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastNewSession() {

    }

    private void registerClient(Message message, Session session, SessionModel sessionModel) {
        Client client = new Client();
        client.setIsActive(true);
        client.setName(message.getSenderUsername());
        client.setUuid(message.getSenderUuid());

        List<Client> list = Arrays.asList(session.getClients());
        list.add(client);

        session.setClients((Client[]) list.toArray());

        ClientConverter clientConverter = new ClientConverter(sessionModel);
        ClientModel clientModel = clientConverter.convert(client);

        sessionModel.getClients().add(clientModel);
        broadcastNewSession();
    }

    private void unregisterClient(Message message, Session session, SessionModel sessionModel) {
        Client foundClient = null;
        for (Client client :
                session.getClients()) {
            if (client.getUuid().equals(message.getSenderUsername())) {
                foundClient = client;
                break;
            }
        }

        if (foundClient == null) {
            return;
        }


        foundClient.setIsActive(false);
        UUID clientUUID = UUID.fromString(foundClient.getUuid());
        for (ClientModel client :
                sessionModel.getClients()) {
            if (client.getUuid().equals(clientUUID)) {
                client.setIsActive(false);

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
                break;
            }
        }

        broadcastNewSession();
    }

    private void requestSong(Message message, Session session, SessionModel sessionModel) {
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
        broadcastNewSession();
    }

    private void castSkipSongVote(Message message, Session activeSession, SessionModel activeSessionModel) {
        Song downVoteTarget = null;
        for (Song song : activeSession.getSongs()) {
            if (message.getSongDetails().getUuid().equals(song.getUuid())) {
                for (DownVote downVote : song.getDownVotes()) {
                    if (downVote.getClientUuid().equals(message.getSenderUuid())) {
                        //already cast the vote, liar liar chickeneier
                        return;
                    }
                }
                downVoteTarget = song;
                break;
            }
        }

        if (downVoteTarget == null) {
            return;
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
                        return;
                    }
                }
                DownVoteConverter downVoteConverter = new DownVoteConverter(activeSessionModel.getClients(), songModel);
                DownVoteModel model = downVoteConverter.convert(downVote);
                songModel.getDownVotes().add(model);
                songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() + 1);

                checkDownVoteCount(activeSessionModel, songModel);
            }
        }

        broadcastNewSession();
    }


    private void removeSkipSongVote(Message message, Session activeSession, SessionModel activeSessionModel) {
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
            return;
        }

        UUID songUUID = UUID.fromString(downVoteTarget.getUuid());
        UUID downVoteUUID = UUID.fromString(downVote.getUuid());

        for (SongModel songModel : activeSessionModel.getSongs()) {
            if (songModel.getUuid().equals(songUUID)) {
                for (int i = 0; i < songModel.getDownVotes().size(); i++) {
                    if (songModel.getDownVotes().get(i).getUuid().equals(downVoteUUID)) {
                        songModel.getDownVotes().remove(i);
                        checkDownVoteCount(activeSessionModel, songModel);
                        return;
                    }
                }
                //ehm wat? this should have failed in the Session object.
                return;
            }
        }

        broadcastNewSession();

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

    private void sessionUpdate(Message message, Session activeSession, SessionModel activeSessionModel) {
        Session receivedSession = message.getSession();

        activeSession.setSessionName(receivedSession.getSessionName());
        activeSession.setCreationDateTime(receivedSession.getCreationDateTime());
        activeSession.setHostUuid(receivedSession.getHostUuid());


        //todo BROT: perform IN MEMORY update of current session (no replacing references!)
    }
}
