package ch.ethz.inf.vs.kompose.data.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Session implements Parcelable {
    private Client[] clients;
    private String hostUuid;
    private Song[] songs;
    private String sessionName;
    private String sessionStatus;
    private String creationDateTime;
    private String uuid;

    public Session() {}

    protected Session(Parcel in) {
        clients = in.createTypedArray(Client.CREATOR);
        hostUuid = in.readString();
        songs = in.createTypedArray(Song.CREATOR);
        sessionName = in.readString();
        sessionStatus = in.readString();
        creationDateTime = in.readString();
        uuid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(clients, flags);
        dest.writeString(hostUuid);
        dest.writeTypedArray(songs, flags);
        dest.writeString(sessionName);
        dest.writeString(sessionStatus);
        dest.writeString(creationDateTime);
        dest.writeString(uuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };

    @JsonProperty("clients")
    public Client[] getClients() {
        return clients;
    }

    @JsonProperty("clients")
    public void setClients(Client[] value) {
        this.clients = value;
    }

    @JsonProperty("host_uuid")
    public String getHostUuid() {
        return hostUuid;
    }

    @JsonProperty("host_uuid")
    public void setHostUuid(String value) {
        this.hostUuid = value;
    }

    @JsonProperty("songs")
    public Song[] getSongs() {
        return songs;
    }

    @JsonProperty("songs")
    public void setSongs(Song[] value) {
        this.songs = value;
    }

    @JsonProperty("session_name")
    public String getSessionName() {
        return sessionName;
    }

    @JsonProperty("session_name")
    public void setSessionName(String value) {
        this.sessionName = value;
    }

    @JsonProperty("creation_date_time")
    public String getCreationDateTime() {
        return creationDateTime;
    }

    @JsonProperty("creation_date_time")
    public void setCreationDateTime(String value) {
        this.creationDateTime = value;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    @JsonProperty("uuid")
    public void setUuid(String value) {
        this.uuid = value;
    }

    @JsonProperty("session_status")
    public String getSessionStatus() {
        return sessionStatus;
    }

    @JsonProperty("session_status")
    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
}
