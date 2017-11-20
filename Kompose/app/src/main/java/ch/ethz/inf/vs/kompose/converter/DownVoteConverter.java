package ch.ethz.inf.vs.kompose.converter;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.DownVote;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

public class DownVoteConverter implements IBaseConverter<DownVoteModel, DownVote> {

    private ClientModel[] clientModels;
    private SongModel downVoteFor;

    public DownVoteConverter(ClientModel[] clientModels, SongModel downVoteFor) {
        this.clientModels = clientModels;
        this.downVoteFor = downVoteFor;
    }

    public DownVoteModel convert(DownVote downVote) {

        UUID clientUUID = UUID.fromString(downVote.getClientUuid());
        ClientModel clientModel = null;
        for (ClientModel model : clientModels) {
            if (model.getUuid().equals(clientUUID)) {
                clientModel = model;
                break;
            }
        }


        DownVoteModel downVoteModel = new DownVoteModel(UUID.fromString(downVote.getUuid()), clientModel, downVoteFor);

        // format DateTime as ISO 8601
        DateTimeFormatter isoParser = ISODateTimeFormat.dateTime();
        downVoteModel.setCastDateTime(isoParser.parseDateTime(downVote.getCastDateTime()));

        return downVoteModel;
    }

    public DownVote convert(DownVoteModel downVoteModel) {
        DownVote downVote = new DownVote();
        downVote.setCastDateTime(downVoteModel.getCastDateTime().toString());
        downVote.setUuid(downVoteModel.getUuid().toString());
        if (downVoteModel.getClientModel() != null)
            downVote.setClientUuid(downVoteModel.getClientModel().getUuid().toString());
        return downVote;
    }
}
