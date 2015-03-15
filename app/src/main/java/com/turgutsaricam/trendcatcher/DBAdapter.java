package com.turgutsaricam.trendcatcher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Turgut on 07.03.2015.
 */
public class DBAdapter {

    public static final String DATABASE_NAME = "trendcatcher.db";
    public static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_STREAM_SESSION =
            "create table " + DBAdapterStreamSession.DATABASE_TABLE + " ("
                    + DBAdapterStreamSession.KEY_ROWID +                    " integer primary key autoincrement, "
                    + DBAdapterStreamSession.KEY_TWEET_COUNT +              " integer, "
                    + DBAdapterStreamSession.KEY_USER_COUNT +               " integer, "
                    + DBAdapterStreamSession.KEY_IS_COMPLETED +             " integer, "
                    + DBAdapterStreamSession.KEY_TWEET_LIMIT +              " integer, "
                    + DBAdapterStreamSession.KEY_DURATION_LIMIT +           " integer, "
                    + DBAdapterStreamSession.KEY_LOC_LEFT_TOP_LAT +         " real, "
                    + DBAdapterStreamSession.KEY_LOC_LEFT_TOP_LNG +         " real, "
                    + DBAdapterStreamSession.KEY_LOC_RIGHT_TOP_LAT +        " real, "
                    + DBAdapterStreamSession.KEY_LOC_RIGHT_TOP_LNG +        " real, "
                    + DBAdapterStreamSession.KEY_LOC_RIGHT_BOTTOM_LAT +     " real, "
                    + DBAdapterStreamSession.KEY_LOC_RIGHT_BOTTOM_LNG +     " real, "
                    + DBAdapterStreamSession.KEY_LOC_LEFT_BOTTOM_LAT +      " real, "
                    + DBAdapterStreamSession.KEY_LOC_LEFT_BOTTOM_LNG +      " real, "
                    + DBAdapterStreamSession.KEY_STARTED_AT +               " integer, "
                    + DBAdapterStreamSession.KEY_FINISHED_AT +              " integer, "
                    + DBAdapterStreamSession.KEY_AREA +                     " real"
            + ");";

    private static final String CREATE_TABLE_TWEET =
            "create table " + DBAdapterTweet.DATABASE_TABLE + " ("
                    + DBAdapterTweet.KEY_ROWID +                    " integer primary key autoincrement, "
                    + DBAdapterTweet.KEY_STREAM_SESSION_ID +        " integer not null, "
                    + DBAdapterTweet.KEY_USER_ID +                  " integer, "
                    + DBAdapterTweet.KEY_TWEET_ID +                 " integer, "
                    + DBAdapterTweet.KEY_TWEET_TEXT +               " text, "
                    + DBAdapterTweet.KEY_CREATED_AT +               " integer, "
                    + DBAdapterTweet.KEY_LANG +                     " text, "
                    + DBAdapterTweet.KEY_RETWEET_COUNT +            " integer, "
                    + DBAdapterTweet.KEY_IN_REPLY_TO_SCREEN_NAME +  " text, "
                    + DBAdapterTweet.KEY_IN_REPLY_TO_STATUS_ID +    " integer, "
                    + DBAdapterTweet.KEY_IN_REPLY_TO_USER_ID +      " integer, "
                    + DBAdapterTweet.KEY_LOC_ID +                   " text, "
                    + DBAdapterTweet.KEY_MEDIA_COUNT +              " integer, "
                    + DBAdapterTweet.KEY_IS_SENSITIVE +             " integer"
            + ");";

    private static final String CREATE_TABLE_TWITTER_USER =
            "create table " + DBAdapterTwitterUser.DATABASE_TABLE + " ("
                    + DBAdapterTwitterUser.KEY_ROWID +                      " integer not null, "
                    + DBAdapterTwitterUser.KEY_SCREEN_NAME +                " text, "
                    + DBAdapterTwitterUser.KEY_NAME +                       " text, "
                    + DBAdapterTwitterUser.KEY_DESCRIPTION +                " text, "
                    + DBAdapterTwitterUser.KEY_LANG +                       " text, "
                    + DBAdapterTwitterUser.KEY_PROFILE_LINK_COLOR +         " text, "
                    + DBAdapterTwitterUser.KEY_CREATED_AT +                 " integer, "
                    + DBAdapterTwitterUser.KEY_IS_VERIFIED +                " integer, "
                    + DBAdapterTwitterUser.KEY_STATUSES_COUNT +             " integer, "
                    + DBAdapterTwitterUser.KEY_FOLLOWERS_COUNT +            " integer, "
                    + DBAdapterTwitterUser.KEY_FAVORITES_COUNT +            " integer, "
                    + DBAdapterTwitterUser.KEY_FRIENDS_COUNT +              " integer, "
                    + DBAdapterTwitterUser.KEY_LOCATION +                   " text, "
                    + DBAdapterTwitterUser.KEY_TIMEZONE +                   " text, "
                    + DBAdapterTwitterUser.KEY_UTC_OFFSET +                 " integer, "
                    + DBAdapterTwitterUser.KEY_IS_GEO_ENABLED +             " integer, "
                    + DBAdapterTwitterUser.KEY_LAST_UPDATED +               " integer, "
                    + "PRIMARY KEY (" + DBAdapterTwitterUser.KEY_ROWID + ")"
            + ");";

    private static final String CREATE_TABLE_STREAM_SESSION_KEYWORD =
            "create table " + DBAdapterStreamSessionKeyword.DATABASE_TABLE + " ("
                    + DBAdapterStreamSessionKeyword.KEY_ROWID +                      " integer primary key autoincrement, "
                    + DBAdapterStreamSessionKeyword.KEY_STREAM_SESSION_ID +          " integer, "
                    + DBAdapterStreamSessionKeyword.KEY_KEYWORD +                    " text "
            + ");";

    private static final String CREATE_TABLE_TWITTER_LOCATION =
            "create table " + DBAdapterTwitterLocation.DATABASE_TABLE + " ("
                    + DBAdapterTwitterLocation.KEY_ROWID +                      " integer primary key autoincrement, "
                    + DBAdapterTwitterLocation.KEY_LOCATION_ID +                " text not null, "
                    + DBAdapterTwitterLocation.KEY_NAME +                       " text, "
                    + DBAdapterTwitterLocation.KEY_LATITUDE +                   " real, "
                    + DBAdapterTwitterLocation.KEY_LONGITUDE +                  " real, "
                    + DBAdapterTwitterLocation.KEY_COUNTRY +                    " text, "
                    + DBAdapterTwitterLocation.KEY_COUNTRY_CODE +               " text, "
                    + DBAdapterTwitterLocation.KEY_FULL_NAME +                  " text, "
                    + DBAdapterTwitterLocation.KEY_STREET_ADDRESS +             " text, "
                    + DBAdapterTwitterLocation.KEY_PLACE_TYPE +                 " text "
            + ");";

    private static final String CREATE_TABLE_TWEET_MEDIA =
            "create table " + DBAdapterTweetMedia.DATABASE_TABLE + " ("
                    + DBAdapterTweetMedia.KEY_ROWID +                         " integer primary key autoincrement, "
                    + DBAdapterTweetMedia.KEY_USER_ID +                       " integer, "
                    + DBAdapterTweetMedia.KEY_TWEET_ID +                      " integer, "
                    + DBAdapterTweetMedia.KEY_TYPE +                          " text, "
                    + DBAdapterTweetMedia.KEY_CONTENT +                       " text "
            + ");";

    private Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        myDBHelper.close();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_STREAM_SESSION);
            db.execSQL(CREATE_TABLE_TWEET);
            db.execSQL(CREATE_TABLE_TWITTER_USER);
            db.execSQL(CREATE_TABLE_STREAM_SESSION_KEYWORD);
            db.execSQL(CREATE_TABLE_TWITTER_LOCATION);
            db.execSQL(CREATE_TABLE_TWEET_MEDIA);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}
