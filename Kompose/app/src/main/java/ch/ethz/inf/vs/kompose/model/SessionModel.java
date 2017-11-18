package ch.ethz.inf.vs.kompose.model;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.net.InetAddress;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.Client;

public class SessionModel {

    public SessionModel(UUID uuid,
                        InetAddress hostIP,
                        int hostPort) {
        this.uuid = uuid;
        this.hostIP = hostIP;
        this.hostPort = hostPort;
    }

    private UUID uuid;
    private String sessionName;

    private UUID hostUUID;
    private InetAddress hostIP;
    private int hostPort;

    private final ObservableList<ClientModel> clients = new ObservableArrayList<>();
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

    public InetAddress getHostIP() {
        return hostIP;
    }

    public void setHostIP(InetAddress hostIP) {
        this.hostIP = hostIP;
    }

    public int getHostPort() {

        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public ObservableList<ClientModel> getClients() {
        return clients;
    }

    public PlayListModel getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlayListModel playlist) {
        this.playlist = playlist;
    }
}
