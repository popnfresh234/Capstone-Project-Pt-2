package com.dmtaiwan.alexander.iloveyoubike;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by lenovo on 7/29/2015.
 */
public class StationDetailActivity extends AppCompatActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            StationDetailFragment fragment = new StationDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.station_detail_container, fragment)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }
    }
}
