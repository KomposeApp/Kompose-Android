package ch.ethz.inf.vs.kompose.converter;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.Client;
import ch.ethz.inf.vs.kompose.model.ClientModel;

public class ClientConverter {

    public static ClientModel convert(Client client) {
        ClientModel clientModel = new ClientModel(UUID.fromString(client.getUuid()));
        clientModel.setName(client.getName());
        clientModel.setIsActive(client.getIsActive());
        return clientModel;
    }

    public static Client convert(ClientModel clientModel) {
        Client client = new Client();
        client.setUuid(clientModel.getUuid().toString());
        client.setIsActive(clientModel.isActive());
        client.setName(clientModel.getName());
        return client;
    }
}
