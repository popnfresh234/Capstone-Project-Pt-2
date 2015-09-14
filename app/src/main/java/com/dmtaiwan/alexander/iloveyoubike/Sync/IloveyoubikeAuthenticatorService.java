package com.dmtaiwan.alexander.iloveyoubike.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Alexander on 9/14/2015.
 */
public class IloveyoubikeAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private IloveyoubikeAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new IloveyoubikeAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}

