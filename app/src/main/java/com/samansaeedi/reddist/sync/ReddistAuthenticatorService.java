package com.samansaeedi.reddist.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by captain on 8/29/14.
 */
public class ReddistAuthenticatorService extends Service {

    private ReddistAuthenticator reddistAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        reddistAuthenticator = new ReddistAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return reddistAuthenticator.getIBinder();
    }
}
