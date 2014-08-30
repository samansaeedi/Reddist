package com.samansaeedi.reddist.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.samansaeedi.reddist.data.ReddistContract.ReddistEntry;

/**
 * Created by captain on 8/29/14.
 */
public class ReddistProvider extends ContentProvider {

    private static final int REDDIST = 100;
    private static final int REDDIST_WITH_SUBLIST = 101;
    private static final int REDDIST_WITH_SUBREDDIT_AND_SUBLIST = 102;
    private static final int REDDIST_WITH_SUBLIST_AND_ID = 103;
    private static final int REDDIST_WITH_SUBREDDIT_AND_SUBLIST_AND_ID = 104;
    private static final int REDDIST_WITH_FETCHDATE = 105;

    public static UriMatcher buildUriMatcher() {
        UriMatcher mch = new UriMatcher(UriMatcher.NO_MATCH);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST, REDDIST);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST + "/d/#",
                REDDIST_WITH_FETCHDATE);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST + "/*",
                REDDIST_WITH_SUBLIST);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST + "/*/#",
                REDDIST_WITH_SUBLIST_AND_ID);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST + "/r/*/*",
                REDDIST_WITH_SUBREDDIT_AND_SUBLIST);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST + "/r/*/*/#",
                REDDIST_WITH_SUBREDDIT_AND_SUBLIST_AND_ID);
        return mch;
    }

    public static final UriMatcher uriMatcher = buildUriMatcher();

    private static final String selectionSublist =
            ReddistEntry.COLUMN_REDDIT_SUBREDDIT + " = \'-\' and " +
            ReddistEntry.COLUMN_REDDIT_SUBLIST + " = ?";
    private static final String selectionSubredditAndSublist =
            ReddistEntry.COLUMN_REDDIT_SUBREDDIT + " = ? and " +
            ReddistEntry.COLUMN_REDDIT_SUBLIST + " = ?";
    private static final String selectionSublistAndID =
            ReddistEntry.COLUMN_REDDIT_SUBREDDIT + " = \'-\' and " +
                    ReddistEntry.COLUMN_REDDIT_SUBLIST + " = ? and " +
                    ReddistEntry.COLUMN_REDDIT_ID + " = ?";
    private static final String selectionSubredditAndSublistAndID =
            ReddistEntry.COLUMN_REDDIT_SUBREDDIT + " = ? and " +
                    ReddistEntry.COLUMN_REDDIT_SUBLIST + " = ? and " +
                    ReddistEntry.COLUMN_REDDIT_ID + " = ?";
    private static final String selectionfetchDate = ReddistEntry.COLUMN_FETCHED + " <= ?";


    private ReddistDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new ReddistDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch(uriMatcher.match(uri)){
            case REDDIST:
            {
                cursor = dbHelper.getReadableDatabase().query(
                        ReddistContract.ReddistEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case REDDIST_WITH_FETCHDATE:
            {
                cursor = dbHelper.getReadableDatabase().query(
                        ReddistContract.ReddistEntry.TABLE_NAME,
                        projection,
                        selectionfetchDate,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case REDDIST_WITH_SUBLIST:
            {
                cursor = dbHelper.getReadableDatabase().query(
                        ReddistContract.ReddistEntry.TABLE_NAME,
                        projection,
                        selectionSublist,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case REDDIST_WITH_SUBREDDIT_AND_SUBLIST:
            {
                cursor = dbHelper.getReadableDatabase().query(
                        ReddistContract.ReddistEntry.TABLE_NAME,
                        projection,
                        selectionSubredditAndSublist,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case REDDIST_WITH_SUBLIST_AND_ID:
            {
                cursor = dbHelper.getReadableDatabase().query(
                        ReddistContract.ReddistEntry.TABLE_NAME,
                        projection,
                        selectionSublistAndID,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case REDDIST_WITH_SUBREDDIT_AND_SUBLIST_AND_ID:
            {
                cursor = dbHelper.getReadableDatabase().query(
                        ReddistContract.ReddistEntry.TABLE_NAME,
                        projection,
                        selectionSubredditAndSublistAndID,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case REDDIST:
            case REDDIST_WITH_SUBLIST:
            case REDDIST_WITH_FETCHDATE:
            case REDDIST_WITH_SUBREDDIT_AND_SUBLIST:
                return ReddistEntry.CONTENT_TYPE;
            case REDDIST_WITH_SUBLIST_AND_ID:
            case REDDIST_WITH_SUBREDDIT_AND_SUBLIST_AND_ID:
                return ReddistEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);

        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int mch = uriMatcher.match(uri);
        Uri returnUri;
        switch (mch){
            case REDDIST:{
                long id = dbHelper.getWritableDatabase()
                        .insert(ReddistEntry.TABLE_NAME, null, values);
                if(id > 0)
                    returnUri = ReddistEntry.buildReddistUri(id);
                else throw new SQLException("failed to insert into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(returnUri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int mch = uriMatcher.match(uri);
        int rows;
        switch (mch){
            case REDDIST:{
                rows = dbHelper.getWritableDatabase().delete(ReddistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rows > 0 || selection == null)
            getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int mch = uriMatcher.match(uri);
        int rows;
        switch (mch){
            case REDDIST:{
                rows = dbHelper.getWritableDatabase().update(ReddistEntry.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rows > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int mch = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (mch){
            case REDDIST:
            {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ReddistEntry.TABLE_NAME, null, value);
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
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
