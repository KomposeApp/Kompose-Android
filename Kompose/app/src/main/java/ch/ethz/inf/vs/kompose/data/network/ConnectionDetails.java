package ch.ethz.inf.vs.kompose.data.network;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;

import ch.ethz.inf.vs.kompose.data.json.Song;

/**
 * Created by git@famoser.ch on 20/11/2017.
 */

public class ConnectionDetails implements Parcelable {

    public ConnectionDetails(InetAddress hostIP, int hostPort) {
        this.hostIP = hostIP;
        this.hostPort = hostPort;
    }

    private InetAddress hostIP;
    private int hostPort;


    protected ConnectionDetails(Parcel in) {
        hostPort = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hostPort);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ConnectionDetails> CREATOR = new Creator<ConnectionDetails>() {
        @Override
        public ConnectionDetails createFromParcel(Parcel in) {
            return new ConnectionDetails(in);
        }

        @Override
        public ConnectionDetails[] newArray(int size) {
            return new ConnectionDetails[size];
        }
    };

    public InetAddress getHostIP() {
        return hostIP;
    }

    public int getHostPort() {
        return hostPort;
    }
}
