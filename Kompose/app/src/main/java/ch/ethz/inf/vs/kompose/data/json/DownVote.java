package ch.ethz.inf.vs.kompose.data.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DownVote implements Parcelable {
    private String uuid;
    private String clientUuid;

    public DownVote() {
    }

    protected DownVote(Parcel in) {
        uuid = in.readString();
        clientUuid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(clientUuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownVote> CREATOR = new Creator<DownVote>() {
        @Override
        public DownVote createFromParcel(Parcel in) {
            return new DownVote(in);
        }

        @Override
        public DownVote[] newArray(int size) {
            return new DownVote[size];
        }
    };

    @JsonProperty("client_uuid")
    public String getClientUuid() {
        return clientUuid;
    }

    @JsonProperty("client_uuid")
    public void setClientUuid(String value) {
        this.clientUuid = value;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    @JsonProperty("uuid")
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
