package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
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
public class MapFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FragmentCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraChangeListener{

    MapView mMapView;
    private GoogleMap googleMap;
    private HashMap<Marker, Integer> mIdMap;
    private static int MAPS_LOADER = 100;
    private Cursor mData;

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

        //Create hashmap for associating stationId with marker
        mIdMap = new HashMap<Marker, Integer>();
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

        googleMap = mMapView.getMap();
        if (googleMap != null) {
            googleMap.setOnCameraChangeListener(this);
            setupMap();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        restartLoader();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onFragmentShown() {
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
            if (googleMap != null && Utilities.isGooglePlayAvailable(getActivity())) {
                googleMap.clear();
                setUserLocation();
                mData = data;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setUserLocation() {
        Location userLocation = Utilities.getUserLocation(getActivity());

        if (userLocation != null) {
            //Get the user's location and zoom the camera if less than 20km (20000meters) from Taipei, otherwise zoom to default location
            float distanceFromTaipei = Utilities.calculateDistance(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG, userLocation);
            if (distanceFromTaipei <= 20000) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 14.5f), 10, null);
            }else{
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG), 14f), 10, null);
            }
        } else {
            //Default location
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Utilities.TAIPEI_LAT, Utilities.TAIPEI_LONG), 14f), 10, null);
        }
    }

    private void populateMap(Cursor data) {
        //This method adds markers only to the visible section of the map.  Otherwise adding too many markers blocks the UI thread.
       //Create hashmap to store markers for lookup by ID
       HashMap<Integer, Marker> markerList = new HashMap<Integer, Marker>();
        if (googleMap != null) {
            //Get visibile bounds of map
            LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
            if (data.moveToFirst()) {
                do {
                    int stationId = data.getInt(COL_STATION_ID);
                    if (bounds.contains(new LatLng(data.getDouble(COL_STATION_LAT), data.getDouble(COL_STATION_LONG)))) {

                        if (!markerList.containsKey(stationId)) {
                            int bikesAvailable = data.getInt(COL_BIKES_AVAILABLE);
                            int spacesAvailable = data.getInt(COL_SPACES_AVAILABLE);
                            int markerDrawable = Utilities.getMarkerIconDrawable(bikesAvailable, spacesAvailable);
                            String snippet = getString(R.string.snippet_string_bikes) + String.valueOf(bikesAvailable) + " " + getString(R.string.snippet_string_spaces) + String.valueOf(spacesAvailable);
                            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(data.getDouble(COL_STATION_LAT), data.getDouble(COL_STATION_LONG))).title(data.getString(COL_STATION_NAME_EN));
                            markerOptions.snippet(snippet);
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(markerDrawable));
                            Marker marker = googleMap.addMarker(markerOptions);
                            mIdMap.put(marker, stationId);
                            markerList.put(stationId, marker);
                        }
                    }else {
                        //If the marker was previously on screen, remove it from the map and hashmap
                        if (markerList.containsKey(stationId)) {
                            markerList.get(stationId).remove();
                            markerList.remove(stationId);
                        }
                    }

                } while (data.moveToNext());
            }
        }
    }
    private void restartLoader() {
        getActivity().getSupportLoaderManager().restartLoader(MAPS_LOADER, null, this);
    }

    private void setupMap() {
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setMyLocationEnabled(true);
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
        populateMap(mData);
    }
}
