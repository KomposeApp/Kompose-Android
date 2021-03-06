package ch.ethz.inf.vs.kompose.model;

import android.databinding.Bindable;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.data.network.ClientConnectionDetails;
import ch.ethz.inf.vs.kompose.model.base.UniqueModel;

public class ClientModel extends UniqueModel {

    public ClientModel(UUID uuid, SessionModel partOfSession) {
        super(uuid);
        this.partOfSession = partOfSession;
    }

    private String name;
    private boolean isActive;
    private SessionModel partOfSession;
    private ClientConnectionDetails clientConnectionDetails;


    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
        notifyPropertyChanged(BR.isActive);
    }

    public SessionModel getPartOfSession() {
        return partOfSession;
    }

    public ClientConnectionDetails getClientConnectionDetails() {
        return clientConnectionDetails;
    }

    public void setClientConnectionDetails(ClientConnectionDetails clientConnectionDetails) {
        this.clientConnectionDetails = clientConnectionDetails;
    }
}
