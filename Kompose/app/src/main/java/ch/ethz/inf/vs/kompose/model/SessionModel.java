package ch.ethz.inf.vs.kompose.model;

import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.data.network.ServerConnectionDetails;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.model.base.UniqueModel;
import ch.ethz.inf.vs.kompose.model.comparators.SongComparator;
import ch.ethz.inf.vs.kompose.model.comparators.UniqueModelComparator;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;

public class SessionModel extends UniqueModel {

    public SessionModel(UUID uuid, UUID hostUUID) {
        super(uuid);
        this.hostUUID = hostUUID;
    }

    private String name;
    private SessionStatus sessionStatus;
    private UUID hostUUID;
    private String hostName;
    private DateTime creationDateTime;
    private ServerConnectionDetails connectionDetails;
    private SongModel currentlyPlaying;
    private int activeDevices;

    private final ObservableList<ClientModel> clients = new ObservableArrayList<>();

    private final ObservableList<SongModel> playQueue = new ObservableUniqueSortedList<>(
            new SongComparator(), new UniqueModelComparator<SongModel>()
    );

    private final ObservableList<SongModel> allSongList = new ObservableUniqueSortedList<>(
            new SongComparator(), new UniqueModelComparator<SongModel>()
    );

    private final ObservableList<SongModel> playedSongs = new ObservableUniqueSortedList<>(
            new SongComparator(), new UniqueModelComparator<SongModel>()
    );

    private final ObservableList<SongModel> skippedSongs = new ObservableUniqueSortedList<>(
            new SongComparator(), new UniqueModelComparator<SongModel>()
    );

    /**
     * @return all songs waiting to be played
     */
    public ObservableList<SongModel> getPlayQueue() {
        return playQueue;
    }

    /**
     * @return all songs already played
     */
    public ObservableList<SongModel> getPlayedSongs() {
        return playedSongs;
    }

    /**
     * @return all songs from this session, weather played, not yet played or skipped
     */
    public ObservableList<SongModel> getAllSongList() {
        return allSongList;
    }

    /**
     * @return the songs which are / were skipped
     */
    public ObservableList<SongModel> getSkippedSongs() {
        return skippedSongs;
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

    @Bindable
    public SongModel getCurrentlyPlaying() {
        return currentlyPlaying;
    }

    public void setCurrentlyPlaying(SongModel currentlyPlaying) {
        if (currentlyPlaying != this.currentlyPlaying) {
            this.currentlyPlaying = currentlyPlaying;
            notifyPropertyChanged(BR.currentlyPlaying);
        }
    }

    @Bindable
    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
        notifyPropertyChanged(BR.sessionStatus);
    }

    public UUID getHostUUID() {
        return hostUUID;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
        notifyPropertyChanged(BR.hostName);
    }

    public ObservableList<ClientModel> getClients() {
        return clients;
    }

    @Bindable
    public int getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(int activeDevices) {
        this.activeDevices = activeDevices;
        notifyPropertyChanged(BR.activeDevices);
    }
}
