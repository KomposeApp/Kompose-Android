package ch.ethz.inf.vs.kompose.service;

public interface SimpleListener<TMessage, TPayload> {
    void onEvent(TMessage status, TPayload value);
}
