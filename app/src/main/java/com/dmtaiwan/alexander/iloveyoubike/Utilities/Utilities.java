package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.dmtaiwan.alexander.iloveyoubike.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
 * Created by Alexander on 10/18/2015.
 */
public class Utilities {
    public static final String FILE_NAME = "youbikeData.json";
    public static final Double TAIPEI_LAT = 25.033611;
    public static final Double TAIPEI_LONG = 121.565;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    //Fragment tags
    public static final String FRAGMENT_TAG_MAIN = "main_fragment";
    public static final String FRAGMENT_TAG_DETAIL = "detail_fragment";

    public static final int ICON_SIZE_SMALL = 0;
    public static final int ICON_SIZE_LARGE = 1;

    //Sort codes
    public static final int SORT_DEFAULT =2222;
    public static final int SORT_PROXIMITY = 3333;

    //Extra codes
    public static final String EXTRA_STATION = "com.dmtaiwan.extra.station";
    public static final String EXTRA_OUTSTATE_LATLNG = "com.dmtaiwan.extra.outstatelatlng";
    public static final String EXTRA_STATION_ID = "com.dmtaiwan.extra.stationid";

    //Constants for shared prefs
    public static final String SHARED_PREFS_LOCATION_LAT_KEY = "com.dmtaiwan.alexander.key.location.lat";
    public static final String SHARED_PREFS_LOCATION_LONG_KEY = "com.dmtaiwan.alexander.key.location.long";
    public static final String SHARED_PREFS_FAVORITE_KEY = "com.dmtaiwan.alexander.key.favorite";
    public static final String SHARED_PREFS_DATA_STATUS_KEY = "com.dmtaiwan.alexander.key.data";
    public static final String SHARED_PREFS_SORT_KEY = "com.dmtaiwan.alexander.key.sort";

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    static public boolean doesFileExist(Context context) {
        File file = context.getFileStreamPath(FILE_NAME);
        return file.exists();
    }

    public static String readFromFile(Context context) {
        String json = "";
        try {
            InputStream inputStream = context.openFileInput(Utilities.FILE_NAME);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                json = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static void writeToFile(String json, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(Utilities.FILE_NAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(json);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String formatTime(String string) {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        String timeString = "";
        try {
            Date date = format.parse(string);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String hourString;

            if (hour < 10) {
                hourString = "0" + String.valueOf(hour);
            } else {
                hourString = String.valueOf(hour);
            }
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

    public static float calculateDistance(double targetLat, double targetLong, Location userLocation) {
        Location stationLocation = new Location("");

        stationLocation.setLatitude(targetLat);
        stationLocation.setLongitude(targetLong);

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

    private static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }

    public static void setUserLocation(Location location, Context context) {
        if (location != null) {
            SharedPreferences settings;
            SharedPreferences.Editor spe;
            double lat = location.getLatitude();
            double longitude = location.getLongitude();
            settings = PreferenceManager.getDefaultSharedPreferences(context);
            spe = settings.edit();
            spe.putLong(SHARED_PREFS_LOCATION_LAT_KEY, Double.doubleToRawLongBits(lat));
            spe.putLong(SHARED_PREFS_LOCATION_LONG_KEY, Double.doubleToRawLongBits(longitude));
            spe.apply();
        }
    }

    public static Location getUserLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        double lat = Double.longBitsToDouble(prefs.getLong(Utilities.SHARED_PREFS_LOCATION_LAT_KEY, 0));
        double longitude = Double.longBitsToDouble(prefs.getLong(Utilities.SHARED_PREFS_LOCATION_LONG_KEY, 0));
        //If activity_station_detai; location has been stored in shared prefs, retrieve it and set the lat/long coordinates for the query
        if (lat != 0 && longitude != 0) {
            Location userLocation = new Location("newLocation");
            userLocation.setLatitude(lat);
            userLocation.setLongitude(longitude);
            return userLocation;
        } else {
            return null;
        }
    }

    public static void setSortOrder(int sortKey, Context context) {
        SharedPreferences settings;
        SharedPreferences.Editor spe;
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        spe = settings.edit();
        spe.putInt(SHARED_PREFS_SORT_KEY, sortKey);
        spe.apply();
    }

    public static int getSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int sortKey = prefs.getInt(Utilities.SHARED_PREFS_SORT_KEY, SORT_DEFAULT);
        return sortKey;
    }


    public static int getStatusIconDrawable(int bikesAvailable, int spacesAvailable, int size) {
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

    public static String buildShareText(Context context, String bikesAvailable, String spacesAvailable, String stationName, String lastUpdate) {
        String shareText = context.getResources().getString(R.string.shareTextOne) + " " + bikesAvailable + " "
                + context.getResources().getString(R.string.shareTextTwo) + " " + spacesAvailable + " "
                + context.getResources().getString(R.string.shareTextThree) + " " + stationName + " "
                + context.getString(R.string.shareTextFour) + " " + lastUpdate;
        return shareText;
    }

    public static ArrayList<String> getFavoriteArray(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
}


