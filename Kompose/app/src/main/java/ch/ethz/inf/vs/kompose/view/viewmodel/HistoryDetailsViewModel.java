package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.BaseObservable;

import ch.ethz.inf.vs.kompose.model.SessionModel;

public class HistoryDetailsViewModel extends BaseObservable {
    private SessionModel sessionModel;

    public HistoryDetailsViewModel(SessionModel sessionModel) {
        this.sessionModel = sessionModel;
    }

    public SessionModel getSessionModel() {
        return sessionModel;
    }
}
