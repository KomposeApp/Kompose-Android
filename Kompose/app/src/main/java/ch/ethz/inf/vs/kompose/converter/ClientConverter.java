package ch.ethz.inf.vs.kompose.converter;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.data.network.ClientConnectionDetails;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;

/** Convert client data representation to model representation, and vice-versa. **/

public class ClientConverter implements IBaseConverter<ClientModel, Client> {

    // Require a SessionModel to create ClientModel from data.
    private SessionModel sessionModel;

    public ClientConverter(SessionModel sessionModel) {
        this.sessionModel = sessionModel;
    }

    /** Data --> Model **/
    public ClientModel convert(Client client) {
        ClientModel clientModel = new ClientModel(UUID.fromString(client.getUuid()), sessionModel);
        clientModel.setName(client.getName());
        clientModel.setIsActive(client.getIsActive());
        clientModel.setClientConnectionDetails(new ClientConnectionDetails(null,
                client.getPort(), null));
        return clientModel;
    }

    /** Model --> Data **/
    public Client convert(ClientModel clientModel) {
        Client client = new Client();
        client.setUuid(clientModel.getUUID().toString());
        client.setName(clientModel.getName());
        client.setIsActive(clientModel.getIsActive());

        if (clientModel.getClientConnectionDetails() != null) {
            client.setPort(clientModel.getClientConnectionDetails().getPort());
        }

        return client;
    }
}
