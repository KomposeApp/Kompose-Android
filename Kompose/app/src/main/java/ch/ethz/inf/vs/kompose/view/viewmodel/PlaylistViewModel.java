package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.Bindable;
import android.view.View;

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
    }

    public void onLinkAddClicked(View view) {
        if (listener != null) {
            listener.addSongClicked(view);
        }
    }


    public interface ClickListener {
        void addSongClicked(View v);
    }
}
