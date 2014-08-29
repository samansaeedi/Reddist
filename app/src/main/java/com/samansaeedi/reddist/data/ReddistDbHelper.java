package com.samansaeedi.reddist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.samansaeedi.reddist.data.ReddistContract.ReddistEntry;

/**
 * Created by captain on 8/29/14.
 */
public class ReddistDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "reddist.db";

    public ReddistDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_WEATHER_TABLE = "create table " + ReddistEntry.TABLE_NAME + " (" +
                ReddistEntry._ID + " integer primary key autoincrement," +
                ReddistEntry.COLUMN_AUTHOR + " text not null," +
                ReddistEntry.COLUMN_REDDIT_ID + " text not null," +
                ReddistEntry.COLUMN_SUBREDDIT + " text not null," +
                ReddistEntry.COLUMN_REDDIT_SUBLIST + " text not null," +
                ReddistEntry.COLUMN_SCORE + " integer not null," +
                ReddistEntry.COLUMN_UPS + " integer not null," +
                ReddistEntry.COLUMN_DOWNS + " integer not null," +
                ReddistEntry.COLUMN_TITLE + " text not null," +
                ReddistEntry.COLUMN_CREATED_UTC + " integer not null," +
                ReddistEntry.COLUMN_FETCHED + " integer not null," +
                ReddistEntry.COLUMN_LINK + " text not null," +
                ReddistEntry.COLUMN_URL + " text not null," +
                ReddistEntry.COLUMN_NUM_COMMENTS + " integer not null," +
                ReddistEntry.COLUMN_DOMAIN + " text," +
                ReddistEntry.COLUMN_CONTENT_TEXT + " text," +
                ReddistEntry.COLUMN_CONTENT_HTML + " text," +
                ReddistEntry.COLUMN_THUMBNAIL + " text," +

                " unique (" + ReddistEntry.COLUMN_REDDIT_ID + "," +
                ReddistEntry.COLUMN_SUBREDDIT+ "," + ReddistEntry.COLUMN_REDDIT_SUBLIST +
                ") on conflict replace);";

        db.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + ReddistEntry.TABLE_NAME);
        onCreate(db);
    }
}
