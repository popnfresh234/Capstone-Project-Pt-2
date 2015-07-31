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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

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
    @InjectView(R.id.button_station_detail_favorite)
    ImageButton mFavoriteButton;

    private int mStationId;
    private String mLanguage;
    private Location mUserLocation;
    private SharedPreferences mSharedPrefs;
    private boolean isFavorite;
    private ArrayList<String> mFavoritesArray;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
//        if (mUserLocation != null) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
//        }
//        if (mUserLocation == null) {
//            mContainer.setVisibility(View.GONE);
//            mEmptyView.setVisibility(View.VISIBLE);
//        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, -1) != -1) {
            mStationId = getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, -1);
            mUsingId = true; //Set flag showing that ID is passed with intent
        }

        //Fetch language preference
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLanguage = mSharedPrefs.getString(getActivity().getString(R.string.pref_key_language), getActivity().getString(R.string.pref_language_english));

        //Fetch user location from shared prefs
        mUserLocation = Utilities.getUserLocation(mSharedPrefs);
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

        //If started from list of stations, ID passed in with intent, query for specific station
        if (mUsingId) {
            Uri stationDetailUri = StationContract.StationEntry.buildUriStation(mStationId);
            return new CursorLoader(
                    getActivity(),
                    stationDetailUri,
                    STATION_COLUMNS,
                    null,
                    null,
                    null);
        } else {
            //Coming from Nearest Station, query for nearest station
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

        //Get the stationID and check for favorite
        mStationId = cursor.getInt(COL_STATION_ID);

        //Get the list of favorite stations from SharedPrefs
        mFavoritesArray = Utilities.getFavoriteArray(mSharedPrefs);

        //If a list of favorites has been stored in SharedPrefs
        if (mFavoritesArray != null) {

            //Set the favorite button and flag
            if (mFavoritesArray.contains(String.valueOf(mStationId))) {
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_black_48dp);
                isFavorite = true;
            }
            //Button already unfavorite state by default, set flag
            else {
                isFavorite = false;
            }
        }
        //If mFavoritesArray == null, no favorites have been stored, flag not favorite
        else {
            isFavorite = false;
        }

        if (mLanguage.equals(getActivity().getString(R.string.pref_language_english))) {
            mStationName.setText(cursor.getString(COL_STATION_NAME_EN));
            mDistrict.setText(cursor.getString(COL_STATION_DISTRICT_EN));
        } else {
            mStationName.setText(cursor.getString(COL_STATION_NAME_ZH));
            mDistrict.setText(cursor.getString(COL_STATION_DISTRICT_ZH));
        }

        //Set the distance if we obtain one from the user's location.  If not, set to no data
        if (mUserLocation != null) {
            //calculate the distance from the user's last known location
            double stationLat = cursor.getDouble(COL_STATION_LAT);
            double stationLong = cursor.getDouble(COL_STATION_LONG);
            float distance = Utilities.calculateDistance(stationLat, stationLong, mUserLocation);
            mDistance.setText(Utilities.formatDistance(distance));
        } else {
            mDistance.setText(getActivity().getString(R.string.text_view_station_detail_no_data));
        }

        mBikesAvailable.setText(String.valueOf(cursor.getInt(COL_BIKES_AVAILABLE)));
        mSpacesAvailable.setText(String.valueOf(cursor.getInt(COL_SPACES_AVAILABLE)));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @OnClick(R.id.button_station_detail_favorite)
    public void onFavoriteClicked() {
        SharedPreferences.Editor spe = mSharedPrefs.edit();
        Gson gson = new Gson();

        //If not a favorite station
        if (!isFavorite) {
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_black_48dp);
            isFavorite = true;

            //If this is the first station favorited
            if (mFavoritesArray == null) {
                mFavoritesArray = new ArrayList<String>();
                mFavoritesArray.add(String.valueOf(mStationId));
                Log.i(LOG_TAG, mFavoritesArray.toString());
                spe.putString(Utilities.SHARED_PREFS_FAVORITE_KEY, gson.toJson(mFavoritesArray));
                spe.commit();
            }
            //Otherwise adding to the list of favorites
            else {
                mFavoritesArray.add(String.valueOf(mStationId));
                spe.putString(Utilities.SHARED_PREFS_FAVORITE_KEY, gson.toJson(mFavoritesArray));
                spe.commit();
                Log.i(LOG_TAG, mFavoritesArray.toString());

            }
        } else if (isFavorite) {
            isFavorite = false;
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_outline_grey600_48dp);
            int index = mFavoritesArray.indexOf(String.valueOf(mStationId));
            mFavoritesArray.remove(index);
            Log.i(LOG_TAG, mFavoritesArray.toString());
            spe.putString(Utilities.SHARED_PREFS_FAVORITE_KEY, gson.toJson(mFavoritesArray));
            spe.commit();
        }
    }
}
