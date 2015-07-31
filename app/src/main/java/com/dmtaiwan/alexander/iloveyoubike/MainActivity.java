package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.dmtaiwan.alexander.iloveyoubike.Sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.dmtaiwan.alexander.iloveyoubike.data.StationDbHelper;
import com.google.gson.Gson;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallback{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private LocationProvider mLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize SyncAdapter, fills database if new account
        IloveyoubikeSyncAdapter.initializeSyncAdapter(this);
        mLocationProvider = new LocationProvider(this, this);

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
                if(favoritesArray != null && favoritesArray.size()>0) {
                    intent = new Intent(this, StationListActivity.class);
                    intent.putExtra(Utilities.EXTRA_FAVORITES, true);
                    startActivity(intent);
                }else{
                    Toast.makeText(this, "You haven't added any favorites yet!", Toast.LENGTH_LONG).show();
                }

                break;
            case 1:
                intent = new Intent(this, StationDetailActivity.class);
                startActivity(intent);
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
