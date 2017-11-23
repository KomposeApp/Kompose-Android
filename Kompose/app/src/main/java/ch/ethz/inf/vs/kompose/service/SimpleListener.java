package ch.ethz.inf.vs.kompose.service;

interface SimpleListener {
    void onEvent(int status);
    void onEvent(int status, Object object);
}
