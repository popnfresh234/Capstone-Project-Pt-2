package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.location.Location;
import android.util.Log;

import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by lenovo on 7/29/2015.
 */
public class Utilities {
    private static final String LOG_TAG = Utilities.class.getSimpleName();

    public static final String EXTRA_STATION_ID = "com.dmtaiwan.alexander.extra.stationid";

    public static final String SHARED_PREFS_LOCATION_KEY = "com.dmtaiwan.alexander.key.location";

    public static final double TAIPEI_LAT = 25.0333;
    public static final double TAIPEI_LONG = 121.6333;

    //Use calculated distance between two points to sort stations based on proximity.  (StationLat-LocationLat)^2 + (StationLong - LocationLong)^2 = Distance^2
    //SQLITE can't perform sqrt function but not necessary for sort order
    public static final String getSortOrderDistanceString(double lat, double longitude) {
        return "((" + String.valueOf(lat) +
                "-" + StationContract.StationEntry.COLUMN_STATION_LAT + ") * (" + String.valueOf(lat) + "-"
                + StationContract.StationEntry.COLUMN_STATION_LAT + ") +(" + String.valueOf(longitude) + "-"
                + StationContract.StationEntry.COLUMN_STATION_LONG + ") * (" + String.valueOf(longitude) + "-"
                + StationContract.StationEntry.COLUMN_STATION_LONG + "))";
    }

    public static String formatTime(String string) {
        Log.i(LOG_TAG, string);
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        String timeString = "";
        try {
            Date date = format.parse(string);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            String hourString = String.valueOf(hour);
            String minuteString = String.valueOf(minute);
            if (minute >= 10) {
                timeString = hourString + ":" + minuteString;
            }
            if (minute < 10) {
                timeString = hourString + ":0" + minuteString;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timeString;
    }


    public static float calculateDistance(double stationLat, double stationLong, Location userLocation) {
        Location stationLocation = new Location("");

        stationLocation.setLatitude(stationLat);
        stationLocation.setLongitude(stationLong);

        return userLocation.distanceTo(stationLocation);
    }

    public static String formatDistance(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDec(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }

}
