package com.samansaeedi.reddist.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by captain on 8/29/14.
 */
public class ReddistProvider extends ContentProvider {

    private static final int REDDIST = 100;
    private static final int REDDIST_WITH_SUBREDDIT = 101;
    private static final int REDDIST_WITH_SUBREDDIT_AND_SUBLIST = 102;
    private static final int REDDIST_WITH_FETCHDATE = 103;

    public static UriMatcher buildUriMatcher() {
        UriMatcher mch = new UriMatcher(UriMatcher.NO_MATCH);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST, REDDIST);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST + "/#",
                REDDIST_WITH_FETCHDATE);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST + "/r/*",
                REDDIST_WITH_SUBREDDIT);
        mch.addURI(ReddistContract.CONTENT_AUTHORITY, ReddistContract.PATH_REDDIST + "/r/*/*",
                REDDIST_WITH_SUBREDDIT_AND_SUBLIST);
        return mch;
    }

    public static final UriMatcher uriMatcher = buildUriMatcher();
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
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
