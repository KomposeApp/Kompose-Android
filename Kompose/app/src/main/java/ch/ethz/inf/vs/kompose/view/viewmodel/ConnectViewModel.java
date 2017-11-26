package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.Bindable;
import android.databinding.ObservableList;

import java.util.Observable;

import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SessionModel;

/**
 * Created by git@famoser.ch on 23/11/2017.
 */

public class ConnectViewModel extends Observable {
    private ObservableList<SessionModel> sessionModels;

    public ConnectViewModel(ObservableList<SessionModel> sessionModels) {
        this.sessionModels = sessionModels;
    }

    public ObservableList<SessionModel> getSessionModels() {
        return sessionModels;
    }


    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
