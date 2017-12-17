package ch.ethz.inf.vs.kompose.converter;

import android.databinding.ObservableList;
import android.util.Log;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.DownVote;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.enums.DownloadStatus;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

/**
 * Convert Session data representation to model representation, and vice-versa.
 **/

public class SongConverter implements IBaseConverter<SongModel, Song> {

    private final String LOG_TAG = "##SongConverter";

    //Client pool
    private ClientModel[] clientModels;

    //Constructor for standard arrays (no transformation needed)
    public SongConverter(ClientModel[] clientModels) {
        this.clientModels = clientModels;
    }

    //Constructor for observable list
    public SongConverter(ObservableList<ClientModel> clientModels) {
        if (clientModels.size() == 0) {
            this.clientModels = new ClientModel[0];
        } else {
            this.clientModels = clientModels.toArray(new ClientModel[0]);
        }
    }

    /**
     * Data --> Model
     **/
    public SongModel convert(Song song) {

        //resolve client / session
        ClientModel proposedBy = null;
        SessionModel sessionModel = null;
        if (song.getProposedByClientUuid() != null) {
            UUID proposedUUID = UUID.fromString(song.getProposedByClientUuid());
            for (ClientModel clientModel : clientModels) {
                if (clientModel.getUUID().equals(proposedUUID)) {
                    proposedBy = clientModel;
                    sessionModel = clientModel.getPartOfSession();
                }
            }
        } else{
            //Workaround
            Log.d(LOG_TAG, "ProposedBy was set to null for some reason!");
        }

        //create song model
        SongModel songModel = new SongModel(UUID.fromString(song.getUuid()), proposedBy, sessionModel);
        songModel.setTitle(song.getTitle());
        songModel.setVideoID(song.getVideoID());
        songModel.setSecondsLength(song.getLengthInSeconds());

        songModel.setOrder(song.getOrder());
        songModel.setDownVoteCount(0);

        songModel.setDownloadUrl(URI.create(song.getDownloadUrl()));
        songModel.setThumbnailUrl(URI.create(song.getThumbnailUrl()));
        songModel.setSourceUrl(URI.create(song.getSourceUrl()));

        songModel.setSongStatus(SongStatus.valueOf(song.getSongStatus()));
        songModel.setDownloadStatus(DownloadStatus.valueOf(song.getDownloadStatus()));

        //create downVotes
        if (song.getDownVotes() != null) {
            DownVoteConverter downVoteConverter = new DownVoteConverter(clientModels, songModel);
            if (song.getDownVotes() != null) {
                for (int i = 0; i < song.getDownVotes().length; i++) {
                    DownVoteModel model = downVoteConverter.convert(song.getDownVotes()[i]);
                    songModel.getDownVotes().add(model);
                    if (model.getClientModel() != null && model.getClientModel().getIsActive()) {
                        songModel.setDownVoteCount(songModel.getDownVoteCount() + 1);
                    }
                }
            }
        }

        return songModel;
    }

    public Song convert(SongModel songModel) {
        Song song = new Song();

        song.setUuid(songModel.getUUID().toString());
        song.setOrder(songModel.getOrder());
        song.setTitle(songModel.getTitle());
        song.setVideoID(songModel.getVideoID());
        song.setLengthInSeconds(songModel.getSecondsLength());

        song.setDownloadUrl(songModel.getDownloadUrl().toString());
        song.setSourceUrl(songModel.getSourceUrl().toString());
        song.setThumbnailUrl(songModel.getThumbnailUrl().toString());

        song.setSongStatus(songModel.getSongStatus().toString());
        song.setDownloadStatus(songModel.getDownloadStatus().toString());
        if (songModel.getProposedBy() != null) {
            song.setProposedByClientUuid(songModel.getProposedBy().getUUID().toString());
        } else{
            //Workaround
            Log.d(LOG_TAG, "ProposedBy was set to null for some reason!");
        }

        DownVoteConverter downVoteConverter = new DownVoteConverter(clientModels, songModel);
        List<DownVoteModel> downVoteModels = songModel.getDownVotes();
        DownVote[] downVotes = new DownVote[downVoteModels.size()];
        int i = 0;
        for (DownVoteModel dvm : downVoteModels) {
            downVotes[i] = downVoteConverter.convert(dvm);
            i++;
        }
        song.setDownVotes(downVotes);

        return song;
    }
}
