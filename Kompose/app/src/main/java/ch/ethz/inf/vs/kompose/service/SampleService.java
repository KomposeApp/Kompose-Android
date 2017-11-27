package ch.ethz.inf.vs.kompose.service;

import android.databinding.ObservableList;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.MainActivity;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;


public class SampleService {
    ObservableList<ClientModel> clientList = new ObservableUniqueSortedList<>(new Comparator<ClientModel>() {
        @Override
        public int compare(ClientModel o1, ClientModel o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });

    public SessionModel getSampleSession(String sessionName) {
        SessionModel sessionModel = new SessionModel(UUID.randomUUID(), UUID.randomUUID());
        sessionModel.setCreationDateTime(DateTime.now());
        sessionModel.setName(sessionName);

        for (int i = 0; i < 4; i++) {
            ClientModel clientModel = new ClientModel(UUID.randomUUID(), sessionModel);

            for (int j = 0; j < 5; j++) {
                sessionModel.getSongs().add(getSampleSong(sessionModel, clientModel, i * 10 + j));
            }
        }


        return sessionModel;
    }

    public ClientModel getSampleClient(SessionModel sessionModel, String clientName) {
        ClientModel clientModel = new ClientModel(UUID.randomUUID(), sessionModel);
        clientModel.setIsActive(true);
        clientModel.setName(clientName);

        return clientModel;
    }

    public SongModel getSampleSong(SessionModel sessionModel, ClientModel model, Integer integer) {
        SongModel songModel = new SongModel(UUID.randomUUID(), model, sessionModel);
        songModel.setOrder(integer);
        songModel.setStatus(SongStatus.REQUESTED);
        songModel.setValidDownVoteCount(0);
        songModel.setSecondsLength(100);
        songModel.setSkipVoteCasted(integer % 2 == 0);
        songModel.setDownloaded(integer % 4 == 0);
        songModel.setTitle("song " + integer);
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
