package ch.ethz.inf.vs.kompose.enums;

public enum SongStatus {
    /**
     * the song is currently locally being resolved.
     * if resolving fails, it is removed automatically
     * if resolving succeeds the status is set to requested
     */
    RESOLVING,
    /**
     * the song has been requested at the server, but no answer yet
     */
    REQUESTED,
    /**
     * the song has been put into the queue
     */
    IN_QUEUE,
    /**
     * the song is currently playing
     */
    PLAYING,
    /**
     * the song is currently paused
     */
    PAUSED,
    /**
     * the song was skipped because it could not be downloaded or it was excluded by popular vote
     */
    SKIPPED_BY_POPULAR_VOTE,
    /**
     * the song was skipped because it errored to be played / downlaoded
     */
    SKIPPED_BY_ERROR,
    /**
     * the song was played
     */
    PLAYED;
}
