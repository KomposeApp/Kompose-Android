package ch.ethz.inf.vs.kompose.model.base;

import android.databinding.BaseObservable;

import java.util.UUID;


public abstract class UniqueModel extends BaseObservable {

    public UniqueModel(UUID uuid) {
        this.uuid = uuid;
    }

    private UUID uuid;

    public UUID getUUID() {
        return uuid;
    }

    public void rectifyUUID(UUID newUUID){
        if(uuid == null) uuid = newUUID;
    }
}
