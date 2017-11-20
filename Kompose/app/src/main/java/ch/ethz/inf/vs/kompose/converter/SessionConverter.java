package ch.ethz.inf.vs.kompose.converter;

import android.databinding.ObservableList;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.Client;
import ch.ethz.inf.vs.kompose.data.Session;
import ch.ethz.inf.vs.kompose.data.Song;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

public class SessionConverter implements IBaseConverter<SessionModel, Session> {

    public SessionModel convert(Session session) {

        SessionModel sessionModel = new SessionModel(
                UUID.fromString(session.getUuid()),
                UUID.fromString(session.getHostUuid()),
                null, 0);
        sessionModel.setSessionName(session.getSessionName());

        //convert clients
        if (session.getClients() != null) {
            ClientConverter clientConverter = new ClientConverter(sessionModel);
            for (Client client : session.getClients()) {
                sessionModel.getClients().add(clientConverter.convert(client));
            }
        }

        //convert songs
        if (session.getSongs() != null) {
            SongConverter songConverter = new SongConverter(sessionModel.getClients());
            for (Song song : session.getSongs()) {
                sessionModel.getSongs().add(songConverter.convert(song));
            }
        }

        return sessionModel;
    }

    public Session convert(SessionModel sessionModel) {
        Session session = new Session();

        ClientConverter clientConverter = new ClientConverter(sessionModel);
        ObservableList<ClientModel> clientModels = sessionModel.getClients();
        session.setClients(new Client[clientModels.size()]);
        for (int i = 0; i < clientModels.size(); i++) {
            session.getClients()[i] = clientConverter.convert(clientModels.get(i));
        }

        SongConverter songConverter = new SongConverter(clientModels);
        ObservableList<SongModel> songModels = sessionModel.getSongs();
        session.setSongs(new Song[songModels.size()]);
        for (int i = 0; i < songModels.size(); i++) {
            session.getSongs()[i] = songConverter.convert(songModels.get(i));
        }

        session.setHostUuid(sessionModel.getHostUUID().toString());
        session.setSessionName(sessionModel.getSessionName());
        session.setUuid(sessionModel.getUuid().toString());

        return session;
    }
}
