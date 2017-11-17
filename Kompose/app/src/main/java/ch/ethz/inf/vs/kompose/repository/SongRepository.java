package ch.ethz.inf.vs.kompose.repository;

import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;

public class SongRepository {

    private StateService mStateService;
    private NetworkService mNetworkService;

    public SongRepository(StateService stateService, NetworkService networkService) {
        mStateService = stateService;
        mNetworkService = networkService;
    }

    /**
     * sends the requested song to the server and puts it into the playlist
     *
     * @param item the new song which should be included in the playlist
     */
    public void requestNewSong(SongModel item) {
        Message msg = new Message();
        msg.setSenderUuid(mStateService.deviceUUID.toString());
        msg.setSongDetails(SongConverter.convert(item));
        msg.setType(MessageType.REQUEST_SONG.toString());

        mNetworkService.sendMessage(msg, mStateService.hostIP, mStateService.hostPort);
    }

    /**
     * down votes this song, possibly removing it from the play queue
     *
     * @param item the song which is disliked
     */
    public void downVoteSong(SongModel item) {
        // TODO
    }
}
