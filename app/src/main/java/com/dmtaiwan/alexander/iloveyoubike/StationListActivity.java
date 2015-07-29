package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapterStation;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;

/**
 * Created by Alexander on 7/28/2015.
 */
public class StationListActivity extends AppCompatActivity implements StationListFragment.Callback{
    private static final String LOG_TAG = StationListActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_list);
    }

    @Override
    public void onItemSelected(int stationId, RecyclerAdapterStation.ViewHolder vh) {
        Log.i(LOG_TAG, String.valueOf(stationId));
        Intent detailIntent = new Intent(this, StationDetailActivity.class);
        detailIntent.putExtra(Utilities.EXTRA_STATION_ID, stationId);
        startActivity(detailIntent);
    }
}
