package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.util.Comparator;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.comparators.SessionComparator;
import ch.ethz.inf.vs.kompose.model.comparators.UniqueModelComparator;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;
import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;


public class ConnectViewModel extends BaseViewModel {
    private ObservableList<SessionModel> sessionModels;

    public ConnectViewModel() {
        this.sessionModels = new ObservableUniqueSortedList<>(
                new Comparator<SessionModel>() {
                    @Override
                    public int compare(SessionModel o1, SessionModel o2) {
                        //simply return 1 so new objects always end at the end of the list
                        return 1;
                    }
                },
                new UniqueModelComparator<SessionModel>());
    }

    public ObservableList<SessionModel> getSessionModels() {
        return sessionModels;
    }


    private String clientName;

    @Bindable
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
