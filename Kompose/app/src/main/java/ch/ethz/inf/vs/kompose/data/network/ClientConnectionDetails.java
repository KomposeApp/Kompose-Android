package ch.ethz.inf.vs.kompose.data.network;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.net.Socket;

public class ClientConnectionDetails implements Parcelable {

    public ClientConnectionDetails(Socket socket, DateTime lastRequestReceived) {
        this.socket = socket;
        this.lastRequestReceived = lastRequestReceived;
    }

    private Socket socket;
    private DateTime lastRequestReceived;

    protected ClientConnectionDetails(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ClientConnectionDetails> CREATOR = new Creator<ClientConnectionDetails>() {
        @Override
        public ClientConnectionDetails createFromParcel(Parcel in) {
            return new ClientConnectionDetails(in);
        }

        @Override
        public ClientConnectionDetails[] newArray(int size) {
            return new ClientConnectionDetails[size];
        }
    };

    public Socket getSocket() {
        return socket;
    }

    public DateTime getLastRequestReceived() {
        return lastRequestReceived;
    }

    public void setLastRequestReceived(DateTime lastRequestReceived) {
        this.lastRequestReceived = lastRequestReceived;
    }
}
