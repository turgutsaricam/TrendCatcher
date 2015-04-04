package com.turgutsaricam.trendcatcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Turgut on 07.03.2015.
 */
public class DBAdapterTweet {

    // Table name
    public static final String DATABASE_TABLE                   = "tweet";

    // DB Fields which is cliché
    public static final String KEY_ROWID                        = "_id";
    public static final int COL_ROWID                           = 0;

    /* TABLE FIELDS */
    // Field numbers
    public static final int COL_STREAM_SESSION_ID               = 1;
    public static final int COL_USER_ID                         = 2;
    public static final int COL_TWEET_ID                        = 3;
    public static final int COL_TWEET_TEXT                      = 4;
    public static final int COL_CREATED_AT                      = 5;
    public static final int COL_LANG                            = 6;
    public static final int COL_RETWEET_COUNT                   = 7;
    public static final int COL_IN_REPLY_TO_SCREEN_NAME         = 8;
    public static final int COL_IN_REPLY_TO_STATUS_ID           = 9;
    public static final int COL_IN_REPLY_TO_USER_ID             = 10;
    public static final int COL_LOC_ID                          = 11;
    public static final int COL_MEDIA_COUNT                     = 12;
    public static final int COL_IS_SENSITIVE                    = 13;
    public static final int COL_LATITUDE                        = 14;
    public static final int COL_LONGITUDE                       = 15;

    // Field names
    public static final String KEY_STREAM_SESSION_ID            = "stream_session_id";
    public static final String KEY_USER_ID                      = "user_id";
    public static final String KEY_TWEET_ID                     = "tweet_id";
    public static final String KEY_TWEET_TEXT                   = "tweet_text";
    public static final String KEY_CREATED_AT                   = "created_at";
    public static final String KEY_LANG                         = "lang";
    public static final String KEY_RETWEET_COUNT                = "retweet_count";
    public static final String KEY_IN_REPLY_TO_SCREEN_NAME      = "in_reply_to_screen_name";
    public static final String KEY_IN_REPLY_TO_STATUS_ID        = "in_reply_to_status_id";
    public static final String KEY_IN_REPLY_TO_USER_ID          = "in_reply_to_user_id";
    public static final String KEY_LOC_ID                       = "loc_id";
    public static final String KEY_MEDIA_COUNT                  = "media_count";
    public static final String KEY_IS_SENSITIVE                 = "is_sensitive";
    public static final String KEY_LATITUDE                     = "latitude";
    public static final String KEY_LONGITUDE                    = "longitude";

    // All keys
    public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_STREAM_SESSION_ID, KEY_USER_ID, KEY_TWEET_ID, KEY_TWEET_TEXT,
            KEY_CREATED_AT, KEY_LANG, KEY_RETWEET_COUNT, KEY_IN_REPLY_TO_SCREEN_NAME, KEY_IN_REPLY_TO_STATUS_ID, KEY_IN_REPLY_TO_USER_ID,
            KEY_LOC_ID, KEY_MEDIA_COUNT, KEY_IS_SENSITIVE, KEY_LATITUDE, KEY_LONGITUDE};

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

    public DBAdapterTweet(Context context) {
        this.context = context;
    }

    public DBAdapterTweet open() {
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
     * @param stream_session_id
     * @param user_id
     * @param tweet_id
     * @param tweet_text
     * @param created_at
     * @param lang
     * @param retweet_count
     * @param in_reply_to_screen_name
     * @param in_reply_to_status_id
     * @param in_reply_to_user_id
     * @param loc_id Location ID retread from Tweet
     * @param media_count Photo/Video count in the tweet
     * @param is_sensitive
     * @return ID of new row or -1 if an error occurs
     */
    public long insertRow(long stream_session_id, long user_id, long tweet_id, String tweet_text, long created_at,
                          String lang, int retweet_count, String in_reply_to_screen_name, long in_reply_to_status_id,
                          long in_reply_to_user_id, String loc_id, int media_count, int is_sensitive,
                          double latitude, double longitude) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_STREAM_SESSION_ID, stream_session_id);
        cv.put(KEY_USER_ID, user_id);
        cv.put(KEY_TWEET_ID, tweet_id);
        cv.put(KEY_TWEET_TEXT, tweet_text);
        cv.put(KEY_CREATED_AT, created_at);
        cv.put(KEY_LANG, lang);
        cv.put(KEY_RETWEET_COUNT, retweet_count);
        cv.put(KEY_IN_REPLY_TO_SCREEN_NAME, in_reply_to_screen_name);
        cv.put(KEY_IN_REPLY_TO_STATUS_ID, in_reply_to_status_id);
        cv.put(KEY_IN_REPLY_TO_USER_ID, in_reply_to_user_id);
        cv.put(KEY_LOC_ID, loc_id);
        cv.put(KEY_MEDIA_COUNT, media_count);
        cv.put(KEY_IS_SENSITIVE, is_sensitive);
        cv.put(KEY_LATITUDE, latitude);
        cv.put(KEY_LONGITUDE, longitude);

        return mDB.insert(DATABASE_TABLE, null, cv);
    }

    public int getTotalTweetCount() {
        Cursor c = mDB.query(true, "sqlite_sequence", new String[] {"name", "seq"}, "name='" + DATABASE_TABLE + "'",
                null, null, null, null, null);
        if(c != null) {
            c.moveToFirst();
            int count = c.getInt(1);
            c.close();
            return count;
        }

        return 0;
    }
}
