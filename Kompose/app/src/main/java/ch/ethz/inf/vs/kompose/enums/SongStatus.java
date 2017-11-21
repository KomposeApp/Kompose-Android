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
     * the song is in queue, but will be skipped once its his turn because it has too many negative votes
     */
    EXCLUDED_BY_POPULAR_VOTE,
    /**
     * the song could not be downloaded at the server and will therfore be skipped once its his turn
     */
    DOWNLOAD_FAILED,
    /**
     * the song is currently playing
     */
    PLAYING,
    /**
     * the song was skipped because it could not be downloaded or it was excluded by popular vote
     */
    SKIPPED,
    /**
     * the song was played
     */
    PLAYED;
}
