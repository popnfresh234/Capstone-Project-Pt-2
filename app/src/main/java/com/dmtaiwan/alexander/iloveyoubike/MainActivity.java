package com.dmtaiwan.alexander.iloveyoubike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dmtaiwan.alexander.iloveyoubike.Sync.IloveyoubikeSyncAdapter;


public class MainActivity extends AppCompatActivity{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize SyncAdapter
        IloveyoubikeSyncAdapter.initializeSyncAdapter(this);
    }

    public void onListItemClick(int position) {
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                Intent intent = new Intent(this, StationListActivity.class);
                startActivity(intent);
                break;
            case 3:
                break;
            default:
                break;
        }
    }


}
