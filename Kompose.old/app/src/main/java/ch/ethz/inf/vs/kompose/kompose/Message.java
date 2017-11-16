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

    public MessageType type;
    public String userName;
    public UUID senderUUID;
    public Session session;
    public PlaylistItem songDetails;
    public String errorMessage;

    public Message(MessageType type,
                   String userName,
                   UUID senderUUID,
                   Session session,
                   PlaylistItem playlistItem,
                   String errorMessage) {
        this.type = type;
        this.userName = userName;
        this.senderUUID = senderUUID;
        this.session = session;
        this.songDetails = playlistItem;
        this.errorMessage = errorMessage;
    }

    public Message(JSONObject json) throws MessageException,JSONException {
        MessageType msgType = stringTypeToEnum(json.optString("type"));
        if (msgType == null) {
            throw new MessageException("Invalid message type");
        }

        this.type = msgType;
        this.userName = json.optString("username", null);
        this.errorMessage = json.optString("error_message", null);

        String senderUUIDParse = json.optString("sender_uuid", null);
        if (senderUUIDParse != null) {
            this.senderUUID = UUID.fromString(senderUUIDParse);
        } else {
            this.senderUUID = null;
        }

        JSONObject sessionJSON = json.optJSONObject("session");
        if (sessionJSON != null) {
            this.session = new Session(sessionJSON);
        } else {
            this.session = null;
        }

        JSONObject songDetailsJSON = json.optJSONObject("song_details");
        if (songDetailsJSON != null) {
            this.songDetails = new PlaylistItem(songDetailsJSON);
        } else {
            this.songDetails = null;
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.putOpt("type", type.name());
        json.putOpt("username", userName);
        json.putOpt("sender_uuid", senderUUID);

        if (session != null) {
            json.put("session", session.toJSON());
        }

        if (songDetails != null) {
            json.put("song_details", songDetails.toJSON());
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
