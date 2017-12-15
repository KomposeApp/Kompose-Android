package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.Bindable;
import android.databinding.ObservableList;
import android.util.Log;
import android.view.View;

import java.util.Comparator;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.comparators.UniqueModelComparator;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;
import ch.ethz.inf.vs.kompose.service.preferences.PreferenceUtility;
import ch.ethz.inf.vs.kompose.view.viewholder.JoinSessionViewHolder;
import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;


public class MainViewModel extends BaseViewModel implements JoinSessionViewHolder.ClickListener {
    private final String LOG_TAG = "##ViewModel";

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

    private boolean enableState;

    private String clientName;
    private String sessionName;
    private String ipAddress;
    private String port;

    public void setEnabled(boolean enabled){
        enableState = enabled;
    }

    public boolean isEnabled(){
        return enableState;
    }

    @Bindable
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
        notifyPropertyChanged(BR.clientName);
    }



    @Bindable
    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
        notifyPropertyChanged(BR.sessionName);
    }



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
        if (clickListener != null && isEnabled()) {
            clickListener.createSessionClicked();
        }else{
            Log.wtf(LOG_TAG, "Prevented consecutive \"Create Session\" button pushing.");
        }
    }

    @Override
    public void joinButtonClicked(View v, int position) {
        if (clickListener != null && isEnabled()) {
            if (getSessionModels().size() > position) {
                SessionModel pressedSession = getSessionModels().get(position);
                if (pressedSession != null) {
                    clickListener.joinSessionClicked(pressedSession);
                }
            }
        }else{
            Log.wtf(LOG_TAG, "Prevented consecutive Join button pushing.");
        }
    }

    public void joinManualClicked(View v) {
        if (clickListener != null && isEnabled()) {
            clickListener.joinManualClicked();
        }else{
            Log.wtf(LOG_TAG, "Prevented consecutive Manual Join button pushing.");
        }
    }

    public void openHistoryClicked(View v) {
        if (clickListener != null && isEnabled()) {
            clickListener.openHistoryClicked();
        } else{
            Log.wtf(LOG_TAG, "Prevented consecutive History button pushing.");
        }
    }

    public void openSettingsClicked(View v) {
        if (clickListener != null && isEnabled()) {
            clickListener.openSettingsClicked();
        } else{
            Log.wtf(LOG_TAG, "Prevented consecutive Settings button pushing.");
        }
    }

    public void setFromPreferences(PreferenceUtility preferences) {
        this.setClientName(preferences.getUsername());
        this.setSessionName(preferences.getSessionName());
        this.setPort(preferences.getDefaultPort());
        this.setIpAddress(preferences.getDefaultIp());
    }

    public void saveToPreferences(PreferenceUtility preferences) {
        preferences.setUsername(getClientName().trim());
        preferences.setSessionName(getSessionName().trim());
        preferences.setDefaultPort(getPort());
        preferences.setDefaultIp(getIpAddress());
        preferences.applyChanges();
    }

    public interface ClickListener {
        void createSessionClicked();

        void joinSessionClicked(SessionModel sessionModel);

        void joinManualClicked();

        void openHistoryClicked();

        void openSettingsClicked();
    }
}
