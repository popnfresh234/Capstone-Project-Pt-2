package com.dmtaiwan.alexander.iloveyoubike;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by lenovo on 7/29/2015.
 */
public class StationDetailActivity extends AppCompatActivity implements StationDetailFragment.OnFavoriteListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.inject(this);
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.


            StationDetailFragment fragment = new StationDetailFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.station_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onFavorited(int stationId) {

    }
}
