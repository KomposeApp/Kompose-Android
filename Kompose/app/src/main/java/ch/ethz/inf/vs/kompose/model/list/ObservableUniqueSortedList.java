package ch.ethz.inf.vs.kompose.model.list;

import android.databinding.ObservableArrayList;
import android.util.Log;

import java.util.Collection;
import java.util.Comparator;

import ch.ethz.inf.vs.kompose.model.base.UniqueModel;

/*
 * An observable list that will only insert elements in sorted order.
 * Don't use any other method of adding elements than `add`.
 */
public class ObservableUniqueSortedList<T extends UniqueModel> extends ObservableArrayList<T> {
    private static final String LOG_TAG = "ObservableUniqueSortedL";
    private Comparator<T> orderComparator;
    private Comparator<T> uniqueComparator;

    public ObservableUniqueSortedList(Comparator<T> orderComparator, Comparator<T> uniqueComparator) {
        this.orderComparator = orderComparator;
        this.uniqueComparator = uniqueComparator;
    }

    @Override
    public boolean add(T object) {
        boolean sameIdFound = false;
        boolean sameInstanceFound = false;
        int insertPosition = 0;
        boolean positionFixed = false;
        for (int i = 0; i < super.size(); i++) {
            T myGet = super.get(i);
            if (myGet == object) {
                sameInstanceFound = true;
                break;
            }
            if (uniqueComparator.compare(object, myGet) == 0) {
                sameIdFound = true;
                break;
            }
            if (!positionFixed && orderComparator.compare(object, myGet) < 0) {
                insertPosition = i;
                positionFixed = true;
            }
        }

        if (sameInstanceFound) {
            //this is not so bad
            return false;
        }

        if (sameIdFound) {
            Log.wtf(LOG_TAG, "PANTS ON FIRE: tried to add duplicate object with same UUID!");
            return false;
        }

        insertPosition = positionFixed ? insertPosition : super.size();
        super.add(insertPosition, object);
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
