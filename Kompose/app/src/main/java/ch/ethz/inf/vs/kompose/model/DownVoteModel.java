package ch.ethz.inf.vs.kompose.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.joda.time.DateTime;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.BR;

public class DownVoteModel extends BaseObservable {
    public DownVoteModel(UUID uuid, ClientModel clientModel, SongModel downVoteFor) {
        this.uuid = uuid;
        this.clientModel = clientModel;
        this.downVoteFor = downVoteFor;
    }

    private DateTime castDateTime;
    private UUID uuid;
    private ClientModel clientModel;
    private SongModel downVoteFor;

    public UUID getUuid() {
        return uuid;
    }

    @Bindable
    public DateTime getCastDateTime() {
        return castDateTime;
    }

    public void setCastDateTime(DateTime castTime) {
        this.castDateTime = castTime;
        notifyPropertyChanged(BR.castDateTime);
    }

    @Bindable
    public ClientModel getClientModel() {
        return clientModel;
    }

    public SongModel getDownVoteFor() {
        return downVoteFor;
    }
}
