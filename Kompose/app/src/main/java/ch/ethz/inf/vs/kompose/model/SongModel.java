package ch.ethz.inf.vs.kompose.model;

import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;

import org.joda.time.DateTime;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.base.UniqueModel;

public class SongModel extends UniqueModel {
    public SongModel(UUID uuid, ClientModel proposedBy, SessionModel partOfSession) {
        super(uuid);
        this.proposedBy = proposedBy;
        this.partOfSession = partOfSession;
    }

    private String title;
    private String videoID;
    private int secondsLength;
    private int order;

    private int validDownVoteCount;
    private final ObservableList<DownVoteModel> downVotes = new ObservableArrayList<>();
    private ClientModel proposedBy;
    private SessionModel partOfSession;

    private URI sourceUrl;
    private URI downloadUrl;
    private URI thumbnailUrl;

    private boolean skipVoteCasted;
    private DateTime creationDateTime;

    private SongStatus songStatus = SongStatus.RESOLVING;
    private DownloadStatus downloadStatus = DownloadStatus.NOT_STARTED;
    private int downloadProgress = 0;
    private int playbackProgress = 0;

    private Drawable thumbnail;
    private File downloadPath;

    private MediaPlayer mediaPlayer;

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
    public int getDownVoteCount() {
        return validDownVoteCount;
    }

    public void setDownVoteCount(int validDownVoteCount) {
        this.validDownVoteCount = validDownVoteCount;
        notifyPropertyChanged(BR.downVoteCount);
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

    public String getFileName() {
        return getUUID().toString() + ".m4a";
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
    public SongStatus getSongStatus() {
        return songStatus;
    }

    public void setSongStatus(SongStatus songStatus) {
        this.songStatus = songStatus;
        notifyPropertyChanged(BR.songStatus);
    }

    @Bindable
    public int getSecondsLength() {
        return secondsLength;
    }

    public void setSecondsLength(int secondsLength) {
        this.secondsLength = secondsLength;

        notifyPropertyChanged(BR.songLengthMinSec);
        notifyPropertyChanged(BR.secondsLength);
    }

    @Bindable
    public String getSongLengthMinSec() {
        int sec = secondsLength % 60;
        int min = (secondsLength - sec) / 60;
        return min + ":" + (sec < 10 ? "0" : "") + sec;
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

    @Bindable
    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
        notifyPropertyChanged(BR.downloadStatus);
    }

    @Bindable
    public Drawable getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Drawable drawable) {
        this.thumbnail = drawable;
        notifyPropertyChanged(BR.thumbnail);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String id) {
        this.videoID = id;
    }

    @Bindable
    public int getDownloadProgress() {
        return downloadProgress;
    }

    /**
     * value from 0 to 100
     *
     * @param downloadProgress the current download progress
     */
    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
        notifyPropertyChanged(BR.downloadProgress);
    }

    @Bindable
    public int getPlaybackProgress() {
        return playbackProgress;
    }

    public void setPlaybackProgress(int playbackProgress) {
        this.playbackProgress = playbackProgress;
        this.notifyPropertyChanged(BR.playbackProgress);
    }
}
