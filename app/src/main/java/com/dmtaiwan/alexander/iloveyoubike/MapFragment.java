package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.FragmentCallback;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

/**
 * Created by Alexander on 8/11/2015.
 */
public class MapFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FragmentCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener {

    MapView mMapView;
    private GoogleMap mMap;
    private HashMap<Marker, Integer> mIdMap;
    private HashMap<Integer, Marker> mMarkerMap;
    private static int MAPS_LOADER = 100;
    private Cursor mData;
    private Boolean mIsGotoStation = false;
    private int mStationId = -1;
    private int mOutstateId = -1;
    private Marker mCurrentMarker;
    private Boolean mFirstRun = true;
    private Boolean mIsPopulated;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(MAPS_LOADER, null, this);
        mIsPopulated = false;
        if (savedInstanceState != null) {
            mStationId = savedInstanceState.getInt(Utilities.EXTRA_STATION_ID);
        }
        //Create hashmap for associating stationId with marker
        mIdMap = new HashMap<Marker, Integer>();
        mMarkerMap = new HashMap<Integer, Marker>();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap = mMapView.getMap();
        if (mMap != null) {
            mMap.setOnCameraChangeListener(this);
            setupMap();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        restartLoader();
    }

    @Override
    public void onPause() {
        super.onPause();
        setIsGotoStation(false);
        mFirstRun = true;
        mIsPopulated = false;
    }


    @Override
    public void onFragmentShown() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_generic_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri allStationsUri = StationContract.StationEntry.buildUriAllStations();
        String sortOrder = StationContract.StationEntry.COLUMN_STATION_ID + " ASC";
        return new CursorLoader(
                getActivity(),
                allStationsUri,
                STATION_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            if (mMap != null && Utilities.isGooglePlayAvailable(getActivity())) {

                if (!mIsGotoStation) {
                    setUserLocation();
                    if (mOutstateId != -1) {
                        mStationId = mOutstateId;
                    }
                    if (!mIsPopulated) {
                        populateMap(data);
                    }

                }
                mData = data;
            } else {
                mIdMap.clear();
                mMarkerMap.clear();
                setUserLocation();
                if (mOutstateId != -1) {
                    mStationId = mOutstateId;
                }
                if (!mIsPopulated) {
                    populateMap(data);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setUserLocation() {
        if (mMap != null && mFirstRun) {
            mFirstRun = false;
            Location userLocation = Utilities.getUserLocation(getActivity());

            if (userLocation != null) {
                //Get the user's location and zoom the camera if less than 20km (20000meters) from Taipei, otherwise zoom to default location
                float distanceFromTaipei = Utilities.calculateDistance(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG, userLocation);
                if (distanceFromTaipei <= 20000) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 14.5f), 10, null);
                } else {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG), 14f), 10, null);
                }
            } else {
                //Default location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG), 14f), 10, null);
            }
        }
    }

    public void zoomToStation(LatLng stationLatLng) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stationLatLng, 14f), 10, null);
        }
    }

    private void populateMap(Cursor data) {
        //This method adds markers only to the visible section of the map.  Otherwise adding too many markers blocks the UI thread.
        //Create hashmap to store markers for lookup by ID

        if (mMap != null) {
            mIsPopulated = true;
            //Get visibile bounds of map
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            if (data.moveToFirst()) {
                do {
                    int stationId = data.getInt(COL_STATION_ID);
                    if (bounds.contains(new LatLng(data.getDouble(COL_STATION_LAT), data.getDouble(COL_STATION_LONG)))) {

                        if (!mMarkerMap.containsKey(stationId)) {
                            int bikesAvailable = data.getInt(COL_BIKES_AVAILABLE);
                            int spacesAvailable = data.getInt(COL_SPACES_AVAILABLE);
                            int markerDrawable = Utilities.getMarkerIconDrawable(bikesAvailable, spacesAvailable);
                            String snippet = getString(R.string.snippet_string_bikes) + String.valueOf(bikesAvailable) + " " + getString(R.string.snippet_string_spaces) + String.valueOf(spacesAvailable);

                            //Check language and get string for snippet title accordingly
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String title;
                            String language = preferences.getString(getActivity().getString(R.string.pref_key_language), getActivity().getString(R.string.pref_language_english));
                            if (language.equals(getActivity().getString(R.string.pref_language_english))) {
                                title = data.getString(COL_STATION_NAME_EN);
                            } else {
                                title = data.getString(COL_STATION_NAME_ZH);
                            }

                            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(data.getDouble(COL_STATION_LAT), data.getDouble(COL_STATION_LONG))).title(title);
                            markerOptions.snippet(snippet);
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(markerDrawable));
                            Marker marker = mMap.addMarker(markerOptions);
                            mIdMap.put(marker, stationId);
                            mMarkerMap.put(stationId, marker);
                        }
                    } else {
                        //If the marker was previously on screen, remove it from the map and hashmap
                        if (mMarkerMap.containsKey(stationId)) {
                            mMarkerMap.get(stationId).remove();
                            mMarkerMap.remove(stationId);
                        }
                    }

                } while (data.moveToNext());
            }
            if (mStationId != -1) {
                mCurrentMarker = mMarkerMap.get(mStationId);
                mOutstateId = mStationId;
                if (mCurrentMarker != null) {
                    mCurrentMarker.showInfoWindow();
                }
                mStationId = -1;
            }
        }
    }

    public void restartLoader() {
        getActivity().getSupportLoaderManager().restartLoader(MAPS_LOADER, null, this);
    }

    private void setupMap() {
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int id = mIdMap.get(marker);
        Intent intent = new Intent(getActivity(), StationDetailActivity.class);
        intent.putExtra(Utilities.EXTRA_STATION_ID, id);
        startActivity(intent);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mData != null && !mData.isClosed()) {
            populateMap(mData);
        }

    }

    public void setIsGotoStation(Boolean isGotoStation) {
        mIsGotoStation = isGotoStation;
    }

    public void setStationId(int stationId) {
        mStationId = stationId;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mOutstateId != -1) {
            outState.putInt(Utilities.EXTRA_STATION_ID, mOutstateId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mStationId = mIdMap.get(marker);
        return false;
    }
}
