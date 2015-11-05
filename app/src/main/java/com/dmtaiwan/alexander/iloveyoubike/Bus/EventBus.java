package com.dmtaiwan.alexander.iloveyoubike.Bus;

import com.squareup.otto.Bus;

/**
 * Created by Alexander on 10/21/2015.
 */
public class EventBus {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
