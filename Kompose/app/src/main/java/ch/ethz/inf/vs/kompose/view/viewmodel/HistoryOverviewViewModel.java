package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.ObservableList;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.comparators.SessionComparator;
import ch.ethz.inf.vs.kompose.model.comparators.UniqueModelComparator;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;


public class HistoryOverviewViewModel extends BaseObservable {
    private ObservableList<SessionModel> sessionModels;

    public HistoryOverviewViewModel() {
        this.sessionModels = new ObservableUniqueSortedList<>(new SessionComparator(), new UniqueModelComparator<SessionModel>());
    }

    public ObservableList<SessionModel> getSessionModels() {
        return sessionModels;
    }
}
