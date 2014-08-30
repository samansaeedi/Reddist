package com.samansaeedi.reddist.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by captain on 8/29/14.
 */
public class ReddistSyncService extends Service {
    private static final Object syncAdapterLock = new Object();
    private static ReddistSyncAdapter reddistSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (syncAdapterLock) {
            if (reddistSyncAdapter == null) {
                reddistSyncAdapter = new ReddistSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return reddistSyncAdapter.getSyncAdapterBinder();
    }
}
