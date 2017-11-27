package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.Bindable;

import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;

/**
 * Created by git@famoser.ch on 23/11/2017.
 */

public class PartyCreationViewModel extends BaseViewModel {

    private String clientName;

    @Bindable
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    private String sessionName;

    @Bindable
    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
}
