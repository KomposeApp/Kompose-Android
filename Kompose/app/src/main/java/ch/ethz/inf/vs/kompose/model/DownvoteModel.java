package ch.ethz.inf.vs.kompose.model;

import org.joda.time.DateTime;

import java.util.UUID;

public class DownvoteModel {

    private DateTime castTime;
    private UUID uuid;

    public DateTime getCastTime() {
        return castTime;
    }

    public void setCastTime(DateTime castTime) {
        this.castTime = castTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
