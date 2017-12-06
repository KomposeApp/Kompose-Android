package ch.ethz.inf.vs.kompose.service;

import java.util.concurrent.BrokenBarrierException;

public interface SimpleListener<TMessage, TPayload> {
    void onEvent(TMessage status, TPayload value);
}
