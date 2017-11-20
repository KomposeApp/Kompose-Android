package ch.ethz.inf.vs.kompose.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.net.InetAddress;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class StateService extends BaseService {

    private SessionModel liveSession;

    public SessionModel getLiveSession() {
        return liveSession;
    }

    public void setLiveSession(SessionModel liveSession) {
        this.liveSession = liveSession;
    }
}
