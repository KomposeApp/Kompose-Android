package ch.ethz.inf.vs.kompose.converter;

import android.databinding.ObservableList;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;

/**
 * Convert Session data representation to model representation, and vice-versa.
 **/

public class SessionConverter implements IBaseConverter<SessionModel, Session> {

    /**
     * Data --> Model
     **/
    public SessionModel convert(Session session) {

        SessionModel sessionModel = new SessionModel(
                UUID.fromString(session.getUuid()),
                UUID.fromString(session.getHostUuid())
        );
        sessionModel.setName(session.getSessionName());
        sessionModel.setSessionStatus(SessionStatus.valueOf(session.getSessionStatus()));

        // format DateTime as ISO 8601
        DateTimeFormatter isoParser = ISODateTimeFormat.dateTime();
        sessionModel.setCreationDateTime(isoParser.parseDateTime(session.getCreationDateTime()));

        //convert clients (also converts all contained clients to models)
        if (session.getClients() != null) {
            ClientConverter clientConverter = new ClientConverter(sessionModel);
            for (Client client : session.getClients()) {
                ClientModel clientModel = clientConverter.convert(client);
                if (client.getUuid().equals(session.getHostUuid())) {
                    sessionModel.setHostName(clientModel.getName());
                }
                sessionModel.getClients().add(clientModel);
            }
        }

        //convert songs
        if (session.getSongs() != null) {
            SongConverter songConverter = new SongConverter(sessionModel.getClients());
            for (Song song : session.getSongs()) {
                sessionModel.getAllSongs().add(songConverter.convert(song));
            }
        }

        return sessionModel;
    }

    /**
     * Model --> Data
     **/
    public Session convert(SessionModel sessionModel) {
        Session session = new Session();

        //Convert client models to data
        ClientConverter clientConverter = new ClientConverter(sessionModel);
        ObservableList<ClientModel> clientModels = sessionModel.getClients();
        session.setClients(new Client[clientModels.size()]);
        for (int i = 0; i < clientModels.size(); i++) {
            session.getClients()[i] = clientConverter.convert(clientModels.get(i));
        }

        //convert song models to data
        SongConverter songConverter = new SongConverter(clientModels);
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < sessionModel.getAllSongs().size(); i++) {
            //TODO: I don't get it -- why are we skipping these in particular? Please discuss before removing this TODO.
            //exclude resolving songs as they only should exist temporarily
            if (sessionModel.getAllSongs().get(i).getSongStatus() != SongStatus.RESOLVING) {
                songs.add(songConverter.convert(sessionModel.getAllSongs().get(i)));
            }
        }
        if (songs.size() > 0) {
            session.setSongs((Song[]) songs.toArray());
        } else {
            session.setSongs(new Song[0]);
        }
        //set remaining contents
        session.setHostUuid(sessionModel.getHostUUID().toString());
        session.setSessionName(sessionModel.getName());
        session.setUUID(sessionModel.getUUID().toString());
        session.setSessionStatus(sessionModel.getSessionStatus().toString());

        if (sessionModel.getCreationDateTime() != null) {
            session.setCreationDateTime(sessionModel.getCreationDateTime().toString());
        }

        return session;
    }
}
