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
import android.widget.LinearLayout;
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
    private boolean mUsingId;

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

    @InjectView(R.id.text_view_station_detail_station_name)
    TextView mStationName;
    @InjectView(R.id.text_view_station_detail_distance)
    TextView mDistance;
    @InjectView(R.id.text_view_station_detail_district)
    TextView mDistrict;
    @InjectView(R.id.text_view_station_detail_bikes)
    TextView mBikesAvailable;
    @InjectView(R.id.text_view_station_detail_spaces)
    TextView mSpacesAvailable;
    @InjectView(R.id.station_detail_container)
    LinearLayout mContainer;
    @InjectView(R.id.text_view_station_detail_empty)
    TextView mEmptyView;

    private int mStationId;
    private String mLanguage;
    private Location mUserLocation;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(mUserLocation!= null) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
        if (mUserLocation == null) {
            mContainer.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, -1) != -1) {
            mStationId = getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, -1);
            mUsingId = true;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLanguage = preferences.getString(getActivity().getString(R.string.pref_key_language), getActivity().getString(R.string.pref_language_english));


        //Fetch user location from shared prefs
        String jsonLocation = preferences.getString(Utilities.SHARED_PREFS_LOCATION_KEY, "");
        if (!jsonLocation.equals("")) {
            try {
                Gson gson = new Gson();
                mUserLocation = gson.fromJson(jsonLocation, Location.class);
            } catch (Exception e) {
                e.printStackTrace();
                mUserLocation = null;
            }
        } else {
            mUserLocation = null;
        }
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
        if(mUsingId) {
            Uri stationDetailUri = StationContract.StationEntry.buildUriStation(mStationId);
            return new CursorLoader(
                    getActivity(),
                    stationDetailUri,
                    STATION_COLUMNS,
                    null,
                    null,
                    null);
        }else{
            Uri nearestStatonUri = StationContract.StationEntry.buildUriAllStations();
            return new CursorLoader(
                    getActivity(),
                    nearestStatonUri,
                    STATION_COLUMNS,
                    null,
                    null,
                    Utilities.getSortOrderDistanceString(mUserLocation.getLatitude(), mUserLocation.getLongitude()) + "LIMIT 1"
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();



        if (mLanguage.equals(getActivity().getString(R.string.pref_language_english))) {
            mStationName.setText(cursor.getString(COL_STATION_NAME_EN));
            mDistrict.setText(cursor.getString(COL_STATION_DISTRICT_EN));
        } else {
            mStationName.setText(cursor.getString(COL_STATION_NAME_ZH));
            mDistrict.setText(cursor.getString(COL_STATION_DISTRICT_ZH));
        }

        //Set the distance if we obtain one from the user's location.  If not, set to no data
        if (mUserLocation != null) {
            //calculate the distasnce from the user's last known location
            double stationLat = cursor.getDouble(COL_STATION_LAT);
            double stationLong = cursor.getDouble(COL_STATION_LONG);
            float distance = Utilities.calculateDistance(stationLat, stationLong, mUserLocation);
            mDistance.setText(Utilities.formatDistance(distance));
        } else {
            mDistance.setText(getActivity().getString(R.string.text_view_station_detail_no_data));
        }

        mBikesAvailable.setText(String.valueOf(cursor.getInt(COL_BIKES_AVAILABLE)));
        mSpacesAvailable.setText(String.valueOf(cursor.getInt(COL_SPACES_AVAILABLE)));
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
