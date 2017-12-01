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
     * the song is currently paused
     */
    PAUSED,
    /**
     * the song was skipped because it could not be downloaded or it was excluded by popular vote
     */
    SKIPPED,
    /**
     * the song was played
     */
    PLAYED;
}
