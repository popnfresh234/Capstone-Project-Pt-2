package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.location.Location;

/**
 * Created by Alexander on 9/14/2015.
 */
public class LocationEvent {
   private Location newLocation;

    public Location getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(Location newLocation) {
        this.newLocation = newLocation;
    }
}
