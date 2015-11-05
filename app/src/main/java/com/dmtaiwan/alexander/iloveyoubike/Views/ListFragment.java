package com.dmtaiwan.alexander.iloveyoubike.Views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmtaiwan.alexander.iloveyoubike.Bus.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Bus.StationListEvent;
import com.dmtaiwan.alexander.iloveyoubike.Models.Station;
import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapter;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Alexander on 10/22/2015.
 */
public class ListFragment extends Fragment {

    private static final String LOG_TAG = ListFragment.class.getSimpleName();


    private RecyclerAdapter mAdapter;

    @Bind(R.id.empty_view)
    View mEmptyView;

    @Bind(R.id.recycler_view_station_list)
    RecyclerView mRecyclerView;


    public static ListFragment newInstance() {
        ListFragment listFragment = new ListFragment();
        return listFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private void fillAdapter(List<Station> stationList) {
        if (mAdapter != null) {
            if (stationList != null && mAdapter != null) {
                mAdapter.updateData(stationList);
            } else if (mAdapter!= null){
                mAdapter.setEmptyView();
            }
        }
    }

    @Subscribe
    public void onDataReturned(StationListEvent event) {
        fillAdapter(event.getStationList());
    }
}
