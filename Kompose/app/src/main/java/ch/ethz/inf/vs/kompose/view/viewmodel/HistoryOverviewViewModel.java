package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.util.Observable;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;

/**
 * Created by git@famoser.ch on 23/11/2017.
 */

public class HistoryOverviewViewModel extends BaseViewModel {
    private ObservableList<SessionModel> sessionModels;

    public HistoryOverviewViewModel() {
        this.sessionModels = new ObservableArrayList<>();
    }

    public ObservableList<SessionModel> getSessionModels() {
        return sessionModels;
    }
}