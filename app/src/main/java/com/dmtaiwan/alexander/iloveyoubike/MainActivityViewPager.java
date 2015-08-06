package com.dmtaiwan.alexander.iloveyoubike;

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
import android.view.View;

import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.RecyclerAdapterStation;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by lenovo on 8/6/2015.
 */
public class MainActivityViewPager extends AppCompatActivity implements StationListFragmentClean.Callback, StationDetailFragmentPager.OnFavoriteListener, LocationProvider.LocationCallback {
    private boolean mTabletLayout = false;
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
        setContentView(R.layout.activity_main_pager);
        ButterKnife.inject(this);

        //Setup a location provider
        mLocationProvider = new LocationProvider(this, this);

        setSupportActionBar(mToolbar);
        setupViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (position == 0) {
                    StationListFragmentClean fragmentClean = (StationListFragmentClean) mAdapter.getItem(0);
                    if (fragmentClean.isAdded()) {
                        fragmentClean.restartLoader();
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    public void setupViewPager(ViewPager viewPager) {
        mAdapter = new ViewPagerAdaper(getSupportFragmentManager());

        //Set up favorites fragment
        StationListFragmentClean favoritesFragment = new StationListFragmentClean();
        Bundle args = new Bundle();
        args.putBoolean(Utilities.EXTRA_FAVORITES, true);
        favoritesFragment.setArguments(args);
        mAdapter.addFragment(favoritesFragment);
        //Set up nearest station fragment
        StationDetailFragmentPager nearestStationFragment = new StationDetailFragmentPager();
        mAdapter.addFragment(nearestStationFragment);

        //Set up all stations fragment
        StationListFragmentClean allStations = new StationListFragmentClean();
        mAdapter.addFragment(allStations);

        viewPager.setAdapter(mAdapter);
    }

    @Override
    public void onFavorited() {
        StationListFragmentClean fragment = (StationListFragmentClean) mAdapter.getItem(0);
        fragment.restartLoader();
    }

    @Override
    public void handleNewLocation(Location location) {
        Utilities.setUserLocation(location, this);
        int currentFragment = mViewPager.getCurrentItem();
        Fragment fragment = mAdapter.getItem(currentFragment);

        if (fragment instanceof StationDetailFragmentPager) {
            StationDetailFragmentPager stationDetailFragmentPager = (StationDetailFragmentPager) fragment;
            stationDetailFragmentPager.restartLoader();
        }

        if (fragment instanceof StationListFragmentClean) {
            StationListFragmentClean stationListFragmentClean = (StationListFragmentClean) fragment;
            stationListFragmentClean.restartLoader();
        }

    }

    public class ViewPagerAdaper extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdaper(FragmentManager fm) {
            super(fm);
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
    }

    @Override
    public void onItemSelected(int stationId, RecyclerAdapterStation.ViewHolder vh) {

        if (mTabletLayout) {
            Bundle args = new Bundle();
            args.putInt(Utilities.EXTRA_STATION_ID, stationId);
            StationDetailFragment fragment = new StationDetailFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, fragment).commit();
        } else {

            Intent detailIntent = new Intent(this, StationDetailActivity.class);
            detailIntent.putExtra(Utilities.EXTRA_STATION_ID, stationId);
            Pair<View, String> p1 = Pair.create((View) vh.stationStatus, getString(R.string.transition_status_iamge_view));
            Pair<View, String> p2 = Pair.create((View) vh.stationName, getString(R.string.transitoin_station_name_text));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1, p2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                startActivity(detailIntent, options.toBundle());
            } else {
                startActivity(detailIntent);
            }
        }
    }
}
