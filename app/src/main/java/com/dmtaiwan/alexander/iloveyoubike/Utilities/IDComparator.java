package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import com.dmtaiwan.alexander.iloveyoubike.Models.Station;

import java.util.Comparator;

/**
 * Created by lenovo on 11/6/2015.
 */
public class IDComparator implements Comparator<Station> {

    @Override
    public int compare(Station lhs, Station rhs) {
        return lhs.getId() - rhs.getId();
    }
}
