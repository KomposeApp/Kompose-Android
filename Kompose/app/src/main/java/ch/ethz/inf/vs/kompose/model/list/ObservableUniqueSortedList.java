package ch.ethz.inf.vs.kompose.model.list;

import android.databinding.ObservableArrayList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

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
        boolean smaller = false;
        // super.sort is only available in API 24
        Iterator<T> it = super.iterator();
        int i = 0;
        while (it.hasNext()) {
            T nextObject = it.next();
            //first part: check where to put it
            if (!smaller) {
                if (comparator.compare(object, nextObject) < 0) {
                    smaller = true;
                } else {
                    i++;
                }
            }
            //check if item already exists
            if (object.getUuid().equals(nextObject.getUuid())) {
                found = true;
                break;
            }
        }
        if (found) {
            return false;
        }

        super.add(i, object);
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
