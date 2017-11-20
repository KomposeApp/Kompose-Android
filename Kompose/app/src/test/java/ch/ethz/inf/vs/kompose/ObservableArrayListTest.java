package ch.ethz.inf.vs.kompose;

import android.databinding.ObservableList;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Comparator;

import ch.ethz.inf.vs.kompose.model.ObservableSortedList;

public class ObservableArrayListTest {
    @Test
    public void addTest() {
        ObservableList<Integer> list = getListInstance();

        list.add(0);
        list.add(-10);
        list.add(420);
        list.add(420);
        list.add(421);
        list.add(-3);
        list.add(-8);
        list.add(14);
        list.add(12);
        list.add(-100);
        list.add(-100);

        checkSorted(list);
    }

    @Test
    public void addAllTest() {
        ObservableList<Integer> list = getListInstance();

        list.add(-10);
        list.add(0, 1);

        checkSorted(list);
    }

    private ObservableList<Integer> getListInstance()
    {
        Comparator<Integer> integerComparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        };
        return new ObservableSortedList<>(integerComparator);
    }

    private void checkSorted(ObservableList<Integer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) > list.get(i + 1)) {
                Assert.fail("not sorted");
            }
        }
    }
}
