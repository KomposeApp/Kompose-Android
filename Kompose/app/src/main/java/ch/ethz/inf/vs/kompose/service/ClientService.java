package ch.ethz.inf.vs.kompose.service;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class ClientService extends BaseService {
    /**
     * get the client model associated to this device from the specific session
     *
     * @param sessionModel the session where the client is active
     */
    public ClientModel getOwnClientModel(SessionModel sessionModel) {
        // TODO
        return new ClientModel(UUID.randomUUID(), sessionModel);
    }

    /**
     * save all changes in the client model and send value to other nodes
     *
     * @param clientModel the client you want to persist
     */
    public void saveClient(ClientModel clientModel) {
        //todo
    }

    /**
     * get all client models for the specified session
     *
     * @return collection of clients
     */
    public ObservableList<ClientModel> getClientModels(SessionModel sessionModel) {
        return new ObservableArrayList<>();
    }
}
