package com.dmtaiwan.alexander.iloveyoubike;


import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.Sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapterStation;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Alexander on 7/28/2015.
 */
public class StationListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, LocationProvider.LocationCallback {
    private static final String LOG_TAG = StationListFragment.class.getSimpleName();
    private static final int STATION_LOADER = 0;
    private RecyclerAdapterStation mAdapter;
    private Boolean mIsFavorites = false;
    private Boolean mSortDefaultOrder = false;
    private int mScrollPosition;
    private LocationProvider mLocationProvider;

    @InjectView(R.id.recycler_view_station_list)
    RecyclerView mRecyclerView;
    @InjectView(R.id.station_list_empty_view)
    TextView mEmptyView;

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



    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(int stationId, RecyclerAdapterStation.ViewHolder vh);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getActivity().supportPostponeEnterTransition();
        getLoaderManager().initLoader(STATION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //Check if this is a favorites list
        mIsFavorites = getActivity().getIntent().getBooleanExtra(Utilities.EXTRA_FAVORITES, false);

        //Create a location provider to update position
        mLocationProvider = new LocationProvider(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_station_list, container, false);
        ButterKnife.inject(this, rootView);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new RecyclerAdapterStation(getActivity(), new RecyclerAdapterStation.StationAdapterOnClickHandler() {
            @Override
            public void onClick(int stationId, RecyclerAdapterStation.ViewHolder vh) {
                ((Callback) getActivity()).onItemSelected(stationId, vh);
            }
        }, mEmptyView);
        mRecyclerView.setAdapter(mAdapter);

        //Returning to fragment
        if (savedInstanceState != null) {
            mScrollPosition = savedInstanceState.getInt(Utilities.OUTSTATE_SCROLL_POSITION);
            mRecyclerView.smoothScrollToPosition(mScrollPosition);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Connect to play services
        mLocationProvider.connect();
        //Restart the loader in case items were unfavorited
        restartLoader();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_station_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateStationData();
            return true;
        }
        if (id == R.id.action_sort_default) {
            mSortDefaultOrder = true;
            restartLoader();
        }

        if (id == R.id.action_sort_proximity) {
            mSortDefaultOrder = false;
            restartLoader();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            mScrollPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            outState.putInt(Utilities.OUTSTATE_SCROLL_POSITION, mScrollPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder;

        Location location = Utilities.getUserLocation(getActivity());

        if (location == null) {
            sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        } else if (mSortDefaultOrder) {
            sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        } else {
            sortOrder = Utilities.getSortOrderDistanceString(location.getLatitude(), location.getLongitude());
        }

        //Create a URI for querying all stations
        Uri allStationsUri = StationContract.StationEntry.buildUriAllStations();


        String selection = null;
        String[] selectionArgs = null;
        if (mIsFavorites) {
            ArrayList<String> favoritesArray = Utilities.getFavoriteArray(getActivity());
            if (favoritesArray != null) {
                String[] strings = new String[favoritesArray.size()];
                selectionArgs = favoritesArray.toArray(strings);
                selection = Utilities.generateFavoritesWhereString(favoritesArray);
            }
        }

        //Otherwise view all stations

        return new CursorLoader(
                getActivity(),
                allStationsUri,
                STATION_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        updateEmptyView();
        scheduleStartPostponedTransition(mRecyclerView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void updateStationData() {
        IloveyoubikeSyncAdapter.syncImmediately(getActivity());
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(STATION_LOADER, null, this);
    }

    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        getActivity().supportStartPostponedEnterTransition();
                        return true;
                    }
                });
    }

    private void updateEmptyView() {
        if (mAdapter.getItemCount() == 0) {
            if (mEmptyView != null) {

                int emptyViewText = R.string.text_view_empty_view;
                int status = Utilities.getServerStatus(getActivity());
                switch (status) {
                    case IloveyoubikeSyncAdapter.STATUS_SERVER_DOWN:
                        emptyViewText = R.string.text_view_empty_view_server_down;
                        break;
                    case IloveyoubikeSyncAdapter.STATUS_SERVER_INVALID:
                        emptyViewText = R.string.text_view_empty_view_server_invalid;
                        break;
                    default:
                        if (!Utilities.isNetworkAvailable(getActivity())) {
                            emptyViewText = R.string.text_view_empty_view_network;
                        }
                }
                mEmptyView.setText(emptyViewText);
            }
        }
    }

    @Override
    public void handleNewLocation(Location location) {
        Utilities.setUserLocation(location, getActivity());
        restartLoader();

    }

}
