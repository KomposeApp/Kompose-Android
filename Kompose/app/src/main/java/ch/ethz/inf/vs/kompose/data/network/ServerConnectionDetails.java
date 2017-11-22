package ch.ethz.inf.vs.kompose.data.network;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;

/**
 * Created by git@famoser.ch on 20/11/2017.
 */

public class ServerConnectionDetails implements Parcelable {

    public ServerConnectionDetails(InetAddress hostIP, int hostPort) {
        this.hostIP = hostIP;
        this.hostPort = hostPort;
    }

    private InetAddress hostIP;
    private int hostPort;


    protected ServerConnectionDetails(Parcel in) {
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

    public static final Creator<ServerConnectionDetails> CREATOR = new Creator<ServerConnectionDetails>() {
        @Override
        public ServerConnectionDetails createFromParcel(Parcel in) {
            return new ServerConnectionDetails(in);
        }

        @Override
        public ServerConnectionDetails[] newArray(int size) {
            return new ServerConnectionDetails[size];
        }
    };

    public InetAddress getHostIP() {
        return hostIP;
    }

    public int getHostPort() {
        return hostPort;
    }
}
