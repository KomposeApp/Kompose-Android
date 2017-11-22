package ch.ethz.inf.vs.kompose.service;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

/**
 * Created by git@famoser.ch on 21/11/2017.
 */

public class SampleService extends BaseService {
    ObservableArrayList<ClientModel> clientModelObservableList = new ObservableUniqueSortedList<>(new Comparator<ClientModel>() {
        @Override
        public int compare(ClientModel o1, ClientModel o2) {
            return 0;
        }
    });

    private SessionModel getSampleSession(String sessionName) {
        SessionModel sessionModel = new SessionModel(UUID.randomUUID(), UUID.randomUUID());
        sessionModel.setCreationDateTime(DateTime.now());
        sessionModel.setSessionName(sessionName);

        return sessionModel;
    }

    private ClientModel getSampleClient(SessionModel sessionModel, String clientName) {
        ClientModel clientModel = new ClientModel(UUID.randomUUID(), sessionModel);
        clientModel.setIsActive(true);
        clientModel.setName(clientName);

        return clientModel;
    }

    public ObservableArrayList<ClientModel> getClients() {
        SessionModel sessionModel = getSampleSession("client session");

        clientModelObservableList.add(getSampleClient(sessionModel, "Sandro"));
        clientModelObservableList.add(getSampleClient(sessionModel, "Dario"));
        clientModelObservableList.add(getSampleClient(sessionModel, "Phillipe"));

        return clientModelObservableList;
    }
}
