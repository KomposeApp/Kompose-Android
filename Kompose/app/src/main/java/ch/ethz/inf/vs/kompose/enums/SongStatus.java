package ch.ethz.inf.vs.kompose.enums;

public enum SongStatus {
    REQUESTED,
    IN_QUEUE,
    EXCLUDED_BY_POPULAR_VOTE,
    DOWNLOAD_FAILED,
    OTHER_ERROR;

    public static SongStatus fromString(String songStatus) {
        SongStatus status = SongStatus.OTHER_ERROR;
        for (SongStatus s : SongStatus.values()) {
            if (s.toString().equals(songStatus)) {
                status = s;
            }
        }
        return status;
    }
}
