package ch.ethz.inf.vs.kompose.data.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Song implements Parcelable {

    private String downloadUrl;
    private DownVote[] downVotes;
    private int order;
    private String proposedByClientUuid;
    private String sourceUrl;
    private String songStatus;
    private String downloadStatus;
    private String thumbnailUrl;
    private String title;
    private String videoID;
    private int lengthInSeconds;
    private String uuid;

    public Song() {
    }

    protected Song(Parcel in) {
        downloadUrl = in.readString();
        downVotes = in.createTypedArray(DownVote.CREATOR);
        order = in.readInt();
        proposedByClientUuid = in.readString();
        sourceUrl = in.readString();
        songStatus = in.readString();
        downloadStatus = in.readString();
        thumbnailUrl = in.readString();
        title = in.readString();
        videoID = in.readString();
        lengthInSeconds = in.readInt();
        uuid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(downloadUrl);
        dest.writeTypedArray(downVotes, flags);
        dest.writeInt(order);
        dest.writeString(proposedByClientUuid);
        dest.writeString(sourceUrl);
        dest.writeString(songStatus);
        dest.writeString(downloadStatus);
        dest.writeString(thumbnailUrl);
        dest.writeString(title);
        dest.writeString(videoID);
        dest.writeInt(lengthInSeconds);
        dest.writeString(uuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @JsonProperty("download_url")
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @JsonProperty("download_url")
    public void setDownloadUrl(String value) {
        this.downloadUrl = value;
    }

    @JsonProperty("down_votes")
    public DownVote[] getDownVotes() {
        return downVotes;
    }

    @JsonProperty("down_votes")
    public void setDownVotes(DownVote[] value) {
        this.downVotes = value;
    }

    @JsonProperty("order")
    public int getOrder() {
        return order;
    }

    @JsonProperty("order")
    public void setOrder(int value) {
        this.order = value;
    }

    @JsonProperty("proposed_by_client_uuid")
    public String getProposedByClientUuid() {
        return proposedByClientUuid;
    }

    @JsonProperty("proposed_by_client_uuid")
    public void setProposedByClientUuid(String value) {
        this.proposedByClientUuid = value;
    }

    @JsonProperty("source_url")
    public String getSourceUrl() {
        return sourceUrl;
    }

    @JsonProperty("source_url")
    public void setSourceUrl(String value) {
        this.sourceUrl = value;
    }

    @JsonProperty("songStatus")
    public String getSongStatus() {
        return songStatus;
    }

    @JsonProperty("songStatus")
    public void setSongStatus(String value) {
        this.songStatus = value;
    }

    @JsonProperty("thumbnail_url")
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @JsonProperty("thumbnail_url")
    public void setThumbnailUrl(String value) {
        this.thumbnailUrl = value;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("videoID")
    public void setVideoID(String value) {
        this.videoID = value;
    }
    @JsonProperty("videoID")
    public String getVideoID() {
        return videoID;
    }

    @JsonProperty("title")
    public void setTitle(String value) {
        this.title = value;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    @JsonProperty("uuid")
    public void setUuid(String value) {
        this.uuid = value;
    }

    @JsonProperty("length_in_seconds")
    public int getLengthInSeconds() {
        return lengthInSeconds;
    }

    @JsonProperty("length_in_seconds")
    public void setLengthInSeconds(int lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }

    @JsonProperty("download_status")
    public String getDownloadStatus() {
        return downloadStatus;
    }

    @JsonProperty("download_status")
    public void setDownloadStatus(String downloadStatus) {
        this.downloadStatus = downloadStatus;
    }
}
