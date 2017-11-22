package ch.ethz.inf.vs.kompose.data.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Client implements Parcelable {
    private boolean isActive;
    private String name;
    private String uuid;

    public Client() {
    }

    protected Client(Parcel in) {
        isActive = in.readByte() != 0;
        name = in.readString();
        uuid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(name);
        dest.writeString(uuid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size) {
            return new Client[size];
        }
    };

    @JsonProperty("is_active")
    public boolean getIsActive() {
        return isActive;
    }

    @JsonProperty("is_active")
    public void setIsActive(boolean value) {
        this.isActive = value;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String value) {
        this.name = value;
    }

    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    @JsonProperty("uuid")
    public void setUuid(String value) {
        this.uuid = value;
    }
}
