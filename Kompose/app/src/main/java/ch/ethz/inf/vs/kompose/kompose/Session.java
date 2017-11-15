package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class Session {

    private String sessionName;
    private String hostUserName;
    private UUID hostUUID;
    private Queue<PlaylistItem> playQueue;

    private Context context;

    // creates an empty playlist
    public Session(Context context,
                   String sessionName,
                   String hostUserName,
                   UUID hostUUID) {
        this.context = context;
        this.sessionName = sessionName;
        this.hostUserName = hostUserName;
        this.hostUUID = hostUUID;
        this.playQueue = new LinkedList<>();
    }

    public Session(Context context, JSONObject json) throws JSONException {
        this.context = context;
        this.sessionName = json.optString("session_name");
        this.hostUserName = json.optString("host_username");
        this.hostUUID = UUID.fromString(json.optString("host_uuid"));

        // build the playlist from JSON
        this.playQueue = new LinkedList<>();
        JSONArray playlistArray = json.optJSONArray("playlist");
        if (playlistArray != null) {
            for (int i = 0; i < playlistArray.length(); i++) {
                JSONObject playlistItemJSON = playlistArray.optJSONObject(i);
                if (playlistItemJSON != null) {
                    this.playQueue.add(new PlaylistItem(context, playlistItemJSON));
                }
            }
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("session_name", sessionName);
        json.put("host_username", hostUserName);
        json.put("host_uuid", hostUUID.toString());
        JSONArray playlistArray = new JSONArray();
        for (PlaylistItem p : playQueue) {
            playlistArray.put(p.toJSON());
        }
        json.put("playlist", playlistArray);
        return json;
    }

    public void addItem(PlaylistItem playlistItem) {
        playQueue.add(playlistItem);
    }

    public void removeItem(int id) {
        for (PlaylistItem p : playQueue) {
            if (p.getId() == id) {
                playQueue.remove(p);
                break;
            }
        }
    }
}
