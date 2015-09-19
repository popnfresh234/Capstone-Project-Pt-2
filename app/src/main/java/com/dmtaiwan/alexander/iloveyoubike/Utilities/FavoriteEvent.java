package com.dmtaiwan.alexander.iloveyoubike.Utilities;

/**
 * Created by Alexander on 9/14/2015.
 */
public class FavoriteEvent {
    private int stationId;
    private  boolean favorited = false;

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    private boolean getFavorited() {
        return favorited;
    }

    private void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }
}
