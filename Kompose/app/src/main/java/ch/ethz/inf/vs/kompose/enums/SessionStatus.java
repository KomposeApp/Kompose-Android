package ch.ethz.inf.vs.kompose.enums;

public enum SessionStatus {
    /**
     * no song left in queue, or no song ready to be played yet
     */
    WAITING,
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
