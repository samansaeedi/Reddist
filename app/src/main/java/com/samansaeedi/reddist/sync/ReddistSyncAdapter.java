package com.samansaeedi.reddist.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.samansaeedi.reddist.MainActivity;
import com.samansaeedi.reddist.R;
import com.samansaeedi.reddist.Utility;
import com.samansaeedi.reddist.data.ReddistContract.ReddistEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Created by captain on 8/29/14.
 */
public class ReddistSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ReddistSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 7 * 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long NOTIFICATION_INTERVAL_MS = 6 * 60 * 60 * 1000;
    public static final int REDDIST_NOTIFICATION_ID = 38734828;



    public ReddistSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

            /*
            * Add the account and account type, no password or user data
            * If successful, return the Account object, otherwise report an error.
            */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
            * If you don't set android:syncable="true" in
            * in your <provider> element in the manifest,
            * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
            * here.
            */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        ReddistSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(LOG_TAG, "onPerformSync");
        String sublistPreference = Utility.getPreferredSublist(getContext());
        String subredditPreference = Utility.getPreferredSubreddit(getContext());
        Integer numberOfItemsPreference = Utility.getPreferredNumberOfListItems(getContext());

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String returnedJsonStr = null;

        String REDDIT_BASE_URL = "http://www.reddit.com";

        try {
            Uri.Builder uriBuilder = Uri.parse(REDDIT_BASE_URL).buildUpon();
            Uri builtUri;
            if(subredditPreference.equalsIgnoreCase("-"))
                uriBuilder = uriBuilder.appendPath(sublistPreference + ".json")
                        .appendQueryParameter("limit", String.valueOf(numberOfItemsPreference));
            else
                uriBuilder = uriBuilder.appendPath("r")
                        .appendPath(subredditPreference)
                        .appendPath(sublistPreference + ".json")
                        .appendQueryParameter("limit", String.valueOf(numberOfItemsPreference));
            builtUri = uriBuilder.build();

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return;
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return;

            returnedJsonStr = buffer.toString();
            Log.i(LOG_TAG, returnedJsonStr);
        } catch (java.io.IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            getReddistDataFromJson(returnedJsonStr, sublistPreference, subredditPreference,
                    numberOfItemsPreference);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void getReddistDataFromJson(String redditJsonString, String sublist, String subreddit,
                                       int numberOfItems) throws JSONException {
        Log.i(LOG_TAG, "getReddistFromJson");
        JSONArray redditArray = new JSONObject(redditJsonString).getJSONObject("data")
                .getJSONArray("children");
        Vector<ContentValues> redditVector = new Vector<ContentValues>();
        long fetched = new Date().getTime();
        try {
            for (int i = 0; i < redditArray.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject data = redditArray.getJSONObject(i).getJSONObject("data");
                for (Field f : ReddistEntry.class.getDeclaredFields()) {
                    if (f.getName().startsWith("COLUMN_REDDIT"))
                        values.put((String) f.get(null), data.getString((String) f.get(null)));
                }
                values.put(ReddistEntry.COLUMN_FETCHED, fetched);
                values.put(ReddistEntry.COLUMN_SUBREDDIT, subreddit);
                values.put(ReddistEntry.COLUMN_SUBLIST, sublist);
                redditVector.add(values);
            }
        }
        catch (IllegalAccessException e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }
        ContentValues[] values = new ContentValues[redditVector.size()];
        redditVector.toArray(values);
        getContext().getContentResolver().delete(ReddistEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().bulkInsert(
                ReddistEntry.buildReddistWithSubredditAndSublist(subreddit, sublist), values);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
//        getContext().getContentResolver().delete(ReddistEntry.CONTENT_URI,
//                ReddistEntry.COLUMN_FETCHED + " <= ?",
//                new String[] {String.valueOf(cal.getTimeInMillis())});
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        Log.i(LOG_TAG, "notifications: " + String.valueOf(prefs.getBoolean("notifications", false)));
        if(prefs.getBoolean("notifications", false))
            notifyUser(subreddit);
    }
    private void notifyUser(String subreddit){
        Log.i(LOG_TAG, "notifyUser");
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= NOTIFICATION_INTERVAL_MS) {
            String title = context.getString(R.string.app_name);
            String contentText;
            if(!subreddit.isEmpty() && !subreddit.equals("-"))
                contentText = String.format("New content available for the %s subreddit",
                        subreddit);
            else contentText = "New content available";
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getContext())
                            .setContentTitle(title)
                            .setContentText(contentText);
            Intent resultIntent = new Intent(getContext(), MainActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(REDDIST_NOTIFICATION_ID, mBuilder.build());
            //refreshing last sync
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(lastNotificationKey, System.currentTimeMillis());
            editor.commit();
        }
    }
}
