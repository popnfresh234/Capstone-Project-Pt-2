package com.dmtaiwan.alexander.iloveyoubike.Views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dmtaiwan.alexander.iloveyoubike.Bus.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Bus.StationListEvent;
import com.dmtaiwan.alexander.iloveyoubike.Models.Station;
import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Alexander on 10/25/2015.
 */
public class MapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private HashMap<Marker, Integer> mIdMap;
    private SparseArray<Marker> mMarkerMap;
    private LatLng mCurrentCameraLatLng;
    private int mStationId = -1;
    private Boolean mIsGoto = false;
    private List<Station> mStationsList;

    @Bind(R.id.mapView)
    MapView mMapView;

    public static MapFragment newInstance() {
        MapFragment mapFragment = new MapFragment();
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //Hashmap for looking up ID by Marker
        mIdMap = new HashMap<Marker, Integer>();
        //SparseArray for looking up Marker by ID
        mMarkerMap = new SparseArray<Marker>();

        if (savedInstanceState != null) {
            Log.i("savedInstanceState", "not null");
            mCurrentCameraLatLng = savedInstanceState.getParcelable(Utilities.EXTRA_OUTSTATE_LATLNG);
            mStationId = savedInstanceState.getInt(Utilities.EXTRA_STATION_ID);
        }
        setHasOptionsMenu(true);
        EventBus.getInstance().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, rootView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); //needed to get the map to display immediately
        mMap = mMapView.getMap();
        if (mMap != null) {
            setupMap();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.clear(); //Clear markers
            mMapView.onResume();
        }
        mMarkerMap.clear(); //Clear out hashmap
        mStationsList = ((MainActivity) getActivity()).getStationList();
        setData(mStationsList);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
        //reset goto station flag for setting user location
        mIsGoto = false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        menu.clear();
//        inflater.inflate(R.menu.menu_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_settings_map) {
//            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
//            startActivity(settingsIntent);
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mStationsList != null) {
            populateMap(mStationsList);
        }
        //Set current camera positoin
        mCurrentCameraLatLng = cameraPosition.target;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int id = mIdMap.get(marker);
        Station selectedStation = null;
        for (Station station : mStationsList) {
            if (station.getId() == id) {
                selectedStation = station;
            }
        }
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(Utilities.EXTRA_STATION, selectedStation);
        startActivity(intent);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        mStationId = mIdMap.get(marker);
        return false;
    }

    public void setData(List<Station> stationsList) {
        mStationsList = stationsList;
        setUserLocation();
        populateMap(stationsList);
    }

    private void setupMap() {
        mMap.setOnCameraChangeListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);

    }

    private void setUserLocation() {
        if (mMap != null && !mIsGoto) {
            if (mCurrentCameraLatLng == null) {
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
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentCameraLatLng, 14f), 10, null);
            }
        }
    }

    private void populateMap(List<Station> stationsList) {
        //Check language for setting station title
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String language = preferences.getString(getActivity().getString(R.string.pref_key_language), getActivity().getString(R.string.pref_language_english));

        if (mMap != null && stationsList != null) {
            //Get visible bounds of map
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            for (int i = 0; i < stationsList.size(); i++) {
                Station station = stationsList.get(i);
                int stationId = station.getId();
                if (bounds.contains(new LatLng(station.getLat(), station.getLng()))) {
                    if (mMarkerMap.indexOfKey(stationId) < 0) {
                        int bikesAvailable = station.getSbi();
                        int spacesAvailable = station.getBemp();
                        int markerDrawable = Utilities.getMarkerIconDrawable(bikesAvailable, spacesAvailable);
                        String snippet = getString(R.string.snippet_string_bikes) + String.valueOf(bikesAvailable) + " " + getString(R.string.snippet_string_spaces) + String.valueOf(spacesAvailable);


                        String title;
                        if (language.equals(getActivity().getString(R.string.pref_language_english))) {
                            title = station.getSnaen();
//                        } else if (language.equals(getActivity().getString(R.string.pref_language_pinyin))) {
//                            int stringId = getResources().getIdentifier("station" + String.valueOf(stationId), "string", getActivity().getPackageName());
//                            title = getString(stringId);
                        } else {
                            title = station.getSna();
                        }

                        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(station.getLat(), station.getLng())).title(title);
                        markerOptions.snippet(snippet);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(markerDrawable));
                        Marker marker = mMap.addMarker(markerOptions);
                        mIdMap.put(marker, stationId);
                        mMarkerMap.put(stationId, marker);
                    }
                } else {
                    //If the marker was previously on screen, remove it from the map and hashmap
                    if (mMarkerMap.indexOfKey(stationId) >= 0) {
                        mMarkerMap.get(stationId).remove();
                        mMarkerMap.remove(stationId);
                    }
                }

            }
        }
        //If a station ID has been set from detail fragment, display its info window
        if (mStationId != -1) {
            Marker currentMarker = mMarkerMap.get(mStationId);
            if (currentMarker != null) {
                currentMarker.showInfoWindow();
            }
        }
    }

    public void zoomToStation(LatLng stationLatLng) {
        if (mMap != null) {
            mIsGoto = true;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stationLatLng, 14f), 10, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("saving", "saved");
        outState.putParcelable(Utilities.EXTRA_OUTSTATE_LATLNG, mCurrentCameraLatLng);
        outState.putInt(Utilities.EXTRA_STATION_ID, mStationId);
        super.onSaveInstanceState(outState);
    }

    @Subscribe
    public void onDataReturned(StationListEvent event) {
        mStationsList = event.getStationList();
        populateMap(event.getStationList());
    }
}

