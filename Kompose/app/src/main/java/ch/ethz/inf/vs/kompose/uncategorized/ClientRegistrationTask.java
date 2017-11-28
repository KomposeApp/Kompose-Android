package ch.ethz.inf.vs.kompose.uncategorized;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import ch.ethz.inf.vs.kompose.data.json.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.StateSingleton;


public class ClientRegistrationTask extends AsyncTask<Void, Void, Socket> {

    private static final String LOG_TAG = "## RegistrationTask";
    private ServerSocket hostSocket;
    private InetAddress hostAddr;

    public ClientRegistrationTask() throws Exception{
        //Retrieve connection details of active session:
        SessionModel session = StateSingleton.getInstance().activeSession;
        if ( session == null || session.getConnectionDetails() == null){
            throw new RuntimeException("Session Model was null or the connections within were null.");
        }
        hostAddr = session.getConnectionDetails().getHostIP();
        //TODO: Set this port to user's own chosen port, not that of the host -- Will need to tell host which port we want to use.
        int localPort = session.getConnectionDetails().getHostPort();
        hostSocket = new ServerSocket(localPort);
        hostSocket.setSoTimeout(StateSingleton.getInstance().getFixedTimeout());
    }

    /**
     * Wait for a connection from the host, then accept and verify whether it is a proper response to the registration.
     * Yes I know, the try/catch blocks are ugly as sin, but alas, it's Java Sockets.
     * @return Connection to the host
     */
    @Override
    protected Socket doInBackground(Void... voids) {
        Socket connection = null;
        try {
            connection = hostSocket.accept();

            if (connection.getInetAddress().equals(hostAddr)) {

                //Read message out message
                ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
                Object o =  input.readObject();
                Message msg = null;

                //TODO: Test if this is really a good way to implement this. Maybe loop until we find right message, in case InputStream is cluttered with garbage data?
                if (o instanceof Message) msg = (Message) o;
                else throw new RuntimeException("Received message did not match necessary type.");

                Log.d(LOG_TAG, "message read from stream: " + msg.toString());
                input.close();

                // Check whether the message we received matches the expected message. If not, close the connection.
                // TODO: Add UUID check for increased security
                if (MessageType.valueOf(msg.getType()) == MessageType.REGISTER_SUCCESSFUL) {
                    return connection;
                }else{
                    throw new RuntimeException("Type of message did not match REGISTER_SUCCESSFUL");
                }

            } else {
                // If the IP doesn't match that of the host, stop immediately. (really cheap security)
                throw new RuntimeException("Unexpected IP address trying to establish a connection.");
            }
        }
        catch(Exception e){

            //Cleanup failed connection
            if (connection != null){
                try {
                    connection.close();
                } catch (IOException io) {
                    Log.e(LOG_TAG, "Cleaning up the connection with host failed");
                    io.printStackTrace();
                }
            }

            e.printStackTrace();
            Log.w(LOG_TAG, e.getMessage());
            return null;
        } finally{
            try {
                hostSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Closing the ServerSocket failed. This may have consequences.");
                e.printStackTrace();
            }
        }
    }

}
