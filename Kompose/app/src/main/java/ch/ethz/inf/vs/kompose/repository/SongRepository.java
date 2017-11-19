package ch.ethz.inf.vs.kompose.repository;

import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;

public class SongRepository {

    private StateService stateService;
    private NetworkService networkService;

    public SongRepository(StateService stateService, NetworkService networkService) {
        this.stateService = stateService;
        this.networkService = networkService;
    }

    /**
     * sends the requested song to the server and puts it into the playlist
     *
     * @param item the new song which should be included in the playlist
     */
    public void requestNewSong(SongModel item) {
        Message msg = new Message();
        msg.setSenderUuid(stateService.deviceUUID.toString());
        msg.setSongDetails(SongConverter.convert(item));
        msg.setType(MessageType.REQUEST_SONG.toString());

        networkService.sendMessage(msg, stateService.liveSession.getHostIP(),
                stateService.liveSession.getHostPort());
    }

    /**
     * down votes this song, possibly removing it from the play queue
     *
     * @param item the song which is disliked
     */
    public void downVoteSong(SongModel item) {
        Message msg = new Message();
        msg.setType(MessageType.CAST_SKIP_SONG_VOTE.toString());
        msg.setSenderUuid(stateService.deviceUUID.toString());
        msg.setSongDetails(SongConverter.convert(item));

        networkService.sendMessage(msg, stateService.liveSession.getHostIP(),
                stateService.liveSession.getHostPort());
    }

    /**
     * Remove the downvote for a song
     *
     * @param item Song for which the downvote is revoked
     */
    public void removeDownVoteSong(SongModel item) {
        Message msg = new Message();
        msg.setType(MessageType.REMOVE_SKIP_SONG_VOTE.toString());
        msg.setSenderUuid(stateService.deviceUUID.toString());
        msg.setSongDetails(SongConverter.convert(item));

        networkService.sendMessage(msg, stateService.liveSession.getHostIP(),
                stateService.liveSession.getHostPort());
    }
}
