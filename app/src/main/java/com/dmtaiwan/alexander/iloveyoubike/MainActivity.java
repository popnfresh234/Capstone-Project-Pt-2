package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.FavoriteEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationEvent;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapterStation;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerEvent;
import com.dmtaiwan.alexander.iloveyoubike.sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallback {

    private static String LOG_TAG = MainActivity.class.getSimpleName();

    private FragmentPagerAdapter mPagerAdapter;
    private LocationProvider mLocationProvider;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.tab_layout)
    TabLayout mTabLayout;

    @Bind(R.id.view_pager)
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //If not tablet mode, enter transition
        if (!getResources().getBoolean(R.bool.isTablet)) {
            supportPostponeEnterTransition();
        }

        //Setup a location provider
        mLocationProvider = new LocationProvider(this, this);

        //Setup Viewpager and Toolbar
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);
        setSupportActionBar(mToolbar);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        //Setup Syncadapter
        IloveyoubikeSyncAdapter.initializeSyncAdapter(this);

        //Register event bus
        EventBus.getInstance().register(this);

        checkPlayServices();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getParcelableExtra(Utilities.EXTRA_LATLNG) != null) {
            Bundle bundle = intent.getParcelableExtra(Utilities.EXTRA_LATLNG);
            LatLng stationLatLng = bundle.getParcelable(Utilities.EXTRA_LATLNG);
            Log.i(LOG_TAG, stationLatLng.toString());
            int stationId = bundle.getInt(Utilities.EXTRA_STATION_ID);
            gotoMap(stationLatLng, stationId);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mLocationProvider.connect();
        IloveyoubikeSyncAdapter.syncImmediately(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unregister(this);
    }

    @Override
    public void handleNewLocation(Location location) {
        //Set the user's location
        Log.i(LOG_TAG, "Location Changed");
        Utilities.setUserLocation(location, this);
        LocationEvent locationEvent = new LocationEvent();
        locationEvent.setNewLocation(location);
        EventBus.getInstance().post(locationEvent);
    }

    public static class PagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 4;
        private Context mContext;

        public PagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    //Favorites fragment
                    return StationListFragment.newInstance(0, "First", true);
                case 1:
                    return StationDetailFragment.newInstance(1, null, false, true);
                case 2:
                    //All stations fragment
                    return StationListFragment.newInstance(2, "Third", false);
                case 3:
                    return MapFragment.newInstance(1, null);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] titleArray = mContext.getResources().getStringArray(R.array.tab_titles);
            return titleArray[position];
        }

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

    public Fragment getCurrentFragment() {
        if (mViewPager != null) {
            int currentFragment = mViewPager.getCurrentItem();
            return (Fragment) mPagerAdapter.instantiateItem(mViewPager, currentFragment);
        } else {
            return null;
        }
    }

    public void passShareIntentToFragment(Intent shareIntent) {
        if (getCurrentFragment() instanceof StationListFragment) {
            StationListFragment stationListFragment = (StationListFragment) getCurrentFragment();
            stationListFragment.setShareIntent(shareIntent);
        }
    }

    public void gotoMap(LatLng stationLatLng, int stationId) {
        mViewPager.setCurrentItem(3);
        MapFragment MapFragment = (MapFragment) getCurrentFragment();
        MapFragment.zoomToStation(stationLatLng);
        MapFragment.setStationId(stationId);
    }

    @Subscribe
    public void onItemSelected(RecyclerEvent recyclerEvent) {

        //Only execute this code if not tablet mode
        if (!getResources().getBoolean(R.bool.isTablet)) {
            int stationId = recyclerEvent.getStationId();
            RecyclerAdapterStation.ViewHolder vh = recyclerEvent.getVh();
            Intent detailIntent = new Intent(this, StationDetailActivity.class);
            detailIntent.putExtra(Utilities.EXTRA_DETAIL_ACTIVITY, true);
            detailIntent.putExtra(Utilities.EXTRA_STATION_ID, stationId);
            //Transitions
            Pair<View, String> p1 = Pair.create((View) vh.stationStatus, getString(R.string.transition_status_image_view));
            Pair<View, String> p2 = Pair.create((View) vh.stationName, getString(R.string.transition_station_name_text));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1, p2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                startActivity(detailIntent, options.toBundle());
            } else {
                startActivity(detailIntent);
            }
        } else {
            StationDetailFragment stationDetailFragment = StationDetailFragment.newInstance(0, null, false, false);
            Bundle args = new Bundle();
            args.putInt(Utilities.EXTRA_STATION_ID, recyclerEvent.getStationId());
            stationDetailFragment.setArguments(args);
            getCurrentFragment().getChildFragmentManager().beginTransaction().replace(R.id.detail_container, stationDetailFragment).commit();
        }
    }
}
