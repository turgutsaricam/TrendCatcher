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
public class DBAdapterTwitterUser {

    // Table name
    public static final String DATABASE_TABLE                       = "twitter_user";

    // DB Fields which is cliché
    public static final String KEY_ROWID                            = "_id";
    public static final int COL_ROWID                               = 0;

    /* TABLE FIELDS */
    // Field numbers
    public static final int COL_SCREEN_NAME                         = 1;
    public static final int COL_NAME                                = 2;
    public static final int COL_DESCRIPTION                         = 3;
    public static final int COL_LANG                                = 4;
    public static final int COL_PROFILE_LINK_COLOR                  = 5;
    public static final int COL_CREATED_AT                          = 6;
    public static final int COL_IS_VERIFIED                         = 7;
    public static final int COL_STATUSES_COUNT                      = 8;
    public static final int COL_FOLLOWERS_COUNT                     = 9;
    public static final int COL_FAVORITES_COUNT                     = 10;
    public static final int COL_FRIENDS_COUNT                       = 11;
    public static final int COL_LOCATION                            = 12;
    public static final int COL_TIMEZONE                            = 13;
    public static final int COL_UTC_OFFSET                          = 14;
    public static final int COL_IS_GEO_ENABLED                      = 15;
    public static final int COL_LAST_UPDATED                        = 16;

    // Field names
    public static final String KEY_SCREEN_NAME                      = "screen_name";
    public static final String KEY_NAME                             = "name";
    public static final String KEY_DESCRIPTION                      = "description";
    public static final String KEY_LANG                             = "lang";
    public static final String KEY_PROFILE_LINK_COLOR               = "profile_link_color";
    public static final String KEY_CREATED_AT                       = "created_at";
    public static final String KEY_IS_VERIFIED                      = "is_verified";
    public static final String KEY_STATUSES_COUNT                   = "statuses_count";
    public static final String KEY_FOLLOWERS_COUNT                  = "followers_count";
    public static final String KEY_FAVORITES_COUNT                  = "favorites_count";
    public static final String KEY_FRIENDS_COUNT                    = "friends_count";
    public static final String KEY_LOCATION                         = "location";
    public static final String KEY_TIMEZONE                         = "timezone";
    public static final String KEY_UTC_OFFSET                       = "utc_offset";
    public static final String KEY_IS_GEO_ENABLED                   = "is_geo_enabled";
    public static final String KEY_LAST_UPDATED                     = "last_updated";

    // All keys
    public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_SCREEN_NAME, KEY_NAME, KEY_DESCRIPTION,
            KEY_LANG, KEY_PROFILE_LINK_COLOR, KEY_CREATED_AT, KEY_IS_VERIFIED, KEY_STATUSES_COUNT, KEY_FOLLOWERS_COUNT,
            KEY_FAVORITES_COUNT, KEY_FRIENDS_COUNT, KEY_LOCATION, KEY_TIMEZONE, KEY_UTC_OFFSET, KEY_IS_GEO_ENABLED, KEY_LAST_UPDATED};

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

    public DBAdapterTwitterUser(Context context) {
        this.context = context;
    }

    public DBAdapterTwitterUser open() {
        mDBHelper = new DatabaseHelper(context);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    /* METHODS START HERE */

    public long insertRow(long twitter_user_id, String screen_name, String name, String description, String lang,
                          String profile_link_color, long created_at, int is_verified, int statuses_count, int followers_count,
                          int favorites_count, int friends_count, String location, String timezone, int utc_offset, int is_geo_enabled, long last_updated) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_ROWID, twitter_user_id);
        cv.put(KEY_SCREEN_NAME, screen_name);
        cv.put(KEY_NAME, name);
        cv.put(KEY_DESCRIPTION, description);
        cv.put(KEY_LANG, lang);
        cv.put(KEY_PROFILE_LINK_COLOR, profile_link_color);
        cv.put(KEY_CREATED_AT, created_at);
        cv.put(KEY_IS_VERIFIED, is_verified);
        cv.put(KEY_STATUSES_COUNT, statuses_count);
        cv.put(KEY_FOLLOWERS_COUNT, followers_count);
        cv.put(KEY_FAVORITES_COUNT, favorites_count);
        cv.put(KEY_FRIENDS_COUNT, friends_count);
        cv.put(KEY_LOCATION, location);
        cv.put(KEY_TIMEZONE, timezone);
        cv.put(KEY_UTC_OFFSET, utc_offset);
        cv.put(KEY_IS_GEO_ENABLED, is_geo_enabled);
        cv.put(KEY_LAST_UPDATED, last_updated);

        return mDB.insert(DATABASE_TABLE, null, cv);
    }

    public boolean updateRow(long twitter_user_id, String screen_name, String name, String description, String lang,
                          String profile_link_color, long created_at, int is_verified, int statuses_count, int followers_count,
                          int favorites_count, int friends_count, String location, String timezone, int utc_offset, int is_geo_enabled,
                             long last_updated) {

        String where = KEY_ROWID + "=" + twitter_user_id;

        ContentValues cv = new ContentValues();
        cv.put(KEY_SCREEN_NAME, screen_name);
        cv.put(KEY_NAME, name);
        cv.put(KEY_DESCRIPTION, description);
        cv.put(KEY_LANG, lang);
        cv.put(KEY_PROFILE_LINK_COLOR, profile_link_color);
        cv.put(KEY_CREATED_AT, created_at);
        cv.put(KEY_IS_VERIFIED, is_verified);
        cv.put(KEY_STATUSES_COUNT, statuses_count);
        cv.put(KEY_FOLLOWERS_COUNT, followers_count);
        cv.put(KEY_FAVORITES_COUNT, favorites_count);
        cv.put(KEY_FRIENDS_COUNT, friends_count);
        cv.put(KEY_LOCATION, location);
        cv.put(KEY_TIMEZONE, timezone);
        cv.put(KEY_UTC_OFFSET, utc_offset);
        cv.put(KEY_IS_GEO_ENABLED, is_geo_enabled);
        cv.put(KEY_LAST_UPDATED, last_updated);

        return mDB.update(DATABASE_TABLE, cv, where, null) != 0;
    }

    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = mDB.query(true, DATABASE_TABLE, ALL_KEYS, where, null, null, null, null, null);
        if(c != null) {
            c.moveToFirst();
        }

        return c;
    }

    public boolean isUserAlreadyAdded(long twitter_user_id) {
        String where = KEY_ROWID + "=" + twitter_user_id;
        Cursor c = mDB.query(true, DATABASE_TABLE, new String[] { KEY_ROWID }, where, null, null, null, null, null);

        return c.getCount() != 0;
    }
}
