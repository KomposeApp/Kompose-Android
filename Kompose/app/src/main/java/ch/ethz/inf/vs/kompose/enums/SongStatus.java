package ch.ethz.inf.vs.kompose.enums;

/**
 * Created by git@famoser.ch on 17/11/2017.
 */

public enum SongStatus {
    REQUESTED,
    IN_QUEUE,
    EXCLUDED_BY_POPULAR_VOTE,
    DOWNLOAD_FAILED,
    OTHER_ERROR;

    public SongStatus fromString(String message) {
        // todo
        return SongStatus.REQUESTED;
    }
}
