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
        DateTimeFormatter isoParser = ISODateTimeFormat.dateTimeNoMillis();
        DownVoteModel downVoteModel = new DownVoteModel(UUID.fromString(downVote.getUuid()));
        downVoteModel.setCastDateTime(isoParser.parseDateTime(downVote.getCastDateTime()));
        for (ClientModel model : clientModels) {
            if (model.getUuid().equals(downVote.getClientUuid())) {
                downVoteModel.setClientModel(model);
                break;
            }
        }
        return downVoteModel;
    }

    public static DownVote convert(DownVoteModel downVoteModel) {
        DownVote downVote = new DownVote();
        downVote.setCastTime(downVoteModel.getCastTime().toString());
        downVote.setClientUuid(downVoteModel.getUuid().toString());
        downVote.setClientUuid(downVoteModel.getClientModel().getUuid().toString());
        return downVote;
    }
}
