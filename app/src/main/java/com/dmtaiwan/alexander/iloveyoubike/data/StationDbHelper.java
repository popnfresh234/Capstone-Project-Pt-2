package com.dmtaiwan.alexander.iloveyoubike.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dmtaiwan.alexander.iloveyoubike.data.StationContract.StationEntry;

/**
 * Created by Alexander on 7/28/2015.
 */
public class StationDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "station.db";

    public StationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_STATION_TABLE = "CREATE TABLE " + StationEntry.TABLE_NAME + " (" +
                StationEntry._ID + " INTEGER PRIMARY KEY," +
                StationEntry.COLUMN_STATION_ID + " INTEGER NOT NULL, " +
                StationEntry.COLUMN_STATION_NAME_ZH + " TEXT NOT NULL, " +
                StationEntry.COLUMN_STATION_DISTRICT_ZH + " TEXT NOT NULL, " +
                StationEntry.COLUMN_STATION_NAME_EN + " TEXT NOT NULL, " +
                StationEntry.COLUMN_STATION_DISTRICT_EN + " TEXT NOT NULL, " +
                StationEntry.COLUMN_STATION_LAT + " REAL NOT NULL, " +
                StationEntry.COLUMN_STATION_LONG + " REAL NOT NULL, " +
                StationEntry.COLUMN_BIKES_AVAIABLE + " INTEGER NOT NULL, " +
                StationEntry.COLUMN_SPACES_AVAILABLE + " INTEGER NOT NULL, " +
                StationEntry.COLUMN_LAST_UPDATED + " TEXT NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_STATION_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StationEntry.TABLE_NAME);
        onCreate(db);
    }
}
