package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class Session {

    private Queue<PlaylistItem> playQueue;
    private String sessionName;
    private String hostUserName;
    private UUID hostUUID;

    private int idCounter = 0;
    private Context context;

    public Session(String sessionName, String hostUserName, UUID hostUUID, Context context) {
        this.sessionName = sessionName;
        this.hostUserName = hostUserName;
        this.hostUUID = hostUUID;
        this.context = context;
        this.playQueue = new LinkedList<>();
    }

    public Session(JSONObject json, Context context) {
        this.context = context;
        this.sessionName = json.optString("session_name");
        this.hostUserName = json.optString("host_username");
        this.hostUUID = UUID.fromString(json.optString("host_UUID"));
        this.playQueue = new LinkedList<>();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject sessionJSON = new JSONObject();
        sessionJSON.put("session_name", sessionName);
        sessionJSON.put("host_username", hostUserName);
        sessionJSON.put("host_UUID", hostUUID.toString());
        JSONArray playlistArray = new JSONArray();
        for (PlaylistItem p : playQueue) {
            playlistArray.put(p.toJSON());
        }
        sessionJSON.put("playlist", playlistArray);
        return sessionJSON;
    }

    public void addItem(String URL) {
        playQueue.add(new PlaylistItem(context, URL, idCounter));
        idCounter++;
    }

    public void removeItem(int id) {
        for (PlaylistItem p : playQueue) {
            if (p.getId() == id) {
                playQueue.remove(p);
            }
        }
    }
}
