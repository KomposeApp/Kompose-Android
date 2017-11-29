package ch.ethz.inf.vs.kompose.enums;

public enum SessionStatus {
    /**
     * the session is active and a song is playing
     */
    PLAYING,
    /**
     * the session is active and but playback ahs been paused
     */
    PAUSED,
    /**
     * the session has been finished
     */
    FINISHED,
}
