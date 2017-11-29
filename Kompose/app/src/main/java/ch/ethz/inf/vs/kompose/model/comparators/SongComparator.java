package ch.ethz.inf.vs.kompose.model.comparators;

import java.util.Comparator;

import ch.ethz.inf.vs.kompose.model.SongModel;

public class SongComparator implements Comparator<SongModel> {
    @Override
    public int compare(SongModel s1, SongModel s2) {
        return s1.getOrder() - s2.getOrder();
    }
}