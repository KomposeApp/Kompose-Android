package ch.ethz.inf.vs.kompose.model;

import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import org.joda.time.DateTime;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.base.UniqueModel;

public class SongModel extends UniqueModel {
    public SongModel(UUID uuid, ClientModel proposedBy, SessionModel partOfSession) {
        super(uuid);
        this.proposedBy = proposedBy;
        this.partOfSession = partOfSession;
    }

    private String title;
    private int secondsLength;
    private int order;

    private int validDownVoteCount;
    private final ObservableList<DownVoteModel> downVotes = new ObservableArrayList<>();
    private ClientModel proposedBy;
    private SessionModel partOfSession;

    private URI downloadUrl;
    private URI thumbnailUrl;
    private URI sourceUrl;

    private boolean skipVoteCasted;
    private DateTime creationDateTime;

    private SongStatus status;

    private boolean isDownloaded;
    private boolean downloadStarted;
    private File downloadPath;

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
        notifyPropertyChanged(BR.order);
    }

    @Bindable
    public boolean getSkipVoteCasted() {
        return skipVoteCasted;
    }

    public void setSkipVoteCasted(boolean skipVoteCasted) {
        this.skipVoteCasted = skipVoteCasted;
        notifyPropertyChanged(BR.skipVoteCasted);
    }

    @Bindable
    public int getValidDownVoteCount() {
        return validDownVoteCount;
    }

    public void setValidDownVoteCount(int validDownVoteCount) {
        this.validDownVoteCount = validDownVoteCount;
        notifyPropertyChanged(BR.validDownVoteCount);
    }

    public ObservableList<DownVoteModel> getDownVotes() {
        return downVotes;
    }

    public ClientModel getProposedBy() {
        return proposedBy;
    }

    @Bindable
    public URI getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(URI downloadUrl) {
        this.downloadUrl = downloadUrl;
        notifyPropertyChanged(BR.downloadUrl);
    }

    @Bindable
    public URI getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(URI thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        notifyPropertyChanged(BR.thumbnailUrl);
    }

    @Bindable
    public URI getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(URI sourceUrl) {
        this.sourceUrl = sourceUrl;
        notifyPropertyChanged(BR.sourceUrl);
    }

    @Bindable
    public SongStatus getStatus() {
        return status;
    }

    public void setStatus(SongStatus status) {
        this.status = status;
        notifyPropertyChanged(BR.status);
    }

    @Bindable
    public int getSecondsLength() {
        return secondsLength;
    }

    public void setSecondsLength(int secondsLength) {
        this.secondsLength = secondsLength;
        notifyPropertyChanged(BR.secondsLength);
    }

    public SessionModel getPartOfSession() {
        return partOfSession;
    }

    @Bindable
    public DateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(DateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
        notifyPropertyChanged(BR.creationDateTime);
    }

    public File getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(File downloadPath) {
        this.downloadPath = downloadPath;
    }

    public boolean isDownloadStarted() {
        return downloadStarted;
    }

    public void setDownloadStarted(boolean downloadStarted) {
        this.downloadStarted = downloadStarted;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }
}
