package ch.ethz.inf.vs.kompose.converter;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.DownVote;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;

public class DownVoteConverter {

    public static DownVoteModel convert(DownVote downVote, ClientModel[] clientModels) {
        // format DateTime as ISO 8601
        DateTimeFormatter isoParser = ISODateTimeFormat.dateTime();
        DownVoteModel downVoteModel = new DownVoteModel(UUID.fromString(downVote.getUuid()));
        downVoteModel.setCastDateTime(isoParser.parseDateTime(downVote.getCastDateTime()));

        UUID clientUUID = UUID.fromString(downVote.getClientUuid());
        for (ClientModel model : clientModels) {
            if (model.getUuid().equals(clientUUID)) {
                downVoteModel.setClientModel(model);
                break;
            }
        }
        return downVoteModel;
    }

    public static DownVote convert(DownVoteModel downVoteModel) {
        DownVote downVote = new DownVote();
        downVote.setCastDateTime(downVoteModel.getCastDateTime().toString());
        downVote.setUuid(downVoteModel.getUuid().toString());
        if (downVoteModel.getClientModel() != null)
            downVote.setClientUuid(downVoteModel.getClientModel().getUuid().toString());
        return downVote;
    }
}
