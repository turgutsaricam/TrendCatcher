package com.turgutsaricam.trendcatcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Turgut on 07.03.2015.
 */
public class DBAdapterStreamSession {

    // Table name
    public static final String DATABASE_TABLE                       = "stream_session";

    // DB Fields which is cliché
    public static final String KEY_ROWID                            = "_id";
    public static final int COL_ROWID                               = 0;

    /* TABLE FIELDS */
    // Field numbers
    public static final int COL_TWEET_COUNT                         = 1;
    public static final int COL_USER_COUNT                          = 2;
    public static final int COL_IS_COMPLETED                        = 3;
    public static final int COL_TWEET_LIMIT                         = 4;
    public static final int COL_DURATION_LIMIT                      = 5;
    public static final int COL_LOC_LEFT_TOP_LAT                    = 6;
    public static final int COL_LOC_LEFT_TOP_LNG                    = 7;
    public static final int COL_LOC_RIGHT_TOP_LAT                   = 8;
    public static final int COL_LOC_RIGHT_TOP_LNG                   = 9;
    public static final int COL_LOC_RIGHT_BOTTOM_LAT                = 10;
    public static final int COL_LOC_RIGHT_BOTTOM_LNG                = 11;
    public static final int COL_LOC_LEFT_BOTTOM_LAT                 = 12;
    public static final int COL_LOC_LEFT_BOTTOM_LNG                 = 13;
    public static final int COL_STARTED_AT                          = 14;
    public static final int COL_FINISHED_AT                         = 15;
    public static final int COL_AREA                                = 16;

    // Field names
    public static final String KEY_TWEET_COUNT                      = "tweet_count";
    public static final String KEY_USER_COUNT                       = "user_count";
    public static final String KEY_IS_COMPLETED                     = "is_completed";
    public static final String KEY_TWEET_LIMIT                      = "tweet_limit";
    public static final String KEY_DURATION_LIMIT                   = "duration_limit";
    public static final String KEY_LOC_LEFT_TOP_LAT                 = "loc_left_top_lat";
    public static final String KEY_LOC_LEFT_TOP_LNG                 = "loc_left_top_lng";
    public static final String KEY_LOC_RIGHT_TOP_LAT                = "loc_right_top_lat";
    public static final String KEY_LOC_RIGHT_TOP_LNG                = "loc_right_top_lng";
    public static final String KEY_LOC_RIGHT_BOTTOM_LAT             = "loc_right_bottom_lat";
    public static final String KEY_LOC_RIGHT_BOTTOM_LNG             = "loc_right_bottom_lng";
    public static final String KEY_LOC_LEFT_BOTTOM_LAT              = "loc_left_bottom_lat";
    public static final String KEY_LOC_LEFT_BOTTOM_LNG              = "loc_left_bottom_lng";
    public static final String KEY_STARTED_AT                       = "started_at";
    public static final String KEY_FINISHED_AT                      = "finished_at";
    public static final String KEY_AREA                             = "area";

    // All keys
    public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_TWEET_COUNT, KEY_USER_COUNT, KEY_IS_COMPLETED, KEY_TWEET_LIMIT,
            KEY_DURATION_LIMIT, KEY_LOC_LEFT_TOP_LAT, KEY_LOC_LEFT_TOP_LNG, KEY_LOC_RIGHT_TOP_LAT, KEY_LOC_RIGHT_TOP_LNG,
            KEY_LOC_RIGHT_BOTTOM_LAT, KEY_LOC_RIGHT_BOTTOM_LNG, KEY_LOC_LEFT_BOTTOM_LAT, KEY_LOC_LEFT_BOTTOM_LNG, KEY_STARTED_AT,
            KEY_FINISHED_AT, KEY_AREA};

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

    public DBAdapterStreamSession(Context context) {
        this.context = context;
    }

    public DBAdapterStreamSession open() {
        mDBHelper = new DatabaseHelper(context);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    /* METHODS START HERE */

    /**
     *
     * @param tweet_count Number of tweets in the stream
     * @param user_count Number of users tweeted in the stream
     * @param is_completed 1 if true, 0 if false
     * @param tweet_limit Limit for tweet count set by the user
     * @param duration_limit Limit for streaming duration set by the user
     * @param leftTop Left top corner location of the selected area
     * @param rightTop
     * @param rightBottom
     * @param leftBottom
     * @param started_at Milliseconds
     * @param finished_at Milliseconds
     * @param area in meter squared
     * @return ID of added row or -1 if an error occurs
     */
    public long insertRow(int tweet_count, int user_count, int is_completed, int tweet_limit, long duration_limit,
                          LatLng leftTop, LatLng rightTop, LatLng rightBottom, LatLng leftBottom, long started_at,
                          long finished_at, double area) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_TWEET_COUNT, tweet_count);
        cv.put(KEY_USER_COUNT, user_count);
        cv.put(KEY_IS_COMPLETED, is_completed);
        cv.put(KEY_TWEET_LIMIT, tweet_limit);
        cv.put(KEY_DURATION_LIMIT, duration_limit);
        cv.put(KEY_LOC_LEFT_TOP_LAT, leftTop.latitude);
        cv.put(KEY_LOC_LEFT_TOP_LNG, leftTop.longitude);
        cv.put(KEY_LOC_RIGHT_TOP_LAT, rightTop.latitude);
        cv.put(KEY_LOC_RIGHT_TOP_LNG, rightTop.longitude);
        cv.put(KEY_LOC_RIGHT_BOTTOM_LAT, rightBottom.latitude);
        cv.put(KEY_LOC_RIGHT_BOTTOM_LNG, rightBottom.longitude);
        cv.put(KEY_LOC_LEFT_BOTTOM_LAT, leftBottom.latitude);
        cv.put(KEY_LOC_LEFT_BOTTOM_LNG, leftBottom.longitude);
        cv.put(KEY_STARTED_AT, started_at);
        cv.put(KEY_FINISHED_AT, finished_at);
        cv.put(KEY_AREA, area);

        return mDB.insert(DATABASE_TABLE, null, cv);
    }

    public long getLastRowID() {
        String[] rowId = new String[] { KEY_ROWID };
        Cursor c = mDB.query(true, DATABASE_TABLE, rowId, null, null, null, null, null, null);

        c.moveToLast();
        long lastRowId = c.getLong(COL_ROWID);
        c.close();

        return lastRowId;
    }

    public boolean updateUserCount(long rowId, int user_count) {
        String where = KEY_ROWID + "=" + rowId;

        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_COUNT, user_count);

        return mDB.update(DATABASE_TABLE, cv, where, null) != 0;
    }
}
