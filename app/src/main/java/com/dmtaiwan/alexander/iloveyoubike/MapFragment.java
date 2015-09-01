package com.dmtaiwan.alexander.iloveyoubike;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.data.StationContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

/**
 * Created by Alexander on 8/28/2015.
 */
public class MapFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, LocationProvider.LocationCallback, FragmentCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener {

    private static final String LOG_TAG = MapFragment.class.getSimpleName();

    private static int MAPS_LOADER = 100;
    private MapView mMapView;
    private GoogleMap mMap;
    private HashMap<Marker, Integer> mIdMap;
    private HashMap<Integer, Marker> mMarkerMap;
    private Cursor mData;
    private Boolean mIsPopulated;
    private Boolean mIsLocationSet = false;
    private LatLng mOutstateLatLng;
    private Boolean mIsGoto = false;
    private int mStationId = -1;

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
        mIsPopulated = false;
        //Hashmap for looking up ID by marker
        mIdMap = new HashMap<Marker, Integer>();
        //Create hashmap for looking up markers by ID
        mMarkerMap = new HashMap<Integer, Marker>();
        getActivity().getSupportLoaderManager().initLoader(MAPS_LOADER, null, this);


        if (savedInstanceState != null) {
            mIsLocationSet = savedInstanceState.getBoolean(Utilities.EXTRA_LOCATION_SET);
            mOutstateLatLng = savedInstanceState.getParcelable(Utilities.EXTRA_OUTSTATE_LATLNG);
            mStationId = savedInstanceState.getInt(Utilities.EXTRA_STATION_ID);
        }

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately
        mMap = mMapView.getMap();
        if (mMap != null) {
            setupMap();
        }

        return rootView;
    }

    private void setupMap() {
        mMap.setOnCameraChangeListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        //Clear the map of old markers and clear out hashmap
        if (mMap != null) {
            mMap.clear();
        }

        mMarkerMap.clear();
        super.onResume();
        restartLoader();

    }

    public void restartLoader() {
        getActivity().getSupportLoaderManager().restartLoader(MAPS_LOADER, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
        //Reset the goto station flag for setting the user location
        mIsGoto = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
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
            mData = data;

                setUserLocation();


        }
    }

    private void setUserLocation() {
        if (mMap != null && !mIsGoto) {
            if (mOutstateLatLng == null) {
                Location userLocation = Utilities.getUserLocation(getActivity());
                if (userLocation != null) {
                    //Get the user's location and zoom the camera if less than 20km (20000meters) from Taipei, otherwise zoom to default location
                    float distanceFromTaipei = Utilities.calculateDistance(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG, userLocation);
                    if (distanceFromTaipei <= 20000) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 14.5f), 10, null);
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG), 14f), 10, null);
                    }
                    mIsLocationSet = true;
                } else {
                    //Default location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG), 14f), 10, null);
                    mIsLocationSet = true;
                }
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mOutstateLatLng, 14f), 10, null);
            }
        }
    }


    private void populateMap(Cursor data) {
        //Check language for setting station title
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String language = preferences.getString(getActivity().getString(R.string.pref_key_language), getActivity().getString(R.string.pref_language_english));

        if (mMap != null && data.moveToFirst()) {
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


                            String title;
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
            //If a station ID has been set from detail fragment, display its info window
            if (mStationId != -1) {
                Marker currentMarker = mMarkerMap.get(mStationId);
                if (currentMarker != null) {
                    currentMarker.showInfoWindow();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void handleNewLocation(Location location) {

    }

    @Override
    public void onFragmentShown() {
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        if (mData != null && !mData.isClosed()) {
            populateMap(mData);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int id = mIdMap.get(marker);
        Intent intent = new Intent(getActivity(), StationDetailActivity.class);
        intent.putExtra(Utilities.EXTRA_STATION_ID, id);
        startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mStationId = mIdMap.get(marker);
        return false;
    }

    public void zoomToStation(LatLng stationLatLng) {
        if (mMap != null) {
            mIsGoto = true;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stationLatLng, 14f), 10, null);
        }
    }

    public void setStationId(int stationId) {
        mStationId = stationId;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Utilities.EXTRA_LOCATION_SET, mIsLocationSet);
        if (mMap != null) {
            outState.putParcelable(Utilities.EXTRA_OUTSTATE_LATLNG, mMap.getCameraPosition().target);
        }
        outState.putInt(Utilities.EXTRA_STATION_ID, mStationId);
        super.onSaveInstanceState(outState);
    }

}
