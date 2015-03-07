package com.turgutsaricam.trendcatcher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Turgut on 07.03.2015.
 */
public class DBAdapterTweets {

    // Table name
    public static final String DATABASE_TABLE = "tweets";

    // DB Fields which is cliché)
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;

    /* TABLE FIELDS */
    // Field numbers
    public static final int COL_TWEET_RAW = 1;

    // Field names
    public static final String KEY_TWEET_RAW = "tweet_raw";

    // All keys
    public static final String[] ALL_KEYS = new String[] { KEY_ROWID, KEY_TWEET_RAW };

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

    public DBAdapterTweets(Context context) {
        this.context = context;
    }

    public DBAdapterTweets open() {
        mDBHelper = new DatabaseHelper(context);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }
}
