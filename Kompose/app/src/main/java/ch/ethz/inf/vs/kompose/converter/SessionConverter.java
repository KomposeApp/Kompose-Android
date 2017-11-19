package ch.ethz.inf.vs.kompose.converter;

import android.databinding.ObservableList;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.Client;
import ch.ethz.inf.vs.kompose.data.Session;
import ch.ethz.inf.vs.kompose.data.Song;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.PlayListModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

public class SessionConverter {

    public static SessionModel convert(Session session) {
        SessionModel sessionModel = new SessionModel(
                UUID.fromString(session.getUuid()),
                null, 0);

        sessionModel.setUuid(UUID.fromString(session.getUuid()));
        sessionModel.setSessionName(session.getSessionName());
        sessionModel.setHostUUID(UUID.fromString(session.getHostUuid()));

        ObservableList<ClientModel> clients = sessionModel.getClients();
        Client[] clientArray = session.getClients();
        for (int i = 0; i < clientArray.length; i++) {
            clients.add(ClientConverter.convert(clientArray[i]));
        }

        PlayListModel playListModel = new PlayListModel();
        Song[] songs = session.getPlaylist();
        ObservableList<SongModel> songModels = playListModel.getPlaylistItems();
        for (int i = 0; i < songs.length; i++) {
            songModels.add(SongConverter.convert(songs[i], (ClientModel[]) clients.toArray()));
        }
        sessionModel.setPlaylist(playListModel);

        return  sessionModel;
    }

    public static Session convert(SessionModel sessionModel) {
        Session session = new Session();

        ObservableList<ClientModel> clientModels = sessionModel.getClients();
        Client[] clients = new Client[clientModels.size()];
        for (int i = 0; i < clientModels.size(); i++) {
            clients[i] = ClientConverter.convert(clientModels.get(i));
        }
        session.setClients(clients);

        ObservableList<SongModel> songModels = sessionModel.getPlaylist().getPlaylistItems();
        Song[] songs = new Song[songModels.size()];
        for (int i = 0; i < songModels.size(); i++) {
            songs[i] = SongConverter.convert(songModels.get(i));
        }
        session.setPlaylist(songs);

        session.setHostUuid(sessionModel.getHostUUID().toString());
        session.setSessionName(sessionModel.getSessionName());
        session.setUuid(sessionModel.getUuid().toString());

        return session;
    }
}
