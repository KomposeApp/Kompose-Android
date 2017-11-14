package ch.ethz.inf.vs.kompose.kompose;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {

    public enum MessageType {
        REQUEST_INFORMATION,
        REGISTER_CLIENT,
        UNREGISTER_CLIENT,
        SESSION_UPDATE,
        REQUEST_SONG,
        VOTE_SKIP_SONG,
        ERROR
    }

    private MessageType type;
    private String userName;
    private Session session;
    private String body;

    public Message(MessageType type, String userName, Session session, String body) {
        this.type = type;
        this.userName = userName;
        this.session = session;
        this.body = body;
    }

    // Construct a message from JSON
    public Message(JSONObject json) throws MessageException {
        MessageType msgType = stringTypeToEnum(json.optString("type"));
        if (msgType == null) {
            throw new MessageException("Invalid message type");
        }
        this.userName = json.optString("username");
        this.body = json.optString("body");
        JSONObject session = json.optJSONObject("session");
        this.session = new Session(session);
    }

    // Serialize a message to JSON
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("userName", this.userName);
        json.put("body", this.body);
        json.put("session", this.session.toJSON());
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
