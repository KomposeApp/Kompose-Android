package ch.ethz.inf.vs.kompose.service;

import android.databinding.ObservableList;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;


public class SampleService {
    ObservableList<ClientModel> clientList = new ObservableUniqueSortedList<>(new Comparator<ClientModel>() {
        @Override
        public int compare(ClientModel o1, ClientModel o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });

    private SessionModel getSampleSession(String sessionName) {
        SessionModel sessionModel = new SessionModel(UUID.randomUUID(), UUID.randomUUID());
        sessionModel.setCreationDateTime(DateTime.now());
        sessionModel.setName(sessionName);

        return sessionModel;
    }

    private ClientModel getSampleClient(SessionModel sessionModel, String clientName) {
        ClientModel clientModel = new ClientModel(UUID.randomUUID(), sessionModel);
        clientModel.setIsActive(true);
        clientModel.setName(clientName);

        return clientModel;
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
