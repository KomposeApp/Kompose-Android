package ch.ethz.inf.vs.kompose.enums;

public enum MessageType {
    REQUEST_INFORMATION,
    REGISTER_CLIENT,
    UNREGISTER_CLIENT,
    SESSION_UPDATE,
    REQUEST_SONG,
    CAST_SKIP_SONG_VOTE,
    REMOVE_SKIP_SONG_VOTE,
    KEEP_ALIVE,
    FINISH_SESSION,
    ERROR;

    public MessageType fromString(String messageType) {
        MessageType msg = MessageType.ERROR;
        for (MessageType m : MessageType.values()) {
            if (m.toString().equals(messageType)) {
                msg = m;
            }
        }
        return msg;
    }
}
