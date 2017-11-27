package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.util.Observable;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;


public class ConnectViewModel extends BaseViewModel {
    private ObservableList<SessionModel> sessionModels;

    public ConnectViewModel() {
        this.sessionModels = new ObservableArrayList<>();
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
