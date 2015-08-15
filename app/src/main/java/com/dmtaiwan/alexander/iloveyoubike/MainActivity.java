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
import android.view.View;

import com.dmtaiwan.alexander.iloveyoubike.Sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.FragmentCallback;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapterStation;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by lenovo on 8/6/2015.
 */
public class MainActivity extends AppCompatActivity implements StationListFragment.Callback, StationDetailFragment.OnFavoriteListener, LocationProvider.LocationCallback {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean mTabletLayout = false;
    private boolean mFavoriteChanged = false;
    private boolean mLocationChanged = false;
    ViewPagerAdaper mAdapter;
    private LocationProvider mLocationProvider;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.tab_layout)
    TabLayout mTabLayout;

    @InjectView(R.id.view_pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        //Setup a location provider
        mLocationProvider = new LocationProvider(this, this);

        //Setup syncadapter
        IloveyoubikeSyncAdapter.initializeSyncAdapter(this);

        setSupportActionBar(mToolbar);

        setupViewPager(mViewPager);

        //Set up tab layout
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                //Callback to restart the loader to reflect changes
                if (mLocationChanged || mFavoriteChanged) {
                    FragmentCallback fragmentToShow = (FragmentCallback) mAdapter.instantiateItem(mViewPager, position);
                    fragmentToShow.onFragmentShown();
                    mLocationChanged = false;
                    mFavoriteChanged = false;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationProvider.connect();
        IloveyoubikeSyncAdapter.syncImmediately(this);
        Utilities.updateWidgets(this);
        checkPlayServices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    public void setupViewPager(ViewPager viewPager) {
        mAdapter = new ViewPagerAdaper(getSupportFragmentManager(), this);

        //Set up favorites fragment
        StationListFragment favoritesFragment = new StationListFragment();
        Bundle args = new Bundle();
        args.putBoolean(Utilities.EXTRA_FAVORITES, true);
        favoritesFragment.setArguments(args);
        mAdapter.addFragment(favoritesFragment);

        //Set up nearest station fragment
        StationDetailFragment nearestStationFragment = new StationDetailFragment();
        mAdapter.addFragment(nearestStationFragment);

        //Set up all stations fragment
        StationListFragment allStations = new StationListFragment();
        mAdapter.addFragment(allStations);


        //Maps Fragment
        MapFragment mapFragment = new MapFragment();
        mAdapter.addFragment(mapFragment);
        viewPager.setAdapter(mAdapter);

    }

    @Override
    public void onFavorited() {
        //Set favoriteChanged flag
        mFavoriteChanged = true;

        //For tablet mode, if item is favorited restart loader in station list fragment to show changes
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof StationListFragment) {
            StationListFragment stationListFragment = (StationListFragment) fragment;
            stationListFragment.restartLoader();
        }
    }



    @Override
    public void handleNewLocation(Location location) {
        mLocationChanged = true;
        Utilities.setUserLocation(location, this);
        Fragment fragment = getCurrentFragment();


        if (fragment instanceof StationDetailFragment) {
            StationDetailFragment stationDetailFragment = (StationDetailFragment) fragment;
            stationDetailFragment.restartLoader();
        }

        if (fragment instanceof StationListFragment) {
            StationListFragment stationListFragment = (StationListFragment) fragment;
            stationListFragment.restartLoader();
        }
    }

    public class ViewPagerAdaper extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private Context mContext;

        public ViewPagerAdaper(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] titleArray = mContext.getResources().getStringArray(R.array.tab_titles);
            return titleArray[position];
        }
    }


    @Override
    public void onItemSelected(int stationId, RecyclerAdapterStation.ViewHolder vh, boolean isTablet) {
        //Check if tablet layout
        mTabletLayout = isTablet;
        //TODO move tablet logic back here
        //Else start detail activity
        Intent detailIntent = new Intent(this, StationDetailActivity.class);
        detailIntent.putExtra(Utilities.EXTRA_DETAIL_ACTIVITY, true);
        detailIntent.putExtra(Utilities.EXTRA_STATION_ID, stationId);
        //Transitons
        //TODO postpone transitons for phone mode
        Pair<View, String> p1 = Pair.create((View) vh.stationStatus, getString(R.string.transition_status_iamge_view));
        Pair<View, String> p2 = Pair.create((View) vh.stationName, getString(R.string.transitoin_station_name_text));
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1, p2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            startActivity(detailIntent, options.toBundle());
        } else {
            startActivity(detailIntent);
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

    private Fragment getCurrentFragment() {
        int currentFragment = mViewPager.getCurrentItem();
        return (Fragment) mAdapter.instantiateItem(mViewPager, currentFragment);
    }
}
