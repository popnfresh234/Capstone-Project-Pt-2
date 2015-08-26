package com.dmtaiwan.alexander.iloveyoubike;

import android.app.Activity;
import android.content.Intent;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.FragmentCallback;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by lenovo on 7/29/2015.
 */
public class StationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, LocationProvider.LocationCallback, FragmentCallback {

    private static final String LOG_TAG = StationDetailFragment.class.getSimpleName();
    public static final int DETAIL_LOADER = 0;
    private boolean mUsingId;
    private int mStationId;
    private boolean isFavorite;
    private ArrayList<String> mFavoritesArray;
    private OnFavoriteListener mCallback;
    private Boolean mIsTablet = false;
    private String mLanguage;
    private Boolean mIsFromDetailActivity = false;

    private double mStationLat;
    private double mStationLong;

    //Share provider
    private ShareActionProvider mShareActionProvider;
    private String mStationName;
    private String mBikesAvailable;
    private String mSpacesAvailable;
    private String mLastUpdate;

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

    @InjectView(R.id.image_view_station_detail_status)
    ImageView mStatus;
    @InjectView(R.id.text_view_station_detail_station_name)
    TextView mStationNameTextView;
    @InjectView(R.id.text_view_station_detail_distance)
    TextView mDistanceTextView;
    @InjectView(R.id.text_view_station_detail_district)
    TextView mDistrictTextView;
    @InjectView(R.id.text_view_station_detail_bikes)
    TextView mBikesAvailableTextView;
    @InjectView(R.id.text_view_station_detail_spaces)
    TextView mSpacesAvailableTextView;
    @InjectView(R.id.station_detail_container)
    LinearLayout mContainer;
    @InjectView(R.id.text_view_station_detail_empty)
    TextView mEmptyView;
    @InjectView(R.id.button_station_detail_favorite)
    ImageButton mFavoriteButton;
    @InjectView(R.id.button_station_detail_map)
    ImageButton mMapButton;

    @Optional
    @InjectView(R.id.toolbar_detail)
    Toolbar mToolbar;


    public interface OnFavoriteListener {
        public void onFavorited(int stationId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = (OnFavoriteListener) activity;


    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check if tablet
        mIsTablet = getResources().getBoolean(R.bool.isTablet);

        //Check if creating a detail fragment for a specific station
        if (getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, -1) != -1) {
            mStationId = getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, -1);
            mUsingId = true; //Set flag showing that ID is passed with intent
        }

        //Check if creating a detail fragment for a specific station
        if (getArguments() != null && getArguments().getInt(Utilities.EXTRA_STATION_ID, -1) != -1) {
            mStationId = getArguments().getInt(Utilities.EXTRA_STATION_ID);
            mUsingId = true;
        }

        if (!mIsTablet) {
            setHasOptionsMenu(true);
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).checkFragmentForNearest();
        }


        //Get the preferred language
        mLanguage = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_key_language), getString(R.string.pref_language_english));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (mIsFromDetailActivity) {
            rootView = inflater.inflate(R.layout.fragment_detail_alias, container, false);

        } else rootView = inflater.inflate(R.layout.fragment_detail_pager_alias, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Fetch the current language
        String language = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_key_language), getString(R.string.pref_language_english));

        //Check if it has changed since the fragment was created
        if (!language.equals(mLanguage)) {
            //Set the language to current language and restart the loader
            mLanguage = language;
            restartLoader();
        }

        if (mIsFromDetailActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(LOG_TAG, "Station Detail onCreatOptions");
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

       setShareIntent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        Location userLocation = Utilities.getUserLocation(getActivity());

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
        } else if (userLocation != null) {
            //Coming from Nearest Station, query for nearest station
            Uri nearestStatonUri = StationContract.StationEntry.buildUriAllStations();
            return new CursorLoader(
                    getActivity(),
                    nearestStatonUri,
                    STATION_COLUMNS,
                    null,
                    null,
                    Utilities.getSortOrderDistanceString(userLocation.getLatitude(), userLocation.getLongitude()) + "LIMIT 1"
            );
        } else {
            Uri allStationUri = StationContract.StationEntry.buildUriAllStations();
            return new CursorLoader(
                    getActivity(),
                    allStationUri,
                    STATION_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            //Set status icon
            mStatus.setImageResource(Utilities.getStatusIconDrawable(cursor, Utilities.ICON_SIZE_LARGE));

            //Get the stationID and check for favorite
            mStationId = cursor.getInt(COL_STATION_ID);

            //Get the list of favorite stations from SharedPrefs
            mFavoritesArray = Utilities.getFavoriteArray(getActivity());

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

            //Get the preferred language
            String language = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(getActivity().getString(R.string.pref_key_language), getActivity().getString(R.string.pref_language_english));

            if (language.equals(getActivity().getString(R.string.pref_language_english))) {
                mStationName = cursor.getString(COL_STATION_NAME_EN);
                mStationNameTextView.setText(mStationName);
                mDistrictTextView.setText(cursor.getString(COL_STATION_DISTRICT_EN));
            } else {
                mStationName = cursor.getString(COL_STATION_NAME_ZH);
                mStationNameTextView.setText(mStationName);
                mDistrictTextView.setText(cursor.getString(COL_STATION_DISTRICT_ZH));
            }

            //Set the distance if we obtain one from the user's location.  If not, set to no data
            Location userLocation = Utilities.getUserLocation(getActivity());
            if (userLocation != null) {
                //calculate the distance from the user's last known location
                mStationLat = cursor.getDouble(COL_STATION_LAT);
                mStationLong = cursor.getDouble(COL_STATION_LONG);
                float distance = Utilities.calculateDistance(mStationLat, mStationLong, userLocation);
                mDistanceTextView.setText(Utilities.formatDistance(distance));
            } else {
                mDistanceTextView.setText(getActivity().getString(R.string.text_view_station_detail_no_data));
            }

            mBikesAvailable = String.valueOf(cursor.getInt(COL_BIKES_AVAILABLE));
            mBikesAvailableTextView.setText(mBikesAvailable);

            mSpacesAvailable = String.valueOf(cursor.getInt(COL_SPACES_AVAILABLE));
            mSpacesAvailableTextView.setText(mSpacesAvailable);

            mLastUpdate = Utilities.formatTime(cursor.getString(StationListFragment.COL_LAST_UPDATED));
        }


        //Set the share intent
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }

        //if tablet, set stationList shareIntent
        if (mIsTablet && getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).passShareIntentToFragment(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @OnClick(R.id.button_station_detail_favorite)
    public void onFavoriteClicked() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor spe = sharedPrefs.edit();
        Gson gson = new Gson();

        //If not a favorite station
        if (!isFavorite) {
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_black_48dp);
            isFavorite = true;

            //If this is the first station favorited
            if (mFavoritesArray == null) {
                mFavoritesArray = new ArrayList<String>();
                mFavoritesArray.add(String.valueOf(mStationId));
                //Convert to json and store in shared prefs
                spe.putString(Utilities.SHARED_PREFS_FAVORITE_KEY, gson.toJson(mFavoritesArray));
                spe.commit();
            }
            //Otherwise adding to the list of favorites
            else {
                mFavoritesArray.add(String.valueOf(mStationId));
                spe.putString(Utilities.SHARED_PREFS_FAVORITE_KEY, gson.toJson(mFavoritesArray));
                spe.commit();
            }
            //If in tablet mode, call back to activity to update the list of stations
            if (mCallback != null)
                mCallback.onFavorited(mStationId);
        }
        //If it is already a favorite station
        else if (isFavorite) {
            isFavorite = false;
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_outline_grey600_48dp);
            int index = mFavoritesArray.indexOf(String.valueOf(mStationId));
            mFavoritesArray.remove(index);
            //Convert array to json string and store in shared prefs
            spe.putString(Utilities.SHARED_PREFS_FAVORITE_KEY, gson.toJson(mFavoritesArray));
            spe.commit();
            //Call back to activity to notify list of stations
            if (mCallback != null)
                mCallback.onFavorited(mStationId);
        }
    }

    @OnClick(R.id.button_station_detail_map)
    public void onMapButtonCLicked() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).gotoMap(new LatLng(mStationLat, mStationLong));
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle args = new Bundle();
            args.putParcelable(Utilities.EXTRA_LATLNG, new LatLng(mStationLat, mStationLong));
            intent.putExtra(Utilities.EXTRA_LATLNG, args);
            startActivity(intent);
        }
    }


    @Override
    public void handleNewLocation(Location location) {
        Utilities.setUserLocation(location, getActivity());
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    private void checkFavorite() {
        //In case the nearest station was unfavorited from favorite or all station list
        //Check if station is contained in the list of favorites, if so make sure button is in the proper state
        ArrayList<String> favoritesArray = Utilities.getFavoriteArray(getActivity());
        if (favoritesArray != null && favoritesArray.contains(String.valueOf(mStationId))) {
            isFavorite = true;
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_black_48dp);
        } else {
            isFavorite = false;
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_outline_grey600_48dp);
        }
    }

    @Override
    public void onFragmentShown() {
        restartLoader();
        checkFavorite();
    }

    public Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
//        String shareText = "There are " + mBikesAvailable + " bikes available and " + mSpacesAvailable + " spaces available at " + mStationName + " station as of " + mLastUpdate;
        String shareText = Utilities.buildShareText(getActivity(), mBikesAvailable, mSpacesAvailable, mStationName, mLastUpdate);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        return shareIntent;
    }

    public void setShareIntent() {
        if(mStationName!= null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

}
