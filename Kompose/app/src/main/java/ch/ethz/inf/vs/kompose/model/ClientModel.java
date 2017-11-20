package ch.ethz.inf.vs.kompose.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.BR;

public class ClientModel extends BaseObservable {

    public ClientModel(UUID uuid, SessionModel partOfSession) {
        this.uuid = uuid;
        this.partOfSession = partOfSession;
    }

    private UUID uuid;
    private String name;
    private boolean isActive;
    private SessionModel partOfSession;

    public UUID getUuid() {
        return uuid;
    }

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
}
