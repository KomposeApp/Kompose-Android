package ch.ethz.inf.vs.kompose.model;

import org.joda.time.DateTime;

import java.util.UUID;

public class DownVoteModel {
    public DownVoteModel(UUID uuid) {
        this.uuid = uuid;
    }

    private DateTime castTime;
    private UUID uuid;
    private ClientModel clientModel;

    public UUID getUuid() {
        return uuid;
    }

    public DateTime getCastTime() {
        return castTime;
    }

    public void setCastDateTime(DateTime castTime) {
        this.castTime = castTime;
    }

    public ClientModel getClientModel() {
        return clientModel;
    }

    public void setClientModel(ClientModel clientModel) {
        this.clientModel = clientModel;
    }
}
