package com.dmtaiwan.alexander.iloveyoubike.Sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Alexander on 7/29/2015.
 */
public class IloveyoubikeSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static IloveyoubikeSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            sSyncAdapter = new IloveyoubikeSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
