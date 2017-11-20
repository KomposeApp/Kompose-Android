package ch.ethz.inf.vs.kompose;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;

import ch.ethz.inf.vs.kompose.model.ObservableSortedList;

public class ObservableArrayListTest {

    @Test
    public void orderTest() {

        Comparator<Integer> integerComparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        };
        ObservableList<Integer> list = new ObservableSortedList<>(integerComparator);

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

        boolean sorted = true;
        for (int i = 0; i < list.size()-1; i++) {
            if (list.get(i) > list.get(i+1)) {
                sorted = false;
            }
        }
        Assert.assertTrue(sorted);
    }
}
