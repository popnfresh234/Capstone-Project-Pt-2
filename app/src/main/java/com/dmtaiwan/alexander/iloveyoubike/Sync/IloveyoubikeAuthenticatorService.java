package com.dmtaiwan.alexander.iloveyoubike.Sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Alexander on 7/29/2015.
 */
public class IloveyoubikeAuthenticatorService extends Service{

    private IloveyoubikeAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new IloveyoubikeAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
