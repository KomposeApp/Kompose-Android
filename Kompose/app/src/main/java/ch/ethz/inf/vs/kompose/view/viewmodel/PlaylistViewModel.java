package ch.ethz.inf.vs.kompose.view.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import ch.ethz.inf.vs.kompose.BR;
import ch.ethz.inf.vs.kompose.model.SessionModel;

public class PlaylistViewModel extends BaseObservable {
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

    public void onDownVoteClicked(View view) {
        if (listener != null) {
            listener.downVoteCurrentlyClicked(view);
        }
    }


    public interface ClickListener {
        void addSongClicked(View v);

        void playClicked(View v);

        void pauseClicked(View v);

        void downVoteCurrentlyClicked(View v);
    }
}
