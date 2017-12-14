package ch.ethz.inf.vs.kompose.enums;

public enum SessionStatus {
    /**
     * Session is not ready
     */
    UNINITIALIZED,
    /**
     * if session has no songs yet, but is ready
     */
    WAITING,
    /**
     * the session is active and a song is playing
     */
    ACTIVE,
    /**
     * the session has been finished
     */
    FINISHED,
}
