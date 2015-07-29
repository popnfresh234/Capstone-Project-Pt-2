package com.dmtaiwan.alexander.iloveyoubike.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Alexander on 7/28/2015.
 */
public class StationContract {

    public static final String CONTENT_AUTHORITY = "com.dmtaiwan.alexander.iloveyoubike";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_STATION = "station";



    public static final class StationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATION).build();

        public static final Uri CONTENT_URI_TEST =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATION).appendPath("2").build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATION;

        public static final String TABLE_NAME = "station";

        public static final String COLUMN_STATION_ID = "station_id";

        //Station name in Chinese
        public static final String COLUMN_STATION_NAME_ZH = "station_name_zh";

        //Station district in Chinese
        public static final String COLUMN_STATION_DISTRICT_ZH = "station_district_zh";

        //Station name in English
        public static final String COLUMN_STATION_NAME_EN = "station_name_en";

        public static final String COLUMN_STATION_DISTRICT_EN = "station_district_en";

        public static final String COLUMN_STATION_LAT = "station_lat";

        public static final String COLUMN_STATION_LONG = "station_long";

        public static final String COLUMN_BIKES_AVAIABLE = "bikes_available";

        public static final String COLUMN_SPACES_AVAILABLE = "spaces_available";

        public static Uri buildUriStation(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUriAllStations (){
            return CONTENT_URI;
        }

        public static Uri buildUriStationWithId(int stationID) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(stationID)).build();
        }

        public static int getIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

    }
}
