package com.dmtaiwan.alexander.iloveyoubike.Utilities;

/**
 * Created by Alexander on 9/15/2015.
 */
public class RecyclerEvent {
    private int mStationId;
    private RecyclerAdapterStation.ViewHolder vh;

    public void setVh(RecyclerAdapterStation.ViewHolder vh) {
        this.vh = vh;
    }

    public RecyclerAdapterStation.ViewHolder getVh() {
        return vh;
    }

    public void setStationId(int id) {
        this.mStationId = id;
    }

    public int getStationId() {
        return mStationId;
    }
}
