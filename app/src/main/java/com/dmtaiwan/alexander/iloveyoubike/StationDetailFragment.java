package com.dmtaiwan.alexander.iloveyoubike;

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
import android.support.v4.widget.Space;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
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

import com.dmtaiwan.alexander.iloveyoubike.Utilities.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.FavoriteEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LanguageEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.Data.StationContract;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Alexander on 9/12/2015.
 */
public class StationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //Constants
    private static final String LOG_TAG = StationDetailFragment.class.getSimpleName();
    public static final int DETAIL_LOADER = 0;

    private int mStationId = -1;
    private ArrayList<String> mFavoritesArray;
    private double mStationLat;
    private double mStationLong;

    //Flags
    private boolean isFavorite;
    private boolean mIsDetailActivityFragment;

    //Share provider
    private ShareActionProvider mShareActionProvider;
    private String mStationName;
    private String mBikesAvailable;
    private String mSpacesAvailable;
    private String mLastUpdate;


    @Bind(R.id.image_view_station_detail_status)
    ImageView mStatus;

    @Bind(R.id.text_view_station_detail_station_name)
    TextView mStationNameTextView;

    @Bind(R.id.text_view_station_detail_distance)
    TextView mDistanceTextView;

    @Bind(R.id.text_view_station_detail_district)
    TextView mDistrictTextView;

    @Bind(R.id.text_view_station_detail_bikes)
    TextView mBikesAvailableTextView;

    @Bind(R.id.text_view_station_detail_spaces)
    TextView mSpacesAvailableTextView;

    @Bind(R.id.station_detail_container)
    LinearLayout mStationDetailContainer;

    @Bind(R.id.linear_layout_station_title)
    LinearLayout mTitleView;

    @Bind(R.id.linear_layout_station_body)
    LinearLayout mBodyView;

    @Bind(R.id.text_view_station_detail_empty)
    TextView mEmptyView;
    //End for empty view

    @Bind(R.id.button_station_detail_favorite)
    ImageButton mFavoriteButton;

    @Bind(R.id.button_station_detail_map)
    ImageButton mMapButton;

    @Nullable
    @Bind(R.id.toolbar_detail)
    Toolbar mToolbar;

    //Spacers for Nearest Station Fragment in Tablet Mode
    @Nullable
    @Bind(R.id.grid_space_left)
    Space mGridSpaceLeft;

    @Nullable
    @Bind(R.id.grid_space_right)
    Space mGridSpaceRight;

    public static StationDetailFragment newInstance(int page, String title, Boolean isDetailActivity, Boolean isNearestStationFragment) {
        StationDetailFragment testFragment = new StationDetailFragment();
        Bundle args = new Bundle();
        args.putInt("pageInt", page);
        args.putString("title", title);
        args.putBoolean(Utilities.EXTRA_DETAIL_ACTIVITY, isDetailActivity);
        args.putBoolean(Utilities.EXTRA_IS_NEAREST_STATION_FRAGMENT, isNearestStationFragment);
        testFragment.setArguments(args);
        return testFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Register Event Bus
        EventBus.getInstance().register(this);
        mIsDetailActivityFragment = getArguments().getBoolean(Utilities.EXTRA_DETAIL_ACTIVITY);

        //If tablet mode, check for stationID
        if (getArguments() != null && getArguments().getInt(Utilities.EXTRA_STATION_ID, -1) != -1) {
            mStationId = getArguments().getInt(Utilities.EXTRA_STATION_ID);
        }

        //if not tablet or fragment in StationDetailActivity
        if (!getResources().getBoolean(R.bool.isTablet) || getActivity() instanceof StationDetailActivity) {
            setHasOptionsMenu(true);
        }

        if (getArguments().getBoolean(Utilities.EXTRA_IS_NEAREST_STATION_FRAGMENT, false)) {
            setHasOptionsMenu(true);
        }

        //Check and see if this is created from a detailActivity with an ID
        if (getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, -1) != -1) {
            mStationId = getActivity().getIntent().getIntExtra(Utilities.EXTRA_STATION_ID, -1);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_alias, container, false);
        ButterKnife.bind(this, rootView);
        showEmptyView();

        //If detail activity framgnet, setup toolbar
        if (mIsDetailActivityFragment) {
            mStationDetailContainer.setPadding(0, 0, 0, 0);
            mTitleView.setPadding(0, 0, 0, 0);
            mBodyView.setPadding(0, 0, 0, 0);
            mToolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //If detail activity fragment and tablet mode
        if (mIsDetailActivityFragment && getResources().getBoolean(R.bool.isTablet)) {
            setSpacerVisibility();
        }

        //If tablet mode and nearest station fragment
        if (getResources().getBoolean(R.bool.isTablet) && getArguments().getBoolean(Utilities.EXTRA_IS_NEAREST_STATION_FRAGMENT, false)) {
            setSpacerVisibility();
        }

        //If from widget, set home icon
        if (getActivity().getIntent()!= null && getActivity().getIntent().getBooleanExtra(Utilities.EXTRA_WIDGET, false)) {
            mMapButton.setImageResource(R.drawable.ic_home);
        }

        return rootView;
    }

    private void setSpacerVisibility() {
        mGridSpaceLeft.setVisibility(View.VISIBLE);
        mGridSpaceRight.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unregister event bus
        EventBus.getInstance().unregister(this);
    }

    private void showEmptyView() {
        mTitleView.setVisibility(View.GONE);
        mBodyView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView() {
        mTitleView.setVisibility(View.VISIBLE);
        mBodyView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Fetch user location
        Location userLocation = Utilities.getUserLocation(getActivity());

        if (mStationId != -1) {
            Uri stationDetailUri = StationContract.StationEntry.buildUriStation(mStationId);
            return new CursorLoader(
                    getActivity(),
                    stationDetailUri,
                    StationContract.STATION_COLUMNS,
                    null,
                    null,
                    null);
        } else if (userLocation != null) {
            //Coming from Nearest Station, query for nearest station
            Uri nearestStatonUri = StationContract.StationEntry.buildUriAllStations();
            return new CursorLoader(
                    getActivity(),
                    nearestStatonUri,
                    StationContract.STATION_COLUMNS,
                    null,
                    null,
                    Utilities.getSortOrderDistanceString(userLocation.getLatitude(), userLocation.getLongitude()) + "LIMIT 1"
            );
        } else {
            String sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
            Uri allStationUri = StationContract.StationEntry.buildUriAllStations();
            return new CursorLoader(
                    getActivity(),
                    allStationUri,
                    StationContract.STATION_COLUMNS,
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
            hideEmptyView();

            //Start postponed transition
            getActivity().supportStartPostponedEnterTransition();

            //Set status icon
            mStatus.setImageResource(Utilities.getStatusIconDrawable(cursor, Utilities.ICON_SIZE_LARGE));

            //Get the stationID
            mStationId = cursor.getInt(StationContract.COL_STATION_ID);

            //Get the list of favorite stations from SharedPrefs
            mFavoritesArray = Utilities.getFavoriteArray(getActivity());
            //If a list of favorites has been stored in SharedPrefs
            if (mFavoritesArray != null) {
                //Set the favorite button and flag
                if (mFavoritesArray.contains(String.valueOf(mStationId))) {
                    mFavoriteButton.setImageResource(R.drawable.ic_favorite_black_48dp);
                    isFavorite = true;
                }
                //Button already un-favorite state by default, set flag
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
                mStationName = cursor.getString(StationContract.COL_STATION_NAME_EN);
                mStationNameTextView.setText(mStationName);
                mDistrictTextView.setText(cursor.getString(StationContract.COL_STATION_DISTRICT_EN));
                //If pinyin
            } else if (language.equals(getActivity().getString(R.string.pref_language_pinyin))) {
                int stringId = getResources().getIdentifier("station" + String.valueOf(mStationId), "string", getActivity().getPackageName());
                String stationName = getString(stringId);
                mStationNameTextView.setText(stationName);
                mDistrictTextView.setText(cursor.getString(StationContract.COL_STATION_DISTRICT_EN));
            } else {
                mStationName = cursor.getString(StationContract.COL_STATION_NAME_ZH);
                mStationNameTextView.setText(mStationName);
                mDistrictTextView.setText(cursor.getString(StationContract.COL_STATION_DISTRICT_ZH));
            }

            //Set the distance if we obtain one from the user's location.  If not, set to no data
            Location userLocation = Utilities.getUserLocation(getActivity());
            mStationLat = cursor.getDouble(StationContract.COL_STATION_LAT);
            mStationLong = cursor.getDouble(StationContract.COL_STATION_LONG);
            if (userLocation != null) {
                //calculate the distance from the user's last known location
                float distance = Utilities.calculateDistance(mStationLat, mStationLong, userLocation);
                mDistanceTextView.setText(Utilities.formatDistance(distance));
            } else {
                mDistanceTextView.setText(getActivity().getString(R.string.text_view_station_detail_no_data));
            }

            mBikesAvailable = String.valueOf(cursor.getInt(StationContract.COL_BIKES_AVAILABLE));
            mBikesAvailableTextView.setText(mBikesAvailable);

            mSpacesAvailable = String.valueOf(cursor.getInt(StationContract.COL_SPACES_AVAILABLE));
            mSpacesAvailableTextView.setText(mSpacesAvailable);

            mLastUpdate = Utilities.formatTime(cursor.getString(StationContract.COL_LAST_UPDATED));


        }

        //Set the share intent
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }

        //if tablet, set stationList shareIntent
        if (getResources().getBoolean(R.bool.isTablet) && getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).passShareIntentToFragment(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void restartLoader() {
        if (getActivity() != null) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    public Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String shareText = Utilities.buildShareText(getActivity(), mBikesAvailable, mSpacesAvailable, mStationName, mLastUpdate);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        return shareIntent;
    }

    public void setShareIntent() {
        if (mStationName != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
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

            //If this is the first station favorited, create array of favorites and store in shared prefs
            if (mFavoritesArray == null) {
                mFavoritesArray = new ArrayList<String>();
                mFavoritesArray.add(String.valueOf(mStationId));

            }
            //Otherwise adding to the list of favorites
            else {
                mFavoritesArray.add(String.valueOf(mStationId));
            }
        }
        //If it is already a favorite station
        else if (isFavorite) {
            isFavorite = false;
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_outline_grey600_48dp);
            int index = mFavoritesArray.indexOf(String.valueOf(mStationId));
            mFavoritesArray.remove(index);
        }
        //Convert array to json string and store in shared prefs
        spe.putString(Utilities.SHARED_PREFS_FAVORITE_KEY, gson.toJson(mFavoritesArray));
        spe.apply();
        FavoriteEvent favoriteEvent = new FavoriteEvent();
        favoriteEvent.setStationId(mStationId);
        EventBus.getInstance().post(favoriteEvent);
    }

    @OnClick(R.id.button_station_detail_map)
    public void onMapButtonCLicked() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).gotoMap(new LatLng(mStationLat, mStationLong), mStationId);
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle args = new Bundle();
            args.putParcelable(Utilities.EXTRA_LATLNG, new LatLng(mStationLat, mStationLong));
            args.putInt(Utilities.EXTRA_STATION_ID, mStationId);
            intent.putExtra(Utilities.EXTRA_LATLNG, args);
            startActivity(intent);
            if (getActivity().getIntent() != null && getActivity().getIntent().getBooleanExtra(Utilities.EXTRA_WIDGET, false)) {
                getActivity().finish();
            }
        }
    }

    //Listen for location change
    @Subscribe
    public void onLocationChange(LocationEvent locationEvent) {
        restartLoader();
    }

    @Subscribe
    public void onLanguageChange(LanguageEvent languageEvent) {
        restartLoader();
    }

    //Listen for favorite change
    @Subscribe
    public void onFavoriteChange(FavoriteEvent favoriteEvent) {
        checkFavorite();
        restartLoader();
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
}
