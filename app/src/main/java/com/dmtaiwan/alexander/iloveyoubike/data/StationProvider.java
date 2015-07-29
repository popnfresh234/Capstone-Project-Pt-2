package com.dmtaiwan.alexander.iloveyoubike.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Alexander on 7/28/2015.
 */
public class StationProvider extends ContentProvider {

    //Database
    private StationDbHelper mDatabaseHelper;

    //URI query for all station
    //content://com.dmtaiwan.alexander.iloveyoubike/station
    static final int STATION = 100;

    //URI query for station with station_id
    //content://com.dmtaiwan.alexander.iloveyoubike/station/[STATION_ID]
    static final int STATION_WITH_ID = 101;

    private static final String sStationWithId = StationContract.StationEntry.TABLE_NAME + "." + StationContract.StationEntry.COLUMN_STATION_ID + " = ?";

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StationContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, StationContract.PATH_STATION, STATION);
        matcher.addURI(authority, StationContract.PATH_STATION + "/#/", STATION_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new StationDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case STATION:
                retCursor = mDatabaseHelper.getReadableDatabase().query(
                        StationContract.StationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case STATION_WITH_ID:
                retCursor = getStationById(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getStationById(Uri uri, String[] projection, String sortOrder) {
        int id = StationContract.StationEntry.getIdFromUri(uri);
        return mDatabaseHelper.getReadableDatabase().query(
                StationContract.StationEntry.TABLE_NAME,
                projection,
                sStationWithId,
                new String[]{Integer.toString(id)},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STATION:
                return StationContract.StationEntry.CONTENT_TYPE;
            case STATION_WITH_ID:
                return StationContract.StationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Uri returnUri;

        switch (uriType) {
            case STATION:
                long id = db.insert(StationContract.StationEntry.TABLE_NAME, null, values);
                if(id >0) {
                    returnUri = StationContract.StationEntry.buildUriStation(id);
                }else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabaseHelper.getWritableDatabase();
        int rowsDeleted;
        if ( null == selection ) selection = "1";
        switch (uriType) {
            case STATION:
                rowsDeleted = sqlDB.delete(StationContract.StationEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDatabaseHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case STATION:
                rowsUpdated = sqlDB.update(StationContract.StationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STATION:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(StationContract.StationEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
