package ch.ethz.inf.vs.kompose.model;

import java.util.Comparator;
import java.util.Observable;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A playlist that can be observed, i.e. a listener can be registered
 * that is called whenever the playlist changes.
 *
 * famoser: this will not work this way; switch to ObservableList and override add(), else all
 */
public class PlayListModel extends Observable {

    private SortedSet<SongModel> playlistItems;

    public PlayListModel() {
        playlistItems = new TreeSet<>(new PlaylistItemComparator());
    }

    public SortedSet<SongModel> getPlaylistItems() {
        return playlistItems;
    }

    public void setPlaylistItems(SortedSet<SongModel> playlistItems) {
        this.playlistItems = playlistItems;
    }

    private class PlaylistItemComparator implements Comparator<SongModel> {

        @Override
        public int compare(SongModel p1, SongModel p2) {
            return p1.getOrder() < p2.getOrder() ? -1 : 1;
        }
    }
}
