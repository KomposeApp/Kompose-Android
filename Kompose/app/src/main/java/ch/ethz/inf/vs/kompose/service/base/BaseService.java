package ch.ethz.inf.vs.kompose.service.base;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by git@famoser.ch on 20/11/2017.
 */

@SuppressLint("Registered")
public class BaseService extends Service {

    public class LocalBinder extends Binder {
        public BaseService getService() {
            return BaseService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private final IBinder mBinder = new LocalBinder();
}
