package ch.ethz.inf.vs.kompose.repository;

import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;

public class SongRepository {

    private NetworkService networkService;
    private StateService stateService;

    public SongRepository(NetworkService networkService, StateService stateService) {
        this.networkService = networkService;
        this.stateService = stateService;
    }

    /**
     * sends the requested song to the server and puts it into the playlist
     *
     * @param songModel the new song which should be included in the playlist
     */
    public void requestNewSong(SongModel songModel) {
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        Song song = songConverter.convert(songModel);
        networkService.sendRequestSong(stateService.getLiveSession().getConnectionDetails(), song);
    }

    /**
     * down votes this song, possibly removing it from the play queue
     *
     * @param songModel the song which is disliked
     */
    public void castSkipVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        Song song = songConverter.convert(songModel);
        networkService.sendCastSkipSongVote(stateService.getLiveSession().getConnectionDetails(), song);
    }

    /**
     * Remove the downvote for a song
     *
     * @param songModel Song for which the downvote is revoked
     */
    public void removeSkipVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        Song song = songConverter.convert(songModel);
        networkService.sendRemoveSkipSongVote(stateService.getLiveSession().getConnectionDetails(), song);
    }
}
