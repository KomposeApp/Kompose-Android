package ch.ethz.inf.vs.kompose.model.list;

import android.databinding.ObservableArrayList;

import java.util.Collection;
import java.util.Comparator;
import ch.ethz.inf.vs.kompose.model.base.UniqueModel;

/*
 * An observable list that will only insert elements in sorted order.
 * Don't use any other method of adding elements than `add`.
 */
public class ObservableUniqueSortedList<T extends UniqueModel> extends ObservableArrayList<T> {

    private Comparator<T> comparator;

    public ObservableUniqueSortedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T object) {
        boolean found = false;
        int insertIdx = 0;
        boolean idxSet = false;
        for (int i = 0; i < super.size(); i++) {
            if (comparator.compare(super.get(i), object) == 0) {
                found = true;
                break;
            }
            if (!idxSet && comparator.compare(object, get(i)) < 0) {
                insertIdx = i;
                idxSet = true;
            }
        }

        if (found) {
            return false;
        }

        insertIdx = idxSet ? insertIdx : super.size();
        super.add(insertIdx, object);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        for (T elem : collection) {
            add(elem);
        }
        return true;
    }

    @Override
    public void add(int index, T object) {
        //ignore add with index if call is on this objects, as it could break invariant
        add(object);
    }
}
