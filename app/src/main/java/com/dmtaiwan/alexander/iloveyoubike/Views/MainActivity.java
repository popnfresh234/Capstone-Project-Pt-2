package com.dmtaiwan.alexander.iloveyoubike.Views;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dmtaiwan.alexander.iloveyoubike.Bus.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Bus.RecyclerClickEvent;
import com.dmtaiwan.alexander.iloveyoubike.Bus.SortEvent;
import com.dmtaiwan.alexander.iloveyoubike.Bus.StationListEvent;
import com.dmtaiwan.alexander.iloveyoubike.Models.Station;
import com.dmtaiwan.alexander.iloveyoubike.Presenters.MainPresenter;
import com.dmtaiwan.alexander.iloveyoubike.Presenters.MainPresenterImpl;
import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainView, LocationProvider.LocationCallback, ViewPager.OnPageChangeListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private LocationProvider mLocationProvider;
    private MainPresenter mPresenter;
    private List<Station> mStationList;
    private FragmentPagerAdapter mPagerAdapter;

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
        mPresenter = new MainPresenterImpl(this, this);
        mLocationProvider = new LocationProvider(this, this);
        setSupportActionBar(mToolbar);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mTabLayout.setupWithViewPager(mViewPager);
        checkPlayServices();
        EventBus.getInstance().register(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mViewPager.getCurrentItem() == 0) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }else if (mViewPager.getCurrentItem() == 1) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }else if (mViewPager.getCurrentItem() == 2) {
            getMenuInflater().inflate(R.menu.menu_map, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                mPresenter.requestData();
                break;
            case R.id.action_sort_default:
                SortEvent defaultSortEvent = new SortEvent(Utilities.SORT_DEFAULT);
                EventBus.getInstance().post(defaultSortEvent);
                break;
            case R.id.action_sort_proximity:
                Log.i(LOG_TAG, "proximity sort");
                SortEvent proximitySortEvent = new SortEvent(Utilities.SORT_PROXIMITY);
                EventBus.getInstance().post(proximitySortEvent);
                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationProvider.connect();

        //Test Code
        if (mPresenter != null) {
            mPresenter.requestData();
        }
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
    public void onDataReturned(List<Station> stationList) {
        mStationList = stationList;
        StationListEvent event = new StationListEvent(mStationList);
        EventBus.getInstance().post(event);
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void handleNewLocation(Location location) {
        Utilities.setUserLocation(location, this);
        mPresenter.requestData();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Utilities.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    public static class PagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;
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
                    return FavoritesFragment.newInstance();
                case 1:
                    //All stations fragment
                    return ListFragment.newInstance();
                case 2:
                    //Map fragment
                    return MapFragment.newInstance();
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

    private Fragment getCurrentFragment() {
        if (mViewPager != null) {
            int currentFragment = mViewPager.getCurrentItem();
            return (Fragment) mPagerAdapter.instantiateItem(mViewPager, currentFragment);
        } else {
            return null;
        }
    }

    public List<Station> getStationList() {
        return mStationList;
    }

    @Subscribe
    public void onRecyclerViewClick(RecyclerClickEvent recyclerClickEvent) {
        Log.i(LOG_TAG, "event");
        if (getResources().getBoolean(R.bool.isTablet)) {
//            loadDetailFragment(recyclerClickEvent.getStation());
        } else {
            Station station = recyclerClickEvent.getStation();
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Utilities.EXTRA_STATION, station);
            startActivity(intent);
        }
    }
}
