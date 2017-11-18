package ch.ethz.inf.vs.kompose.model;

import java.util.UUID;

public class ClientModel {

    public ClientModel(UUID uuid) {
        this.uuid = uuid;
    }

    private UUID uuid;
    private String name;
    private boolean isActive;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
