package com.dmtaiwan.alexander.iloveyoubike.Views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dmtaiwan.alexander.iloveyoubike.Models.Station;
import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;


/**
 * Created by Alexander on 10/21/2015.
 */
public class DetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_detail);
        if (savedInstanceState == null) {
            Station station = getIntent().getParcelableExtra(Utilities.EXTRA_STATION);
            DetailFragment detailFragment = new DetailFragment();
            Bundle args = new Bundle();
            args.putParcelable(Utilities.EXTRA_STATION, station);
            detailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(R.id.station_detail_container, detailFragment).commit();
        }

    }

}

