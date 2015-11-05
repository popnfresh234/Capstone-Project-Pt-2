package com.dmtaiwan.alexander.iloveyoubike.Bus;

import com.dmtaiwan.alexander.iloveyoubike.Models.Station;

import java.util.List;

/**
 * Created by Alexander on 11/6/2015.
 */
public class StationListEvent {

    public StationListEvent(List<Station> stations) {
        mStationList = stations;
    }

    private List<Station> mStationList;

    public List<Station> getStationList() {
        return mStationList;
    }

    public void setStationList(List<Station> mStationList) {
        this.mStationList = mStationList;
    }
}
