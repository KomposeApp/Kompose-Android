package ch.ethz.inf.vs.kompose.model;


import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.Client;

public class SessionModel {

    public SessionModel(UUID uuid) {
        this.uuid = uuid;
    }

    private UUID uuid;
    private UUID hostUUID;
    private String sessionName;

    private final ObservableList<Client> clients = new ObservableArrayList<>();
    private PlayListModel playlist;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getHostUUID() {
        return hostUUID;
    }

    public void setHostUUID(UUID hostUUID) {
        this.hostUUID = hostUUID;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public ObservableList<Client> getClients() {
        return clients;
    }

    public PlayListModel getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlayListModel playlist) {
        this.playlist = playlist;
    }
}
