package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Intent;
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
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Alexander on 9/12/2015.
 */
public class StationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private String mTitle;
    private int mPage;


    private static final String LOG_TAG = StationDetailFragment.class.getSimpleName();
    public static final int DETAIL_LOADER = 0;
    private int mStationId;
    private boolean isFavorite;
    private ArrayList<String> mFavoritesArray;
    private String mLanguage;
    private double mStationLat;
    private double mStationLong;

    //Share provider
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

    //For Empty View
    @Bind(R.id.linear_layout_station_title)
    LinearLayout mTitleView;

    @Bind(R.id.linear_layout_station_body)
    LinearLayout mBodyView;

    @Bind(R.id.text_view_station_detail_empty)
    TextView mEmptyView;
    //End for empty view


    public static StationDetailFragment newInstance(int page, String title) {
        StationDetailFragment testFragment = new StationDetailFragment();
        Bundle args = new Bundle();
        args.putInt("pageInt", page);
        args.putString("title", title);
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
        mPage = getArguments().getInt("pageInt", 0);
        mTitle = getArguments().getString("title");
        setHasOptionsMenu(true);
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
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_station_detail_pager, container, false);
        ButterKnife.bind(this, rootView);
        showEmptyView();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unregister
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
        if (userLocation != null) {


            //If activity is MainActivity, then we want the nearest station
            if (getActivity() instanceof MainActivity) {
                Log.i(LOG_TAG, "should be here");
                Uri nearestStationUri = StationContract.StationEntry.buildUriAllStations();
                return new CursorLoader(
                        getActivity(),
                        nearestStationUri,
                        StationContract.STATION_COLUMNS,
                        null,
                        null,
                        Utilities.getSortOrderDistanceString(userLocation.getLatitude(), userLocation.getLongitude()) + "LIMIT 1"
                );
            }
            //Fragment attached to detail activity, use stationId to query
            else {
                Uri stationDetailUri = StationContract.StationEntry.buildUriStation(mStationId);
                return new CursorLoader(
                        getActivity(),
                        stationDetailUri,
                        StationContract.STATION_COLUMNS,
                        null,
                        null,
                        null);
            }
        }else{
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            Log.i(LOG_TAG, "cursor");
            cursor.moveToFirst();
            hideEmptyView();

            //Set status icon
            mStatus.setImageResource(Utilities.getStatusIconDrawable(cursor, Utilities.ICON_SIZE_LARGE));

            //Get the stationID and check for favorite
            mStationId = cursor.getInt(StationContract.COL_STATION_ID);

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
        if(mStationName!= null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    //Listen for location change
    @Subscribe
    public void onLocationChange(LocationEvent locationEvent) {
        Log.i(LOG_TAG, "location changed");
        restartLoader();
    }
}
