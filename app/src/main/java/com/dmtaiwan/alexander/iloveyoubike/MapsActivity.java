package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.OnInfoWindowClickListener {

    private static String LOG_TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static int MAPS_LOADER = 100;
    private HashMap<Marker, Integer> mIdMap;

    private static final String[] STATION_COLUMNS = {
            StationContract.StationEntry._ID,
            StationContract.StationEntry.COLUMN_STATION_ID,
            StationContract.StationEntry.COLUMN_STATION_NAME_ZH,
            StationContract.StationEntry.COLUMN_STATION_DISTRICT_ZH,
            StationContract.StationEntry.COLUMN_STATION_NAME_EN,
            StationContract.StationEntry.COLUMN_STATION_DISTRICT_EN,
            StationContract.StationEntry.COLUMN_STATION_LAT,
            StationContract.StationEntry.COLUMN_STATION_LONG,
            StationContract.StationEntry.COLUMN_BIKES_AVAIABLE,
            StationContract.StationEntry.COLUMN_SPACES_AVAILABLE,
            StationContract.StationEntry.COLUMN_LAST_UPDATED
    };

    public static final int COL_ID = 0;
    public static final int COL_STATION_ID = 1;
    public static final int COL_STATION_NAME_ZH = 2;
    public static final int COL_STATION_DISTRICT_ZH = 3;
    public static final int COL_STATION_NAME_EN = 4;
    public static final int COL_STATION_DISTRICT_EN = 5;
    public static final int COL_STATION_LAT = 6;
    public static final int COL_STATION_LONG = 7;
    public static final int COL_BIKES_AVAILABLE = 8;
    public static final int COL_SPACES_AVAILABLE = 9;
    public static final int COL_LAST_UPDATED = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportLoaderManager().initLoader(MAPS_LOADER, null, this);
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartLoader();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri allStationsUri = StationContract.StationEntry.buildUriAllStations();
        String sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        return new CursorLoader(
                this,
                allStationsUri,
                STATION_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "Stations: " + String.valueOf(data.getCount()));

        if (data != null && data.moveToFirst()) {
            if (mMap != null && Utilities.isGooglePlayAvailable(this)) {
                mMap.clear();
                setUserLocation();
                populateMap(data);
            }
        }

    }




    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setUpMap() {
        mMap.setOnInfoWindowClickListener(this);
        mMap.setMyLocationEnabled(true);
    }

    private void restartLoader() {
        getSupportLoaderManager().restartLoader(MAPS_LOADER, null, this);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.i(LOG_TAG, marker.toString());
        Log.i(LOG_TAG, String.valueOf(mIdMap.get(marker)));
        int id = mIdMap.get(marker);
        Intent intent = new Intent(this, StationDetailActivity.class);
        intent.putExtra(Utilities.EXTRA_STATION_ID, id);
        startActivity(intent);
    }

    private void setUserLocation() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Location userLocation = Utilities.getUserLocation(sp);
        if (userLocation != null) {
            //Set marker and zoom map to user's location
//            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(userLocation.getLatitude(), userLocation.getLongitude())).title(getString(R.string.marker_user_location));
//            Marker marker = mMap.addMarker(markerOptions);
//            marker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 14.5f), 1000, null);
        } else {
            //Default location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG), 14f), 1000, null);
        }
    }

    private void populateMap(Cursor data) {
        int i = 0;
        //Create hashmap for associating stationId with marker
        mIdMap = new HashMap<Marker, Integer>();
            if(data.moveToFirst()) {
                do {
                    //Populate the map
                    i++;
                    int stationId = data.getInt(COL_STATION_ID);
                    int bikesAvailable = data.getInt(COL_BIKES_AVAILABLE);
                    int spacesAvailable = data.getInt(COL_SPACES_AVAILABLE);
                    int markerDrawable = Utilities.getMarkerIconDrawable(bikesAvailable, spacesAvailable);
                    String snippet = getString(R.string.snippet_string_bikes) + String.valueOf(bikesAvailable) + " " + getString(R.string.snippet_string_spaces) + String.valueOf(spacesAvailable);
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(data.getDouble(COL_STATION_LAT), data.getDouble(COL_STATION_LONG))).title(data.getString(COL_STATION_NAME_EN));
                    markerOptions.snippet(snippet);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(markerDrawable));
                    Marker marker = mMap.addMarker(markerOptions);
                    mIdMap.put(marker, stationId);
                }while(data.moveToNext());
            }
        Log.i(LOG_TAG, "Stations added to map: " + String.valueOf(i));
    }

}
