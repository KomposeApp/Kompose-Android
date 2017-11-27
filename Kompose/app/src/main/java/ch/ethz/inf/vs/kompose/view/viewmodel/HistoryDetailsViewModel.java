package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.ObservableList;

import java.util.Observable;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;

/**
 * Created by git@famoser.ch on 23/11/2017.
 */

public class HistoryDetailsViewModel extends BaseViewModel {
    private SessionModel sessionModel;

    public HistoryDetailsViewModel(SessionModel sessionModel) {
        this.sessionModel = sessionModel;
    }

    public SessionModel getSessionModel() {
        return sessionModel;
    }
}
