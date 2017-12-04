package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.Bindable;
import android.view.View;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.view.viewmodel.base.BaseViewModel;

public class PlaylistViewModel extends BaseViewModel {
    private SessionModel sessionModel;
    private ClickListener listener;

    public PlaylistViewModel(SessionModel sessionModel, ClickListener listener) {
        this.sessionModel = sessionModel;
        this.listener = listener;
    }

    public SessionModel getSessionModel() {
        return sessionModel;
    }


    private String searchLink;

    @Bindable
    public String getSearchLink() {
        return searchLink;
    }

    public void setSearchLink(String searchLink) {
        this.searchLink = searchLink;
        notifyPropertyChanged(BR.searchLink);
    }

    public void onLinkAddClicked(View view) {
        if (listener != null) {
            listener.addSongClicked(view);
        }
    }

    public void onPlayClicked(View view) {
        if (listener != null) {
            listener.playClicked(view);
        }
    }

    public void onPauseClicked(View view) {
        if (listener != null) {
            listener.pauseClicked(view);
        }
    }


    public interface ClickListener {
        void addSongClicked(View v);

        void playClicked(View v);

        void pauseClicked(View v);
    }
}
