package ch.ethz.inf.vs.kompose.model.comparators;

import java.util.Comparator;

import ch.ethz.inf.vs.kompose.model.ClientModel;


public class ClientComparator implements Comparator<ClientModel> {
    @Override
    public int compare(ClientModel o1, ClientModel o2) {
        return o1.getName().compareTo(o2.getName());
    }
}