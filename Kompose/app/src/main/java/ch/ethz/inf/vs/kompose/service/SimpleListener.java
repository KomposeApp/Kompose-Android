package ch.ethz.inf.vs.kompose.service;

public interface SimpleListener<_STATE, _OBJECT> {
    void onEvent(_STATE status, _OBJECT value);
}
