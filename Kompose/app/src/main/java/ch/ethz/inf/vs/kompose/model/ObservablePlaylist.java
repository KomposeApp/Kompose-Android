package ch.ethz.inf.vs.kompose.model;

import java.util.Comparator;
import java.util.Observable;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A playlist that can be observed, i.e. a listener can be registered
 * that is called whenever the playlist changes.
 */
public class ObservablePlaylist extends Observable {

    private SortedSet<PlaylistItem> playlistItems;

    public ObservablePlaylist() {
        playlistItems = new TreeSet<>(new PlaylistItemComparator());
    }

    public SortedSet<PlaylistItem> getPlaylistItems() {
        return playlistItems;
    }

    public void setPlaylistItems(SortedSet<PlaylistItem> playlistItems) {
        this.playlistItems = playlistItems;
    }

    private class PlaylistItemComparator implements Comparator<PlaylistItem> {

        @Override
        public int compare(PlaylistItem p1, PlaylistItem p2) {
            return p1.order < p2.order ? -1 : 1;
        }
    }
}
