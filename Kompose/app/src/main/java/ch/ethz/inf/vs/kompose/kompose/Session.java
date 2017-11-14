package ch.ethz.inf.vs.kompose.kompose;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;

public class Session {

    private Queue<PlaylistItem> playQueue;
    private String sessionName;

    // TODO
    public Session(String sessionName) {
        this.sessionName = sessionName;
    }

    // TODO
    public Session(JSONObject json) {
    }

    // TODO
    public JSONObject toJSON() throws JSONException {
        return null;
    }

    // TODO
    public void addItem(String URL) {
    }

    // TODO
    public void removeItem(String id) {
    }
}
