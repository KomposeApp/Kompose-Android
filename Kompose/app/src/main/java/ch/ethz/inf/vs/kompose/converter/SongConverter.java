package ch.ethz.inf.vs.kompose.converter;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.net.URI;
import java.util.ArrayList;
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
        songModel.setOrder((int) song.getOrder());
        songModel.setDownVoteCount(song.getDownVotes().length);

        List<DownVoteModel> downVoteModels = new ArrayList<>();
        for (int i = 0; i < song.getDownVotes().length; i++) {
            DownVoteModel model = DownVoteConverter.convert(song.getDownVotes()[i], clientModels);
            downVoteModels.add(model);
        }
        songModel.setDownVotes(downVoteModels);

        UUID proposedUUID = UUID.fromString(song.getProposedBy());
        for (ClientModel clientModel : clientModels) {
            if (clientModel.getUuid().equals(proposedUUID)) {
                songModel.setProposedBy(clientModel);
            }
        }

        songModel.setDownloadUrl(URI.create(song.getDownloadUrl()));
        songModel.setThumbnailUrl(URI.create(song.getThumbnailUrl()));
        songModel.setSourceUrl(URI.create(song.getSourceUrl()));

        songModel.setStatus(SongStatus.valueOf(song.getStatus()));

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

            // should output ISO 8601
            downVote.setCastTime(dvm.getCastTime().toString());

            downVote.setClientUuid(dvm.getUuid().toString());
            downVotes[i] = downVote;
            downVotes[i] = downVote;
            i++;
        }
        song.setDownVotes(downVotes);

        song.setOrder(songModel.getOrder());
        song.setProposedBy(songModel.getProposedBy().getUuid().toString());
        song.setSourceUrl(songModel.getSourceUrl().toString());
        song.setStatus(songModel.getStatus().toString());
        song.setThumbnailUrl(songModel.getThumbnailUrl().toString());
        song.setTitle(songModel.getTitle());
        song.setLength(songModel.getSecondsLength());
        song.setUuid(songModel.getUuid().toString());

        return song;
    }
}
