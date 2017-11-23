package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.ObservableList;

import ch.ethz.inf.vs.kompose.data.json.Client;
import ch.ethz.inf.vs.kompose.model.ClientModel;

/**
 * Created by git@famoser.ch on 23/11/2017.
 */

public class DesignViewModel {

    private ClientModel client;
    private ObservableList<ClientModel> clients;

    public DesignViewModel(ObservableList<ClientModel> clients) {
        this.client = clients.get(0);
        this.clients = clients;
    }

    public ClientModel getClient() {
        return client;
    }

    public ObservableList<ClientModel> getClients() {
        return clients;
    }
}
