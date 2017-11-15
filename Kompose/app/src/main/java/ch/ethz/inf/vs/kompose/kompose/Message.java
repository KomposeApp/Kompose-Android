package ch.ethz.inf.vs.kompose.kompose;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Message {

    public enum MessageType {
        REQUEST_INFORMATION,
        REGISTER_CLIENT,
        UNREGISTER_CLIENT,
        SESSION_UPDATE,
        REQUEST_SONG,
        VOTE_SKIP_SONG,
        KEEP_ALIVE,
        ERROR
    }

    // these correspond to the fields in the JSON protocol
    private MessageType type;
    private String userName;
    private UUID uuid;
    private String body;
    private Session session;
    private PlaylistItem songDetails;

    public Message(MessageType type,
                   String userName,
                   String body,
                   Session session,
                   PlaylistItem playlistItem) {
        this.type = type;
        this.userName = userName;
        this.session = session;
        this.body = body;
        this.songDetails = playlistItem;
    }

    // Construct a message from JSON
    // Context is required for the Session field
    public Message(Context context, JSONObject json) throws MessageException,JSONException {
        MessageType msgType = stringTypeToEnum(json.optString("type"));
        if (msgType == null) {
            throw new MessageException("Invalid message type");
        }

        this.userName = json.optString("username");
        this.uuid = UUID.fromString(json.optString("uuid"));
        this.body = json.optString("body");

        JSONObject sessionJSON = json.optJSONObject("session");
        this.session = new Session(context, sessionJSON);

        JSONObject songDetailsJSON = json.optJSONObject("song_details");
        this.songDetails = new PlaylistItem(context, songDetailsJSON);
    }

    // TODO
    // Serialize a message to JSON
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("userName", userName);
        json.put("body", body);
        if (session != null) {
            json.put("session", session.toJSON());
        } else {
            json.put("sesion", new JSONObject());
        }
        if (songDetails != null) {
            json.put("song_details", songDetails.toJSON());
        } else {
            json.put("song_details", new JSONObject());
        }
        return json;
    }

    private MessageType stringTypeToEnum(String type) {
        MessageType msgType = null;
        for (MessageType m : MessageType.values()) {
            if (m.name().equals(type)) {
                msgType = m;
            }
        }
        return msgType;
    }
}
