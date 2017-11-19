package ch.ethz.inf.vs.kompose.model;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.util.Comparator;
import java.util.Observable;
import java.util.SortedSet;
import java.util.TreeSet;

public class PlayListModel {

    private final ObservableList<SongModel> playlistItems = new ObservableSortedList<>(
            new SongComparator());

    public ObservableList<SongModel> getPlaylistItems() {
        return playlistItems;
    }

    private class SongComparator implements Comparator<SongModel> {
        @Override
        public int compare(SongModel s1, SongModel s2) {
            return s1.getOrder() < s2.getOrder() ? -1 : 1;
        }
    }

}
