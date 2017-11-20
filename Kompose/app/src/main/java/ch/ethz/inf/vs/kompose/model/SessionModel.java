package ch.ethz.inf.vs.kompose.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.net.wifi.p2p.WifiP2pManager;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.data.network.ConnectionDetails;

public class SessionModel extends BaseObservable {

    public SessionModel(UUID uuid, UUID hostUUID) {
        this.uuid = uuid;
        this.hostUUID = hostUUID;
    }

    private UUID uuid;
    private String sessionName;
    private UUID hostUUID;
    private ConnectionDetails connectionDetails;

    private final ObservableList<ClientModel> clients = new ObservableArrayList<>();

    private final ObservableList<SongModel> songs = new ObservableSortedList<>(
            new SongComparator());

    public ObservableList<SongModel> getSongs() {
        return songs;
    }

    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public void setConnectionDetails(ConnectionDetails connectionDetails) {
        this.connectionDetails = connectionDetails;
    }

    private class SongComparator implements Comparator<SongModel> {
        @Override
        public int compare(SongModel s1, SongModel s2) {
            return s1.getOrder() < s2.getOrder() ? -1 : 1;
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getHostUUID() {
        return hostUUID;
    }

    @Bindable
    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
        notifyPropertyChanged(BR.sessionName);
    }

    public ObservableList<ClientModel> getClients() {
        return clients;
    }
}
