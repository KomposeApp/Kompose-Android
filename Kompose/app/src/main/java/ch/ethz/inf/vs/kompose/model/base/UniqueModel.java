package ch.ethz.inf.vs.kompose.model.base;

import android.databinding.BaseObservable;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;

/**
 * Created by git@famoser.ch on 21/11/2017.
 */

public class UniqueModel extends BaseObservable {

    public UniqueModel(UUID uuid) {
        this.uuid = uuid;
    }

    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }
}
