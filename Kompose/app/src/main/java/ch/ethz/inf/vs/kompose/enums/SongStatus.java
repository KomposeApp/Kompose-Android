package ch.ethz.inf.vs.kompose.enums;

public enum SongStatus {
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
     * the song was skipped because it was excluded by popular vote
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
