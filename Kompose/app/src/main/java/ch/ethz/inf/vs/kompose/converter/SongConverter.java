package ch.ethz.inf.vs.kompose.converter;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.DownVote;
import ch.ethz.inf.vs.kompose.data.Song;
import ch.ethz.inf.vs.kompose.enums.SongStatus;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

public class SongConverter {

    public static SongModel convert(Song song, ClientModel[] clientModels) {
        SongModel songModel = new SongModel(UUID.fromString(song.getUuid()));
        songModel.setTitle(song.getTitle());
        songModel.setSecondsLength(song.getLength());

        songModel.setOrder(song.getOrder());
        songModel.setValidDownVoteCount(0);

        songModel.setDownloadUrl(URI.create(song.getDownloadUrl()));
        songModel.setThumbnailUrl(URI.create(song.getThumbnailUrl()));
        songModel.setSourceUrl(URI.create(song.getSourceUrl()));

        songModel.setStatus(SongStatus.valueOf(song.getStatus()));

        if (song.getDownVotes() != null) {
            for (int i = 0; i < song.getDownVotes().length; i++) {
                DownVoteModel model = DownVoteConverter.convert(song.getDownVotes()[i], clientModels);
                songModel.getDownVotes().add(model);
                if (model.getClientModel() != null && model.getClientModel().isActive()) {
                    songModel.setValidDownVoteCount(songModel.getValidDownVoteCount() + 1);
                }
            }
        }

        UUID proposedUUID = UUID.fromString(song.getProposedByClientUuid());
        for (ClientModel clientModel : clientModels) {
            if (clientModel.getUuid().equals(proposedUUID)) {
                songModel.setProposedBy(clientModel);
            }
        }


        return songModel;
    }

    public static Song convert(SongModel songModel) {
        Song song = new Song();

        song.setDownloadUrl(songModel.getDownloadUrl().toString());

        List<DownVoteModel> downvoteModels = songModel.getDownVotes();
        DownVote[] downVotes = new DownVote[downvoteModels.size()];
        int i = 0;
        for (DownVoteModel dvm : downvoteModels) {
            DownVote downVote = new DownVote();
            downVote.setCastDateTime(dvm.getCastDateTime().toString());
            downVote.setUuid(dvm.getUuid().toString());

            if (dvm.getClientModel() != null) {
                downVote.setClientUuid(dvm.getClientModel().getUuid().toString());
            }
            downVotes[i] = downVote;
            i++;
        }
        song.setDownVotes(downVotes);

        song.setOrder(songModel.getOrder());
        song.setProposedByClientUuid(songModel.getProposedBy().getUuid().toString());
        song.setSourceUrl(songModel.getSourceUrl().toString());
        song.setStatus(songModel.getStatus().toString());
        song.setThumbnailUrl(songModel.getThumbnailUrl().toString());
        song.setTitle(songModel.getTitle());
        song.setLength(songModel.getSecondsLength());
        song.setUuid(songModel.getUuid().toString());

        return song;
    }
}
