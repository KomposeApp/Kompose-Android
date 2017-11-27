package ch.ethz.inf.vs.kompose.view.viewmodel;

import java.util.Observable;

import ch.ethz.inf.vs.kompose.model.SessionModel;

/**
 * Created by git@famoser.ch on 23/11/2017.
 */

public class PlaylistViewModel extends Observable {
    private SessionModel sessionModel;

    public PlaylistViewModel(SessionModel sessionModel) {
        this.sessionModel = sessionModel;
    }

    public SessionModel getSessionModel() {
        return sessionModel;
    }
}
