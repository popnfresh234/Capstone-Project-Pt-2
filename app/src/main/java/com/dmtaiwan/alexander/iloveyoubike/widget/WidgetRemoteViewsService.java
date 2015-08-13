package com.dmtaiwan.alexander.iloveyoubike.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.StationListFragment;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;

/**
 * Created by Alexander on 8/13/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();

    private static final String[] STATION_COLUMNS = {
            StationContract.StationEntry._ID,
            StationContract.StationEntry.COLUMN_STATION_ID,
            StationContract.StationEntry.COLUMN_STATION_NAME_ZH,
            StationContract.StationEntry.COLUMN_STATION_DISTRICT_ZH,
            StationContract.StationEntry.COLUMN_STATION_NAME_EN,
            StationContract.StationEntry.COLUMN_STATION_DISTRICT_EN,
            StationContract.StationEntry.COLUMN_STATION_LAT,
            StationContract.StationEntry.COLUMN_STATION_LONG,
            StationContract.StationEntry.COLUMN_BIKES_AVAIABLE,
            StationContract.StationEntry.COLUMN_SPACES_AVAILABLE,
            StationContract.StationEntry.COLUMN_LAST_UPDATED
    };

    public static final int COL_ID = 0;
    public static final int COL_STATION_ID = 1;
    public static final int COL_STATION_NAME_ZH = 2;
    public static final int COL_STATION_DISTRICT_ZH = 3;
    public static final int COL_STATION_NAME_EN = 4;
    public static final int COL_STATION_DISTRICT_EN = 5;
    public static final int COL_STATION_LAT = 6;
    public static final int COL_STATION_LONG = 7;
    public static final int COL_BIKES_AVAILABLE = 8;
    public static final int COL_SPACES_AVAILABLE = 9;
    public static final int COL_LAST_UPDATED = 10;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                String sortOrder;
                Location userLocation = Utilities.getUserLocation(getApplicationContext());


                if (userLocation == null) {
                    sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
                } else {
                    sortOrder = Utilities.getSortOrderDistanceString(userLocation.getLatitude(), userLocation.getLongitude());
                }
                //Create a URI for querying all stations
                Uri allStationsUri = StationContract.StationEntry.buildUriAllStations();



                data = getContentResolver().query(
                        allStationsUri,
                        STATION_COLUMNS,
                        null,
                        null,
                        sortOrder);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.i(LOG_TAG, String.valueOf(data.getCount()));
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    Log.i(LOG_TAG, "returning null");
                    return null;

                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_view_item_widget);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                //Get preferred language
                String language = prefs.getString(getString(R.string.pref_key_language), getString(R.string.pref_language_english));

                //Get station name
                String stationName;
                if (language.equals(getString(R.string.pref_language_chinese))) {
                    stationName = data.getString(COL_STATION_NAME_ZH);
                } else {
                    stationName = data.getString(COL_STATION_NAME_EN);
                }

                //Get last updated time
                String time = Utilities.formatTime(data.getString(StationListFragment.COL_LAST_UPDATED));

                //Get distance
                String distanceString = "";
                double stationLat = data.getDouble(StationListFragment.COL_STATION_LAT);
                double stationLong = data.getDouble(StationListFragment.COL_STATION_LONG);

                Location userLocation = Utilities.getUserLocation(getApplicationContext());

                if (userLocation != null) {
                    float distance = Utilities.calculateDistance(stationLat, stationLong, userLocation);
                    distanceString = Utilities.formatDistance(distance);
                }



                //Set views
                views.setTextViewText(R.id.text_view_station_list_station_name, stationName);
                views.setTextViewText(R.id.text_view_station_list_time, time);
                views.setTextViewText(R.id.text_view_station_list_distance, distanceString);
                views.setImageViewResource(R.id.image_view_station_list_status, Utilities.getStatusIconDrawable(data, Utilities.ICON_SIZE_SMALL));


//                final Intent fillInIntent = new Intent();
//                String locationSetting =
//                        Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
//                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                        locationSetting,
//                        dateInMillis);
//                fillInIntent.setData(weatherUri);
//                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
//                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_view_item_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}