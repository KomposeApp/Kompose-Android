package ch.ethz.inf.vs.kompose.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.UUID;

public class DownVoteModel extends BaseObservable {
    public DownVoteModel(UUID uuid, ClientModel clientModel, SongModel downVoteFor) {
        this.uuid = uuid;
        this.clientModel = clientModel;
        this.downVoteFor = downVoteFor;
    }

    private UUID uuid;
    private ClientModel clientModel;
    private SongModel downVoteFor;

    public UUID getUUID() {
        return uuid;
    }

    @Bindable
    public ClientModel getClientModel() {
        return clientModel;
    }

    public SongModel getDownVoteFor() {
        return downVoteFor;
    }
}
