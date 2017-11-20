package ch.ethz.inf.vs.kompose.model;

import android.databinding.ObservableArrayList;

import java.util.Comparator;
import java.util.Iterator;

/*
 * An observable list that will only insert elements in sorted order.
 * Don't use any other method of adding elements than `add`.
 */
public class ObservableSortedList<T> extends ObservableArrayList<T> {

    private Comparator<T> comparator;

    public ObservableSortedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T object) {
        // super.sort is only available in API 24
        Iterator<T> it = super.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (comparator.compare(object, it.next()) < 0) {
                break;
            }
            i++;
        }
        super.add(i, object);
        return true;
    }

    @Override
    public void add(int index, T object) {
        add(object);
    }
}
