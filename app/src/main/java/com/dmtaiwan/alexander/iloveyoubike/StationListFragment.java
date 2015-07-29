package com.dmtaiwan.alexander.iloveyoubike;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.Sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapterStation;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Alexander on 7/28/2015.
 */
public class StationListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = StationListFragment.class.getSimpleName();
    private static final int STATION_LOADER = 0;
    private RecyclerAdapterStation mAdapter;
    @InjectView(R.id.toolbar_station)
    Toolbar mToolbar;
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
            StationContract.StationEntry.COLUMN_SPACES_AVAILABLE
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


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STATION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //TODO RecyclerView & RecyclerAdapter -> ListView & CursorAdapter
        View rootView = inflater.inflate(R.layout.fragment_station_list, container, false);
        ButterKnife.inject(this, rootView);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new RecyclerAdapterStation(getActivity(), new RecyclerAdapterStation.StationAdapterOnClickHandler() {
            @Override
            public void onClick(int stationId, RecyclerAdapterStation.ViewHolder vh) {

            }
        }, mEmptyView);
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_station_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "onCreateLoader");
        String sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        Uri allStationsUri = StationContract.StationEntry.buildUriAllStations();
        return new CursorLoader(getActivity(),
                allStationsUri,
                STATION_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void updateWeather() {
        //String location = Utility.getPreferredLocation(getActivity());
        //new FetchWeatherTask(getActivity()).execute(location);
        IloveyoubikeSyncAdapter.syncImmediately(getActivity());
    }
}
