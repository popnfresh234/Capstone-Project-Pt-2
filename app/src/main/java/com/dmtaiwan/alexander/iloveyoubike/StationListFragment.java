package com.dmtaiwan.alexander.iloveyoubike;


import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.dmtaiwan.alexander.iloveyoubike.Utilities.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.FavoriteEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.FragmentCallback;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LanguageEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapterStation;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.dmtaiwan.alexander.iloveyoubike.sync.IloveyoubikeSyncAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Bind;


/**
 * Created by Alexander on 7/28/2015.
 */
public class StationListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FragmentCallback {
    private static final String LOG_TAG = StationListFragment.class.getSimpleName();
    private static final int STATION_LOADER = 0;
    private RecyclerAdapterStation mAdapter;


    private Boolean mSortDefaultOrder = false;
    private Boolean mIsFavorites;
    private String mLanguage;
    private ShareActionProvider mShareActionProvider;

    private int mCurrentStation = 0;

    private Parcelable mSavedRecyclerState;

    @Bind(R.id.recycler_view_station_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.station_list_empty_view)
    TextView mEmptyView;


    public static StationListFragment newInstance(int page, String title, Boolean isFavorites) {
        StationListFragment listFragment = new StationListFragment();
        Bundle args = new Bundle();
        args.putInt("pageInt", page);
        args.putString("title", title);
        args.putBoolean(Utilities.EXTRA_FAVORITES, isFavorites);
        listFragment.setArguments(args);
        return listFragment;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STATION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //Register Event Bus
        EventBus.getInstance().register(this);
        //Check if favorites list
        mIsFavorites = getArguments().getBoolean(Utilities.EXTRA_FAVORITES, false);
        //Get the language setting
        mLanguage = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_key_language), getString(R.string.pref_language_english));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_station_list, container, false);


        ButterKnife.bind(this, rootView);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);


        mAdapter = new RecyclerAdapterStation(getActivity(), mEmptyView);
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mSavedRecyclerState = savedInstanceState.getParcelable(Utilities.OUTSTATE_SCROLL_POSITION);
        }
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

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unregister
        EventBus.getInstance().unregister(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder;
        Location userLocation = Utilities.getUserLocation(getActivity());


        if (userLocation == null) {
            sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        } else if (mSortDefaultOrder) {
            sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        } else {
            sortOrder = Utilities.getSortOrderDistanceString(userLocation.getLatitude(), userLocation.getLongitude());
        }
        //Create a URI for querying all stations
        Uri allStationsUri = StationContract.StationEntry.buildUriAllStations();


        String selection = null;
        String[] selectionArgs = null;

        //If favorites view, load the favorites array and set the selection args
        if (mIsFavorites) {
            ArrayList<String> favoritesArray = Utilities.getFavoriteArray(getActivity());
            if (favoritesArray != null) {
                String[] strings = new String[favoritesArray.size()];
                selectionArgs = favoritesArray.toArray(strings);
                selection = Utilities.generateFavoritesWhereString(favoritesArray);
            } else {
                //fake args to return empty cursor and trigger empty view
                String[] fakeArgs = {"999"};
                selectionArgs = fakeArgs;
                selection = StationContract.StationEntry.COLUMN_STATION_ID + " in (?)";
            }
        }

        //Otherwise selection and

        return new CursorLoader(
                getActivity(),
                allStationsUri,
                StationContract.STATION_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //if not tablet, postpone enter transition
        if (!getResources().getBoolean(R.bool.isTablet)) {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().supportStartPostponedEnterTransition();
                    return false;
                }
            });
        }
        mAdapter.swapCursor(data);
        updateEmptyView();
        restoreLayoutPosition();

    }

    private void restoreLayoutPosition() {
        if (mSavedRecyclerState != null) {
            //Null out mSavedRecyclerState so that we don't keep restoring to this position
            mSavedRecyclerState = null;
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedRecyclerState);
        }
    }

    private void updateEmptyView() {
        if (mAdapter.getItemCount() == 0) {
            if (mEmptyView != null) {
                int emptyViewText = R.string.text_view_empty_view;
                if (mIsFavorites) {
                    mEmptyView.setText(R.string.text_view_empty_view_favorites);
                } else {
                    int status = Utilities.getServerStatus(getActivity());
                    switch (status) {
                        case IloveyoubikeSyncAdapter.STATUS_SERVER_DOWN:
                            emptyViewText = R.string.text_view_empty_view_server_down;
                            break;
                        case IloveyoubikeSyncAdapter.STATUS_SERVER_INVALID:
                            emptyViewText = R.string.text_view_empty_view_server_invalid;
                            break;
                        case IloveyoubikeSyncAdapter.STATUS_FAVORITES_EMPTY:
                            emptyViewText = R.string.text_view_empty_view_favorites;
                            break;
                        default:
                            if (!Utilities.isNetworkAvailable(getActivity())) {
                                emptyViewText = R.string.text_view_empty_view_network;
                            }

                            mEmptyView.setText(emptyViewText);
                    }
                }

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void updateStationData() {
        IloveyoubikeSyncAdapter.syncImmediately(getActivity());
    }

    public void restartLoader() {
        if (getActivity() != null) {
            getLoaderManager().restartLoader(STATION_LOADER, null, this);
        }
    }

    @Override
    public void onFragmentShown() {
        restartLoader();
    }

    public void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }

    }

    public void setStationId(int stationId) {
        mCurrentStation = stationId;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRecyclerView != null) {
            outState.putParcelable(Utilities.OUTSTATE_SCROLL_POSITION, mRecyclerView.getLayoutManager().onSaveInstanceState());
        }
    }

    //Listen for location change
    @Subscribe
    public void onLocationChange(LocationEvent locationEvent) {
        Log.i(LOG_TAG, "location changed");
        restartLoader();
    }

    //Listen for favorite change
    @Subscribe
    public void onFavoriteChange(FavoriteEvent favoriteEvent) {
        Log.i(LOG_TAG, "favorite changed");
        restartLoader();
    }

    @Subscribe
    public void onLanguageChange(LanguageEvent languageEvent) {
        restartLoader();
    }
}
