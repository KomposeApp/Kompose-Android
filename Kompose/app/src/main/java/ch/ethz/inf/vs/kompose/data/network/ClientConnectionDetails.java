package ch.ethz.inf.vs.kompose.data.network;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.net.InetAddress;
import java.net.Socket;

public class ClientConnectionDetails implements Parcelable {

    public ClientConnectionDetails(InetAddress clientIP, int clientPort, DateTime lastRequestReceived) {
        this.ip = clientIP;
        this.port = clientPort;
        this.lastRequestReceived = lastRequestReceived;
    }

    private InetAddress ip;
    private int port;
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

    public DateTime getLastRequestReceived() {
        return lastRequestReceived;
    }

    public void setLastRequestReceived(DateTime lastRequestReceived) {
        this.lastRequestReceived = lastRequestReceived;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
