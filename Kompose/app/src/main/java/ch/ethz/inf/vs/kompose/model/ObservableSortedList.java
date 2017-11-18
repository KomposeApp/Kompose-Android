package ch.ethz.inf.vs.kompose.model;

import android.databinding.ObservableArrayList;

import java.util.Comparator;
import java.util.Iterator;

/*
 * An observable list that will only insert elements in sorted order.
 * Don't use any other method of adding elements than `add`.
 */
public class ObservableSortedList<T> extends ObservableArrayList<T> {

    Comparator<T> comparator;

    public ObservableSortedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T object) {
        // super.sort is only available in API 24
        Iterator it = super.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (comparator.compare(object, (T) it.next()) == -1) {
                break;
            }
            i++;
        }
        super.add(i, object);
        return true;
    }
}
