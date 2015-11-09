package com.dmtaiwan.alexander.iloveyoubike.Views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.Bus.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Bus.FavoritesEvent;
import com.dmtaiwan.alexander.iloveyoubike.Bus.SortEvent;
import com.dmtaiwan.alexander.iloveyoubike.Bus.StationListEvent;
import com.dmtaiwan.alexander.iloveyoubike.Models.Station;
import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Alexander on 10/22/2015.
 */
public class FavoritesFragment extends Fragment {

    private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();


    private RecyclerAdapter mAdapter;
    private ArrayList<String> mFavoritesArray;

    @Bind(R.id.empty_view)
    TextView mEmptyView;

    @Bind(R.id.recycler_view_station_list)
    RecyclerView mRecyclerView;

    @Bind(R.id.detail_container)
    FrameLayout mDetailContainer;


    public static FavoritesFragment newInstance() {
        FavoritesFragment favoritesFragment = new FavoritesFragment();
        return favoritesFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFavoritesArray = Utilities.getFavoriteArray(getActivity());
        EventBus.getInstance().register(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, rootView);

        //Set layout manager
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        //Set adapter
        mAdapter = new RecyclerAdapter(getActivity(), mEmptyView);
        mRecyclerView.setAdapter(mAdapter);

        //Set container visibility if tablet mode
        if (getResources().getBoolean(R.bool.isTablet)) {
            mDetailContainer.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fillAdapter(((MainActivity) getActivity()).getStationList());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unregister(this);
    }

    public void fillAdapter(List<Station> stationList) {
        List<Station> favoriteStations = new ArrayList<Station>();
        if (stationList != null && mFavoritesArray != null && mAdapter != null) {

            for (String id : mFavoritesArray) {
                for (int i = 0; i < stationList.size(); i++) {
                    Station station = stationList.get(i);
                    if (String.valueOf(station.getId()).equals(id)) {
                        favoriteStations.add(station);
                    }
                }
            }
            mAdapter.updateData(favoriteStations);


        } else if (mAdapter != null) {
            mAdapter.setEmptyView();
            mEmptyView.setText(getResources().getString(R.string.text_view_favorites_empty));
        }

        if (getResources().getBoolean(R.bool.isTablet)&& favoriteStations.size()>0) {
            Station firstStation = favoriteStations.get(0);
            Bundle args = new Bundle();
            args.putParcelable(Utilities.EXTRA_STATION, firstStation);
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_container, detailFragment)
                    .commit();
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (mAdapter.getItemCount() == 0) {
            if (getResources().getBoolean(R.bool.isTablet)) {
                mDetailContainer.setVisibility(View.GONE);
            }
        }
    }

    public void sortData(int sortCode) {
        if (mAdapter != null) {
            mAdapter.sortData(sortCode);
        }
    }



    @Subscribe
    public void onDataReturned(StationListEvent event) {
        fillAdapter(event.getStationList());
    }

    @Subscribe
    public void onFavoritesEvent(FavoritesEvent favoritesEvent) {
        mFavoritesArray = favoritesEvent.getFavoritesArray();
    }

    @Subscribe
    public void onSortEvent(SortEvent event) {
        sortData(event.getSortCode());
    }
}
