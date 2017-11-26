package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.ObservableList;

import ch.ethz.inf.vs.kompose.model.SessionModel;

/**
 * Created by git@famoser.ch on 23/11/2017.
 */

public class PartyCreationViewModel {
    private ObservableList<SessionModel> sessionModels;

    public PartyCreationViewModel(ObservableList<SessionModel> sessionModels) {
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
