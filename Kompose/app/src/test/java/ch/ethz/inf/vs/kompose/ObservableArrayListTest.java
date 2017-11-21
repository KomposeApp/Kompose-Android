package ch.ethz.inf.vs.kompose;

import android.databinding.ObservableList;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList;

public class ObservableArrayListTest {
    @Test
    public void addTest() {
        ObservableList<SongModel> list = getListInstance();

        list.add(getSongModel(1));
        list.add(getSongModel(4));
        list.add(getSongModel(0));

        checkSorted(list);
    }

    @Test
    public void addAllTest() {
        ObservableList<SongModel> list = getListInstance();

        List<SongModel> arrayList = new ArrayList<>();

        arrayList.add(getSongModel(1));
        arrayList.add(getSongModel(1));
        arrayList.add(getSongModel(2));
        arrayList.add(getSongModel(0));

        list.addAll(arrayList);

        checkSorted(list);
    }

    private SongModel getSongModel(int order) {
        SongModel songModel = new SongModel(UUID.randomUUID(), null, null);
        songModel.setOrder(order);
        return songModel;
    }

    private ObservableList<SongModel> getListInstance() {
        return new ObservableUniqueSortedList<>(new Comparator<SongModel>() {
            @Override
            public int compare(SongModel o1, SongModel o2) {
                return o1.getOrder() < o2.getOrder() ? -1 : 1;
            }
        });
    }

    private void checkSorted(ObservableList<SongModel> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).getOrder() >= list.get(i + 1).getOrder()) {
                Assert.fail("not sorted");
            }
        }
    }
}
