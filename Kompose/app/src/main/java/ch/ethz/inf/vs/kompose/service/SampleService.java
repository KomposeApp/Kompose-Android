package ch.ethz.inf.vs.kompose.service;

import android.databinding.ObservableList;

import org.joda.time.DateTime;

import java.net.URI;
import java.util.Iterator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.model.comparators.ClientComparator;
import ch.ethz.inf.vs.kompose.model.comparators.UniqueModelComparator;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;


public class SampleService {
    private final ObservableList<ClientModel> clientList = new ObservableUniqueSortedList<>(new ClientComparator(), new UniqueModelComparator<ClientModel>());

    public SessionModel getSampleSession(String sessionName) {
        SessionModel sessionModel = new SessionModel(UUID.randomUUID(), UUID.randomUUID(), true);
        sessionModel.setCreationDateTime(DateTime.now());
        sessionModel.setName(sessionName);
        sessionModel.setSessionStatus(SessionStatus.ACTIVE);

        fillSampleSession(sessionModel);

        return sessionModel;
    }

    public void fillSampleSession(SessionModel sessionModel) {
        //sessionModel.setSessionStatus(SessionStatus.PLAYING);
        int order = 0;
        for (int i = 0; i < 1; i++) {
            ClientModel clientModel = new ClientModel(UUID.randomUUID(), sessionModel);


            for (int j = 0; j < 3; j++) {
                sessionModel.getPastSongs().add(getSampleSong(sessionModel, clientModel, order++));
            }

            SongModel songModel = getSampleSong(sessionModel, clientModel, order++);
            songModel.setSongStatus(SongStatus.PLAYING);
            sessionModel.setCurrentlyPlaying(songModel);

            /*
            for (int j = 0; j < 5; j++) {
                sessionModel.getPlayQueue().add(getSampleSong(sessionModel, clientModel, order++));
            }
            */
        }

        for (SongModel songModel : sessionModel.getPastSongs()) {
            sessionModel.getAllSongs().add(songModel);
        }
        sessionModel.getAllSongs().add(sessionModel.getCurrentlyPlaying());


        Iterator<SongModel> iterator = sessionModel.getPlayQueue().iterator();
        if (iterator.hasNext()) {
            do {
                SongModel songModel = iterator.next();
                sessionModel.getPlayQueue().add(songModel);
            } while (iterator.hasNext());
        }
    }

    private ClientModel getSampleClient(SessionModel sessionModel, String clientName) {
        ClientModel clientModel = new ClientModel(UUID.randomUUID(), sessionModel);
        clientModel.setIsActive(true);
        clientModel.setName(clientName);

        return clientModel;
    }

    private SongModel getSampleSong(SessionModel sessionModel, ClientModel model, Integer integer) {
        SongModel songModel = new SongModel(UUID.randomUUID(), model, sessionModel);
        songModel.setOrder(integer);
        songModel.setSongStatus(SongStatus.REQUESTED);
        songModel.setValidDownVoteCount(0);
        songModel.setSecondsLength(100);
        songModel.setSkipVoteCasted(integer % 2 == 0);
        songModel.setDownloadStatus(integer % 4 == 0 ? DownloadStatus.FINISHED : DownloadStatus.STARTED);
        songModel.setTitle("song " + integer);
        songModel.setThumbnailUrl(URI.create("https://i.ytimg.com/vi/CGOt8dZRsHk/hqdefault.jpg"));
        songModel.setSourceUrl(URI.create("https://www.youtube.com/watch?v=ZS0WvzRVByg"));
        songModel.setDownloadUrl(URI.create("https://i.ytimg.com/vi/CGOt8dZRsHk/hqdefault.jpg"));
        return songModel;
    }

    public ObservableList<ClientModel> getClients() {
        SessionModel sessionModel = getSampleSession("client session");

        clientList.add(getSampleClient(sessionModel, "Sandro"));
        clientList.add(getSampleClient(sessionModel, "Dario"));
        clientList.add(getSampleClient(sessionModel, "Phillipe"));

        return clientList;
    }

    public void addMoreClients() {
        SessionModel sessionModel = clientList.get(0).getPartOfSession();
        clientList.add(getSampleClient(sessionModel, "new added client"));
        clientList.add(getSampleClient(sessionModel, "new added client 2"));
        clientList.add(getSampleClient(sessionModel, "new added client 3"));
    }
}
