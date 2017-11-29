package ch.ethz.inf.vs.kompose.model.comparators;

import java.util.Comparator;

import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.model.base.UniqueModel;

public class UniqueModelComparator<T extends UniqueModel> implements Comparator<T> {
    @Override
    public int compare(T s1, T s2) {
        return s1.getUuid().compareTo(s2.getUuid());
    }
}