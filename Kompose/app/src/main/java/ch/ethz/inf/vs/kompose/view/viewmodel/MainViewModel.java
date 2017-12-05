package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.Bindable;
import android.databinding.ObservableList;
import android.view.View;

import java.util.Comparator;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.data.json.Session;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.comparators.UniqueModelComparator;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;
import ch.ethz.inf.vs.kompose.view.viewholder.JoinSessionViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;


public class MainViewModel extends BaseViewModel implements JoinSessionViewHolder.ClickListener {
    private ObservableList<SessionModel> sessionModels;
    private ClickListener clickListener;

    public MainViewModel(ClickListener clickListener) {
        this.clickListener = clickListener;
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
        notifyPropertyChanged(BR.clientName);
    }

    private String sessionName;

    @Bindable
    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
        notifyPropertyChanged(BR.sessionName);
    }

    private String ipAddress;

    private String port;

    @Bindable
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        notifyPropertyChanged(BR.ipAddress);
    }

    @Bindable
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
        notifyPropertyChanged(BR.port);
    }

    public void createSession(View view) {
        if (clickListener != null) {
            clickListener.createSessionClicked();
        }
    }

    @Override
    public void joinButtonClicked(View v, int position) {
        if (clickListener != null) {
            if (getSessionModels().size() > position) {
                SessionModel pressedSession = getSessionModels().get(position);
                if (pressedSession != null) {
                    clickListener.joinSessionClicked(pressedSession);
                }
            }
        }
    }

    public void joinManualClicked(View v) {
        if (clickListener != null) {
            clickListener.joinManualClicked();
        }
    }

    public void openHelpClicked(View v) {
        if (clickListener != null) {
            clickListener.openHelpClicked();
        }
    }

    public void openHistoryClicked(View v) {
        if (clickListener != null) {
            clickListener.openHistoryClicked();
        }
    }

    public interface ClickListener {
        void createSessionClicked();

        void joinSessionClicked(SessionModel sessionModel);

        void joinManualClicked();

        void openHelpClicked();

        void openHistoryClicked();
    }
}
