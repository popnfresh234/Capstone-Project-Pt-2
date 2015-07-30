package com.dmtaiwan.alexander.iloveyoubike;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.google.gson.Gson;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by lenovo on 7/29/2015.
 */
public class StationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = StationDetailFragment.class.getSimpleName();
    public static final int DETAIL_LOADER = 0;

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

    @InjectView(R.id.text_view_station_detail_station_name) TextView mStationName;
    @InjectView(R.id.text_view_station_detail_distance)TextView mDistance;
    @InjectView(R.id.text_view_station_detail_district)TextView mDistrict;
    @InjectView(R.id.text_view_station_detail_bikes) TextView mBikesAvailable;
    @InjectView(R.id.text_view_station_detail_spaces) TextView mSpacesAvailable;
    private int mStationId;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStationId = getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "onCreateLoader");
        String sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        Uri stationDetailUri = StationContract.StationEntry.buildUriStation(mStationId);
        return new CursorLoader(getActivity(),
                stationDetailUri,
                STATION_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String language = preferences.getString(getActivity().getString(R.string.pref_key_language), getActivity().getString(R.string.pref_language_english));

        //calculate the distasnce from the user's last known location
        double stationLat = cursor.getDouble(COL_STATION_LAT);
        double stationLong = cursor.getDouble(COL_STATION_LONG);
        float distance = calculateDistance(stationLat, stationLong);

        if(language.equals(getActivity().getString(R.string.pref_language_english))){
            mStationName.setText(cursor.getString(COL_STATION_NAME_EN));
            mDistrict.setText(cursor.getString(COL_STATION_DISTRICT_EN));
        }else{
            mStationName.setText(cursor.getString(COL_STATION_NAME_ZH));
            mDistrict.setText(cursor.getString(COL_STATION_DISTRICT_ZH));
        }

        //Set the distance if we obtain one from the user's location.  If not, set to no data
        if (distance != 0f) {
            mDistance.setText(String.valueOf(distance));
        }else{
            mDistance.setText(getActivity().getString(R.string.text_view_station_detail_no_data));
        }

        mBikesAvailable.setText(String.valueOf(cursor.getInt(COL_BIKES_AVAILABLE)));
        mSpacesAvailable.setText(String.valueOf(cursor.getInt(COL_SPACES_AVAILABLE)));
        cursor.close();
    }

    private float calculateDistance(double stationLat, double stationLong) {
        Location stationLocation = new Location("");

        stationLocation.setLatitude(stationLat);
        stationLocation.setLongitude(stationLong);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String jsonLocation = prefs.getString(Utilities.SHARED_PREFS_LOCATION_KEY, "");
        if(!jsonLocation.equals("")) {
            try {
                Gson gson = new Gson();
                Location userLocation = gson.fromJson(jsonLocation, Location.class);
                return userLocation.distanceTo(stationLocation);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0f;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
