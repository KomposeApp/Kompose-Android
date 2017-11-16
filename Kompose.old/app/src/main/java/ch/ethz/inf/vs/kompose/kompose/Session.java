package ch.ethz.inf.vs.kompose.kompose;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class Session {

    public String sessionName;
    public String hostUserName;
    public UUID hostUUID;
    public Map<UUID, String> clients;
    public Queue<PlaylistItem> playlist;

    public Session(String sessionName,
                   String hostUserName,
                   UUID hostUUID,
                   Map<UUID, String> clients,
                   Queue<PlaylistItem> playlist) {
        this.sessionName = sessionName;
        this.hostUserName = hostUserName;
        this.hostUUID = hostUUID;
        this.clients  = clients;
        this.playlist = playlist;
    }

    public Session(JSONObject json) throws JSONException {
        this.sessionName = json.optString("session_name", null);
        this.hostUserName = json.optString("host_username", null);

        String hostUUIDParse = json.optString("host_uuid", null);
        if (hostUUIDParse != null) {
            this.hostUUID = UUID.fromString(hostUUIDParse);
        } else {
            this.hostUUID = null;
        }

        this.clients = jsonToClientMap(json.optJSONObject("clients"));
        this.playlist = jsonToPlaylistQueue(json.optJSONArray("playlist"));
    }

    // TODO
    private Map<UUID, String> jsonToClientMap(JSONObject json) {
        return null;
    }

    // TODO
    private Queue<PlaylistItem> jsonToPlaylistQueue(JSONArray json) {
        // build the playlist from JSON
//        this.playQueue = new LinkedList<>();
//        JSONArray playlistArray = json.optJSONArray("playlist");
//        if (playlistArray != null) {
//            for (int i = 0; i < playlistArray.length(); i++) {
//                JSONObject playlistItemJSON = playlistArray.optJSONObject(i);
//                if (playlistItemJSON != null) {
//                    this.playQueue.add(new PlaylistItem(context, playlistItemJSON));
//                }
//            }
//        }
        return null;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.putOpt("session_name", sessionName);
        json.putOpt("host_username", hostUserName);
        json.putOpt("host_uuid", hostUUID.toString());

        if (clients != null && clients.size() > 0) {
            JSONObject clientsJSON = new JSONObject();
            for (Map.Entry<UUID, String> e : clients.entrySet()) {
                clientsJSON.put(e.getKey().toString(), e.getValue());
            }
            json.put("clients", clientsJSON);
        }

        if (playlist != null && playlist.size() > 0) {
            JSONArray playlistArray = new JSONArray();
            for (PlaylistItem p : playlist) {
                playlistArray.put(p.toJSON());
            }
            json.put("playlist", playlistArray);
        }
        return json;
    }

    public void addItem(PlaylistItem playlistItem) {
        playlist.add(playlistItem);
    }

    public void removeItem(int id) {
        for (PlaylistItem p : playlist) {
            if (p.getId() == id) {
                playlist.remove(p);
                break;
            }
        }
    }
}
