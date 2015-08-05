package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dmtaiwan.alexander.iloveyoubike.Sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.dmtaiwan.alexander.iloveyoubike.data.StationDbHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallback {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationProvider mLocationProvider;
    private Context mContext;
    private Cursor mCursor;
    private SQLiteDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize SyncAdapter, fills database if new account
        IloveyoubikeSyncAdapter.initializeSyncAdapter(this);
        IloveyoubikeSyncAdapter.syncImmediately(this);
        checkPlayServices();
        //Create location provider to attempt to determine location
        mLocationProvider = new LocationProvider(this, this);
        mContext = this;

        //If for some reason the account has already been created but the app's data has been cleared fill database
        StationDbHelper dbHelper = new StationDbHelper(this);
        mDb = dbHelper.getReadableDatabase();
        mCursor = mDb.query(StationContract.StationEntry.TABLE_NAME, null, null, null, null, null, null);
        Log.i(LOG_TAG, String.valueOf(mCursor.getCount()));
        if (mCursor.getCount() == 0) {
            IloveyoubikeSyncAdapter.syncImmediately(this);
        }
        mCursor.close();
        mDb.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mCursor.isClosed()) {
            mCursor.close();
        }
        if (mDb.isOpen()) {
            mDb.close();
        }
        mLocationProvider.disconnect();
    }

    public void onListItemClick(int position) {
        Intent intent;
        switch (position) {
            case 0:
                ArrayList<String> favoritesArray = Utilities.getFavoriteArray(this);
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
                Location location = Utilities.getUserLocation(this);

                //If locatin is available, launch details activity
                if (location != null && checkPlayServices()) {
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
                if (checkPlayServices()) {
                    intent = new Intent(this, StationListActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(this, "Please install Google Play Services", Toast.LENGTH_LONG).show();
                }

                break;
            case 3:
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void handleNewLocation(Location location) {
        Utilities.setUserLocation(location, this);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
