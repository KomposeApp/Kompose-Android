package ch.ethz.inf.vs.kompose.enums;

public enum SessionStatus {
    /**
     * if session has no songs yet, or default status if server has not responded yet
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
