package ch.ethz.inf.vs.kompose.view.viewmodel;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;

public class HistoryDetailsViewModel extends BaseViewModel {
    private SessionModel sessionModel;

    public HistoryDetailsViewModel(SessionModel sessionModel) {
        this.sessionModel = sessionModel;
    }

    public SessionModel getSessionModel() {
        return sessionModel;
    }
}
