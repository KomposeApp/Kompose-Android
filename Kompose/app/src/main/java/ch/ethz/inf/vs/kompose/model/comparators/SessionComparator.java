package ch.ethz.inf.vs.kompose.model.comparators;

import java.util.Comparator;

import ch.ethz.inf.vs.kompose.model.SessionModel;

public class SessionComparator implements Comparator<SessionModel> {
    @Override
    public int compare(SessionModel o1, SessionModel o2) {
        return o1.getCreationDateTime().compareTo(o2.getCreationDateTime());
    }
}