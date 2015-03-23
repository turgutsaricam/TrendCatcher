package com.turgutsaricam.trendcatcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Turgut on 07.03.2015.
 */
public class DBAdapterTwitterLocation {

    // Table name
    public static final String DATABASE_TABLE                   = "twitter_location";

    // DB Fields which is cliché
    public static final String KEY_ROWID                        = "_id";
    public static final int COL_ROWID                           = 0;

    /* TABLE FIELDS */
    // Field numbers
    public static final int COL_LOCATION_ID                     = 1;
    public static final int COL_NAME                            = 2;
    public static final int COL_LATITUDE                        = 3;
    public static final int COL_LONGITUDE                       = 4;
    public static final int COL_COUNTRY                         = 5;
    public static final int COL_COUNTRY_CODE                    = 6;
    public static final int COL_FULL_NAME                       = 7;
    public static final int COL_STREET_ADDRESS                  = 8;
    public static final int COL_PLACE_TYPE                      = 9;

    // Field names
    public static final String KEY_LOCATION_ID                  = "location_id";
    public static final String KEY_NAME                         = "name";
    public static final String KEY_LATITUDE                     = "latitude";
    public static final String KEY_LONGITUDE                    = "longitude";
    public static final String KEY_COUNTRY                      = "country";
    public static final String KEY_COUNTRY_CODE                 = "country_code";
    public static final String KEY_FULL_NAME                    = "full_name";
    public static final String KEY_STREET_ADDRESS               = "street_address";
    public static final String KEY_PLACE_TYPE                   = "place_type";

    // All keys
    public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_LOCATION_ID, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE,
            KEY_COUNTRY, KEY_COUNTRY_CODE, KEY_FULL_NAME, KEY_STREET_ADDRESS, KEY_PLACE_TYPE};

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DBAdapter.DATABASE_NAME, null, DBAdapter.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDB;
    private final Context context;

    public DBAdapterTwitterLocation(Context context) {
        this.context = context;
    }

    public DBAdapterTwitterLocation open() {
        mDBHelper = new DatabaseHelper(context);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    /* METHODS START HERE */

    public long insertRow(String location_id, String name, double latitude, double longitude, String country,
                          String country_code, String full_name, String street_address, String place_type) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_LOCATION_ID, location_id);
        cv.put(KEY_NAME, name);
        cv.put(KEY_LATITUDE, latitude);
        cv.put(KEY_LONGITUDE, longitude);
        cv.put(KEY_COUNTRY, country);
        cv.put(KEY_COUNTRY_CODE, country_code);
        cv.put(KEY_FULL_NAME, full_name);
        cv.put(KEY_STREET_ADDRESS, street_address);
        cv.put(KEY_PLACE_TYPE, place_type);

        return mDB.insert(DATABASE_TABLE, null, cv);
    }

    public boolean isLocationAdded(String locationId) {
        String where = KEY_LOCATION_ID + "='" + locationId + "'";
        Cursor c = mDB.query(true, DATABASE_TABLE, new String[] { KEY_LOCATION_ID }, where, null, null, null, null, null);

        return c.getCount() != 0;
    }
}
