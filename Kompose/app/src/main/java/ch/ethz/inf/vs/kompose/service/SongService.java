package ch.ethz.inf.vs.kompose.service;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.model.SongModel;

public class SongService {

    private static final String LOG_TAG = "## Song Service";

    /**
     * sends the requested song to the server
     *
     * @param song the new song which should be included in the playlist
     */
    public void requestNewSong(Song song) {
        song.setUuid(UUID.randomUUID().toString());
        getNetworkService().sendRequestSong(song);
    }

    /**
     * down votes given song
     *
     * @param songModel the song which is disliked
     */
    public void castSkipVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        Song song = songConverter.convert(songModel);
        getNetworkService().sendCastSkipSongVote(song);
    }

    /**
     * Remove the downvote for a song
     *
     * @param songModel Song for which the downvote is revoked
     */
    public void removeSkipVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        Song song = songConverter.convert(songModel);
        getNetworkService().sendRemoveSkipSongVote(song);
    }

}
