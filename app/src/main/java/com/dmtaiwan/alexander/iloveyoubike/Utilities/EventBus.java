package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import com.squareup.otto.Bus;

/**
 * Created by Alexander on 9/14/2015.
 */
public class EventBus {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

}
