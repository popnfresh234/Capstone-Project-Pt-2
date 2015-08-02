package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dmtaiwan.alexander.iloveyoubike.Sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.dmtaiwan.alexander.iloveyoubike.data.StationDbHelper;
import com.google.gson.Gson;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallback {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private LocationProvider mLocationProvider;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize SyncAdapter, fills database if new account
        IloveyoubikeSyncAdapter.initializeSyncAdapter(this);

        //Create location provider to attempt to determine location
        mLocationProvider = new LocationProvider(this, this);
        mContext = this;

        //If for some reason the account has already been created but the app's data has been cleared fill database
        StationDbHelper dbHelper = new StationDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(StationContract.StationEntry.TABLE_NAME, null, null, null, null, null, null);
        Log.i(LOG_TAG, String.valueOf(cursor.getCount()));
        if (cursor.getCount() == 0) {
            IloveyoubikeSyncAdapter.syncImmediately(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    public void onListItemClick(int position) {
        Intent intent;
        switch (position) {
            case 0:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                ArrayList<String> favoritesArray = Utilities.getFavoriteArray(prefs);
                if (favoritesArray != null && favoritesArray.size() > 0) {
                    intent = new Intent(this, StationListActivity.class);
                    intent.putExtra(Utilities.EXTRA_FAVORITES, true);
                    startActivity(intent);
                } else {
                    //Create a snackbar offering to launch station list activity to select a station
                    Snackbar.make(findViewById(R.id.fragment_main), getString(R.string.snackbar_favorites), Snackbar.LENGTH_LONG)
                            .setActionTextColor(getResources().getColor(R.color.theme_primary_dark))
                            .setAction(getString(R.string.snackbar_action_list), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent stationListIntent = new Intent(mContext, StationListActivity.class);
                                    startActivity(stationListIntent);
                                }
                            })
                            .show();
                }

                break;
            case 1:
                //Check if the user has a location
                SharedPreferences prefsLocation = PreferenceManager.getDefaultSharedPreferences(this);
                Location location = Utilities.getUserLocation(prefsLocation);

                //If locatin is available, launch details activity
                if (location != null) {
                    intent = new Intent(this, StationDetailActivity.class);
                    startActivity(intent);
                } else {
                    //Create a snackbar offering to launch settings to turn on location
                    Snackbar.make(findViewById(R.id.fragment_main), getString(R.string.snackbar_location), Snackbar.LENGTH_LONG)
                            .setActionTextColor(getResources().getColor(R.color.theme_primary_dark))
                            .setAction(getString(R.string.snackbar_action_settings), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(settingsIntent);
                                }
                            })
                            .show();
                }

                break;
            case 2:
                intent = new Intent(this, StationListActivity.class);
                startActivity(intent);
                break;
            case 3:
                break;
            default:
                break;
        }
    }

    @Override
    public void handleNewLocation(Location location) {
        Log.i(LOG_TAG, location.toString());
        SharedPreferences settings;
        SharedPreferences.Editor spe;

        //Store user's last known location
        try {
            settings = PreferenceManager.getDefaultSharedPreferences(this);
            spe = settings.edit();
            Gson gson = new Gson();
            String json = gson.toJson(location);
            spe.putString(Utilities.SHARED_PREFS_LOCATION_KEY, json);
            spe.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
