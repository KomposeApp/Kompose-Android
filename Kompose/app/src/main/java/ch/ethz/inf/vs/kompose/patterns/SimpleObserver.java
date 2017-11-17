package ch.ethz.inf.vs.kompose.patterns;

/**
 * Created by git@famoser.ch on 17/11/2017.
 */

public interface SimpleObserver {
    void notify(int message, Object payload);
    void notify(int message);
}
