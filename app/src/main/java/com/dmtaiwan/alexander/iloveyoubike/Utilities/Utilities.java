package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.StationListFragment;
import com.dmtaiwan.alexander.iloveyoubike.Sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by lenovo on 7/29/2015.
 */
public class Utilities {
    private static final String LOG_TAG = Utilities.class.getSimpleName();

    //Constants for Intents
    public static final String EXTRA_STATION_ID = "com.dmtaiwan.alexander.extra.stationid";
    public static final String EXTRA_FAVORITES = "com.dmtaiwan.alexander.extra.favorites";

    //Constants for shared prefs
    public static final String SHARED_PREFS_LOCATION_KEY = "com.dmtaiwan.alexander.key.location";
    public static final String SHARED_PREFS_FAVORITE_KEY = "com.dmtaiwan.alexander.key.favorite";
    public static final String SHARED_PREFS_DATA_STATUS_KEY = "com.dmtaiwan.alexander.key.data";

    //Constants for saved states
    public static final String OUTSTATE_SCROLL_POSITION = "com.dmtaiwan.alexander.outstate.scroll";

    public static final int ICON_SIZE_SMALL = 0;
    public static final int ICON_SIZE_LARGE = 1;


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

    //Fetch Location object from shared prefs

    public static Location getUserLocation(SharedPreferences prefs) {
        String locationJson = prefs.getString(Utilities.SHARED_PREFS_LOCATION_KEY, "");
        //If a location has been stored in shared prefs, retrieve it and set the lat/long coordinates for the query
        if (!locationJson.equals("")) {
            try {
                Gson gson = new Gson();
                return gson.fromJson(locationJson, Location.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static ArrayList<String> getFavoriteArray(SharedPreferences prefs) {
        String favoriteJson = prefs.getString(Utilities.SHARED_PREFS_FAVORITE_KEY, "");
        if (!favoriteJson.equals("")) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            return gson.fromJson(favoriteJson, type);
        } else {
            return null;
        }
    }

    public static String generateFavoritesWhereString(ArrayList<String> favoritesArray) {
        String selection = StationContract.StationEntry.COLUMN_STATION_ID + " in (";
        for (int i = 0; i < favoritesArray.size() - 1; i++) {
            selection += "?,";
        }
        selection += "?)";
        return selection;
    }

    public static int getStatusIconDrawable(Cursor cursor, int size) {
        int bikesAvailable = cursor.getInt(StationListFragment.COL_BIKES_AVAILABLE);
        int spacesAvailable = cursor.getInt(StationListFragment.COL_SPACES_AVAILABLE);
        if (bikesAvailable > 0 && spacesAvailable > 0) {
                return R.drawable.ic_green96x96;
        } else if (spacesAvailable == 0) {
                return R.drawable.ic_yellow96x96;
        } else {
                return R.drawable.ic_red96x96;
        }
    }

    public static int getMarkerIconDrawable(int bikesAvailable, int spacesAvailable) {

        if (bikesAvailable > 0 && spacesAvailable > 0) {
            return R.drawable.ic_location_green;
        } else if (spacesAvailable == 0) {
            return R.drawable.ic_location_yellow;
        } else {
            return R.drawable.ic_location_red;
        }
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

    public static int getServerStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(Utilities.SHARED_PREFS_DATA_STATUS_KEY, IloveyoubikeSyncAdapter.STATUS_SERVER_UNKNOWN);
    }

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isGooglePlayAvailable(Context context) {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (status != ConnectionResult.SUCCESS) {
            return false;
        }else return true;
    }
}
