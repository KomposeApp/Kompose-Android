package ch.ethz.inf.vs.kompose.converter;

import android.databinding.ObservableList;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.json.DownVote;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.DownVoteModel;
import ch.ethz.inf.vs.kompose.model.SongModel;

/** Convert DownVote data representation to model representation, and vice-versa. **/

public class DownVoteConverter implements IBaseConverter<DownVoteModel, DownVote> {

    //Pool of clients to pull the correct downvoter from.
    private ClientModel[] clientModels;
    private SongModel downVoteFor;

    public DownVoteConverter(ClientModel[] clientModels, SongModel downVoteFor) {
        this.clientModels = clientModels;
        this.downVoteFor = downVoteFor;
    }

    public DownVoteConverter(ObservableList<ClientModel> clientModels, SongModel downVoteFor) {
        if (clientModels.size() == 0) {
            this.clientModels = new ClientModel[0];
        } else {
            this.clientModels = (ClientModel[]) clientModels.toArray();
        }
        this.downVoteFor = downVoteFor;
    }

    /** Data --> Model **/
    public DownVoteModel convert(DownVote downVote) {

        UUID clientUUID = UUID.fromString(downVote.getClientUuid());
        ClientModel clientModel = null;
        for (ClientModel model : clientModels) {
            if (model.getUuid().equals(clientUUID)) {
                clientModel = model;
                break;
            }
        }


        return new DownVoteModel(UUID.fromString(downVote.getUuid()), clientModel, downVoteFor);
    }

    /** Model --> Data **/
    public DownVote convert(DownVoteModel downVoteModel) {
        DownVote downVote = new DownVote();
        downVote.setUuid(downVoteModel.getUuid().toString());
        if (downVoteModel.getClientModel() != null)
            downVote.setClientUuid(downVoteModel.getClientModel().getUuid().toString());
        return downVote;
    }
}
