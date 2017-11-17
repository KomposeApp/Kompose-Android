package ch.ethz.inf.vs.kompose.enums;

public enum MessageType {
    REQUEST_INFORMATION,
    REGISTER_CLIENT,
    UNREGISTER_CLIENT,
    SESSION_UPDATE,
    REQUEST_SONG,
    VOTE_SKIP_SONG,
    KEEP_ALIVE,
    FINISH_SESSION,
    ERROR;

    public MessageType fromString(String message) {
        // todo
        return MessageType.ERROR;
    }
}
