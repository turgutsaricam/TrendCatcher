package com.turgutsaricam.trendcatcher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Turgut on 07.03.2015.
 */
public class DBAdapterTweetMedia {

    // Table name
    public static final String DATABASE_TABLE                   = "tweet_media";

    // DB Fields which is cliché
    public static final String KEY_ROWID                        = "_id";
    public static final int COL_ROWID                           = 0;

    /* TABLE FIELDS */
    // Field numbers
    public static final int COL_USER_ID                         = 1;
    public static final int COL_TWEET_ID                        = 2;
    public static final int COL_TYPE                            = 3;
    public static final int COL_CONTENT                         = 4;

    // Field names
    public static final String KEY_USER_ID                      = "user_id";
    public static final String KEY_TWEET_ID                     = "tweet_id";
    public static final String KEY_TYPE                         = "type";
    public static final String KEY_CONTENT                      = "content";

    // All keys
    public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_USER_ID, KEY_TWEET_ID, KEY_TYPE, KEY_CONTENT};

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

    public DBAdapterTweetMedia(Context context) {
        this.context = context;
    }

    public DBAdapterTweetMedia open() {
        mDBHelper = new DatabaseHelper(context);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }

    /* METHODS START HERE */
}
