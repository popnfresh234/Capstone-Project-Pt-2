package com.dmtaiwan.alexander.iloveyoubike;

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

import com.dmtaiwan.alexander.iloveyoubike.Utilities.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationEvent;
import com.dmtaiwan.alexander.iloveyoubike.sync.IloveyoubikeSyncAdapter;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.LocationProvider;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallback{

    private static String LOG_TAG = MainActivity.class.getSimpleName();

    private FragmentPagerAdapter mPagerAdapter;
    private LocationProvider mLocationProvider;

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

        //Setup a location provider
        mLocationProvider = new LocationProvider(this, this);

        //Setup Viewpager and Toolbar
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        setSupportActionBar(mToolbar);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        //Setup Syncadapter
        IloveyoubikeSyncAdapter.initializeSyncAdapter(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void handleNewLocation(Location location) {
        //Set the user's location
        Log.i(LOG_TAG, "Location Changed");
        Utilities.setUserLocation(location, this);
        LocationEvent locationEvent = new LocationEvent();
        EventBus.getInstance().post(locationEvent);
    }

    public static class PagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TestFragment.newInstance(0, "First");
                case 1:
                    return StationDetailFragment.newInstance(1, null);
                case 2:
                    return TestFragment.newInstance(2, "Third");
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
            return "Page " + position;
        }
    }
}
