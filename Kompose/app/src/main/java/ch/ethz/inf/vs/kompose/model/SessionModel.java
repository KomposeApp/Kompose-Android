package ch.ethz.inf.vs.kompose.model;

import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.model.base.UniqueModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;

public class SessionModel extends UniqueModel {

    public SessionModel(UUID uuid, UUID hostUUID) {
        super(uuid);
        this.hostUUID = hostUUID;
    }

    private String sessionName;
    private UUID hostUUID;
    private DateTime creationDateTime;
    private ServerConnectionDetails connectionDetails;

    private final ObservableList<ClientModel> clients = new ObservableArrayList<>();

    private final ObservableList<SongModel> songs = new ObservableUniqueSortedList<>(
            new SongComparator());

    public ObservableList<SongModel> getSongs() {
        return songs;
    }

    public ServerConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public void setConnectionDetails(ServerConnectionDetails connectionDetails) {
        this.connectionDetails = connectionDetails;
    }

    @Bindable
    public DateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(DateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
        notifyPropertyChanged(BR.creationDateTime);
    }

    private class SongComparator implements Comparator<SongModel> {
        @Override
        public int compare(SongModel s1, SongModel s2) {
            return s1.getOrder() < s2.getOrder() ? -1 : 1;
        }
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
