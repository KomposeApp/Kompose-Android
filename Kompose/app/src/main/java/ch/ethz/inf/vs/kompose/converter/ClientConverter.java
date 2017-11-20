package ch.ethz.inf.vs.kompose.converter;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;

public class ClientConverter implements IBaseConverter<ClientModel, Client> {

    private SessionModel sessionModel;

    public ClientConverter(SessionModel sessionModel) {
        this.sessionModel = sessionModel;
    }

    public ClientModel convert(Client client) {
        ClientModel clientModel = new ClientModel(UUID.fromString(client.getUuid()), sessionModel);
        clientModel.setName(client.getName());
        clientModel.setIsActive(client.getIsActive());
        return clientModel;
    }

    public Client convert(ClientModel clientModel) {
        Client client = new Client();
        client.setUuid(clientModel.getUuid().toString());
        client.setIsActive(clientModel.getIsActive());
        client.setName(clientModel.getName());
        return client;
    }
}
