package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.location.Location;

import com.dmtaiwan.alexander.iloveyoubike.Models.Station;

import java.util.Comparator;


/**
 * Created by Alexander on 10/20/2015.
 */
public class DistanceComparator implements Comparator<Station> {
    private Location mLocation;

    public DistanceComparator(Location location) {
        this.mLocation = location;
    }


    private Double distanceFromMe(Station station) {

        if (mLocation == null) {
            double theta = station.getLng() - Utilities.TAIPEI_LONG;
            double dist = Math.sin(deg2rad(station.getLat())) * Math.sin(deg2rad(Utilities.TAIPEI_LAT))
                    + Math.cos(deg2rad(station.getLat())) * Math.cos(deg2rad(Utilities.TAIPEI_LAT))
                    * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            return dist;
        }else{
            double theta = station.getLng() - mLocation.getLongitude();
            double dist = Math.sin(deg2rad(station.getLat())) * Math.sin(deg2rad(mLocation.getLatitude()))
                    + Math.cos(deg2rad(station.getLat())) * Math.cos(deg2rad(mLocation.getLatitude()))
                    * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            return dist;
        }
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public int compare(Station lhs, Station rhs) {
        return distanceFromMe(lhs).compareTo(distanceFromMe(rhs));
    }
}
