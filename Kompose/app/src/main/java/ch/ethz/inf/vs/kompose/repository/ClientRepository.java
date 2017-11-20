package ch.ethz.inf.vs.kompose.repository;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.io.IOException;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.SessionConverter;
import ch.ethz.inf.vs.kompose.data.JsonConverter;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.data.Session;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.AndroidServerService;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;
import ch.ethz.inf.vs.kompose.service.StorageService;

public class ClientRepository {
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
