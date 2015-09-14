package com.dmtaiwan.alexander.iloveyoubike.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Alexander on 9/14/2015.
 */
public class IloveyoubikeSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static IloveyoubikeSyncAdapter sIloveyoubikeSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sIloveyoubikeSyncAdapter == null) {
                sIloveyoubikeSyncAdapter = new IloveyoubikeSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sIloveyoubikeSyncAdapter.getSyncAdapterBinder();
    }
}
