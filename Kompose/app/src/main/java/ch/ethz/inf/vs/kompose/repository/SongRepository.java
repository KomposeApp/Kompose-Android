package ch.ethz.inf.vs.kompose.repository;

import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.Message;
import ch.ethz.inf.vs.kompose.enums.MessageType;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.NetworkService;
import ch.ethz.inf.vs.kompose.service.StateService;

public class SongRepository {

    private NetworkService networkService;

    public SongRepository(NetworkService networkService) {
        this.networkService = networkService;
    }

    /**
     * sends the requested song to the server and puts it into the playlist
     *
     * @param songModel the new song which should be included in the playlist
     */
    public void requestNewSong(SongModel songModel) {
        Message msg = new Message();
        msg.setSenderUuid(StateService.getInstance().deviceUUID.toString());
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        msg.setSongDetails(songConverter.convert(songModel));
        msg.setType(MessageType.REQUEST_SONG.toString());

        networkService.sendMessage(msg, StateService.getInstance().liveSession.getHostIP(),
                StateService.getInstance().liveSession.getHostPort(), null);
    }

    /**
     * down votes this song, possibly removing it from the play queue
     *
     * @param songModel the song which is disliked
     */
    public void downVoteSong(SongModel songModel) {
        Message msg = new Message();
        msg.setType(MessageType.CAST_SKIP_SONG_VOTE.toString());
        msg.setSenderUuid(StateService.getInstance().deviceUUID.toString());
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        msg.setSongDetails(songConverter.convert(songModel));

        networkService.sendMessage(msg, StateService.getInstance().liveSession.getHostIP(),
                StateService.getInstance().liveSession.getHostPort(), null);
    }

    /**
     * Remove the downvote for a song
     *
     * @param songModel Song for which the downvote is revoked
     */
    public void removeDownVoteSong(SongModel songModel) {
        Message msg = new Message();
        msg.setType(MessageType.REMOVE_SKIP_SONG_VOTE.toString());
        msg.setSenderUuid(StateService.getInstance().deviceUUID.toString());
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        msg.setSongDetails(songConverter.convert(songModel));

        networkService.sendMessage(msg, StateService.getInstance().liveSession.getHostIP(),
                StateService.getInstance().liveSession.getHostPort(), null);
    }
}
