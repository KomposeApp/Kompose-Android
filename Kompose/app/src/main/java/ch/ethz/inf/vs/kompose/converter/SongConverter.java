package ch.ethz.inf.vs.kompose.converter;

import android.databinding.ObservableList;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.DownVote;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

/** Convert Session data representation to model representation, and vice-versa. **/

public class SongConverter implements IBaseConverter<SongModel, Song> {

    //Client pool used to determine who the song was proposed by.
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
            this.clientModels = (ClientModel[]) clientModels.toArray();
        }
    }

    /** Data --> Model **/
    public SongModel convert(Song song) {

        //resolve client / session
        ClientModel proposedBy = null;
        SessionModel sessionModel = null;
        UUID proposedUUID = UUID.fromString(song.getProposedByClientUuid());
        for (ClientModel clientModel : clientModels) {
            if (clientModel.getUuid().equals(proposedUUID)) {
                proposedBy = clientModel;
                sessionModel = clientModel.getPartOfSession();
            }
        }

        //create song model
        SongModel songModel = new SongModel(UUID.fromString(song.getUuid()), proposedBy, sessionModel);
        songModel.setTitle(song.getTitle());
        songModel.setSecondsLength(song.getLengthInSeconds());

        songModel.setOrder(song.getOrder());
        songModel.setValidDownVoteCount(0);

        songModel.setDownloadUrl(URI.create(song.getDownloadUrl()));
        songModel.setThumbnailUrl(URI.create(song.getThumbnailUrl()));
        songModel.setSourceUrl(URI.create(song.getSourceUrl()));

        songModel.setStatus(SongStatus.valueOf(song.getStatus()));

        //create downVotes
        if (song.getDownVotes() != null) {
            DownVoteConverter downVoteConverter = new DownVoteConverter(clientModels, songModel);
            if (song.getDownVotes() != null) {
                for (int i = 0; i < song.getDownVotes().length; i++) {
                    DownVoteModel model = downVoteConverter.convert(song.getDownVotes()[i]);
                    songModel.getDownVotes().add(model);
                    if (model.getClientModel() != null && model.getClientModel().getIsActive()) {
                        songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() + 1);
                    }
                }
            }
        }

        return songModel;
    }

    public Song convert(SongModel songModel) {
        Song song = new Song();

        song.setUuid(songModel.getUuid().toString());
        song.setOrder(songModel.getOrder());
        song.setTitle(songModel.getTitle());
        song.setLengthInSeconds(songModel.getSecondsLength());

        song.setDownloadUrl(songModel.getDownloadUrl().toString());
        song.setSourceUrl(songModel.getSourceUrl().toString());
        song.setThumbnailUrl(songModel.getThumbnailUrl().toString());

        song.setStatus(songModel.getStatus().toString());
        song.setProposedByClientUuid(songModel.getProposedBy().getUuid().toString());

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
