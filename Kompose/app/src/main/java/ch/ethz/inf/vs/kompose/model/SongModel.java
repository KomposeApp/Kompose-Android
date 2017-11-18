package ch.ethz.inf.vs.kompose.model;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.net.URI;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.enums.SongStatus;

public class SongModel {
    public SongModel(UUID uuid) {
        this.uuid = uuid;
    }

    private UUID uuid;

    private String title;
    private int secondsLength;
    private int order;

    private int validDownVoteCount;
    private final ObservableList<DownVoteModel> downVotes = new ObservableArrayList<>();
    private ClientModel proposedBy;

    private URI downloadUrl;
    private URI thumbnailUrl;
    private URI sourceUrl;

    private SongStatus status;

    public UUID getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getValidDownVoteCount() {
        return validDownVoteCount;
    }

    public void setValidDownVoteCount(int validDownVoteCount) {
        this.validDownVoteCount = validDownVoteCount;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ObservableList<DownVoteModel> getDownVotes() {
        return downVotes;
    }

    public ClientModel getProposedBy() {
        return proposedBy;
    }

    public void setProposedBy(ClientModel proposedBy) {
        this.proposedBy = proposedBy;
    }

    public URI getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(URI downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public URI getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(URI thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public URI getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(URI sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public SongStatus getStatus() {
        return status;
    }

    public void setStatus(SongStatus status) {
        this.status = status;
    }

    public int getSecondsLength() {
        return secondsLength;
    }

    public void setSecondsLength(int secondsLength) {
        this.secondsLength = secondsLength;
    }

}
