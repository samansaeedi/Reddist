package com.samansaeedi.reddist;

import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.samansaeedi.reddist.data.ReddistContract.ReddistEntry;
import com.samansaeedi.reddist.sync.ReddistSyncAdapter;

/**
 * Created by captain on 8/29/14.
 */
public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ListFragment.class.getSimpleName();

    private ReddistAdapter adapter;
    private ListView listView;
    private int mPosition;
    private String mSublist;
    private String mSubreddit;
    private boolean zeroSyncAttempted = false;
    private boolean twoPane = false;
    private int highestScore;

    public static final int REDDIST_LOADER = 0;

    public static final String[] LIST_COLUMNS = {
            ReddistEntry.TABLE_NAME + "." + ReddistEntry._ID,
            ReddistEntry.COLUMN_REDDIT_TITLE,
            ReddistEntry.COLUMN_REDDIT_AUTHOR,
            ReddistEntry.COLUMN_REDDIT_NUM_COMMENTS,
            ReddistEntry.COLUMN_REDDIT_SCORE,
            ReddistEntry.COLUMN_REDDIT_CREATED_UTC
    };

    public static final int COL_ID = 0;
    public static final int COL_REDDIT_TITLE = 1;
    public static final int COL_REDDIT_AUTHOR = 2;
    public static final int COL_REDDIT_NUM_COMMENTS = 3;
    public static final int COL_REDDIT_SCORE = 4;
    public static final int COL_REDDIT_CREATED_UTC = 5;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(REDDIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if(savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("position");
            mSubreddit = savedInstanceState.getString("subreddit");
            mSublist = savedInstanceState.getString("sublist");
        }
        Bundle b = getArguments();
        twoPane = b.getBoolean("twoPane");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSublist != null && mSubreddit != null &&
                (!mSublist.equals(Utility.getPreferredSublist(getActivity())) ||
                !mSubreddit.equals(Utility.getPreferredSubreddit(getActivity())))) {
            getLoaderManager().restartLoader(REDDIST_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        //String deviceId = ((TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        Bundle adBundle = new Bundle();
        adBundle.putString("color_bg", String.valueOf(getResources().getColor(R.color.orangered)));
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("014038006382687")
                .build();
        adView.loadAd(adRequest);
        if(adapter == null)
            adapter = new ReddistAdapter(getActivity(), null, 0);

        if(twoPane)
            adapter.setViewId(R.layout.list_item_reddist_wide);
        else adapter.setViewId(R.layout.list_item_reddist);

        listView = (ListView) rootView.findViewById(R.id.listview_reddist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = ((CursorAdapter)parent.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                //boolean isMetric = Utility.isMetric(getActivity());
                mPosition = position;
                ((Callback) getActivity()).onItemSelected(cursor.getLong(COL_ID), cursor.getPosition() + 1, true, highestScore);
            }
        });
        return rootView;
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        mSubreddit = Utility.getPreferredSubreddit(getActivity());
        mSublist = Utility.getPreferredSublist(getActivity());
        String sortOrder;
        if(mSublist.equalsIgnoreCase("new"))
            sortOrder = ReddistEntry.COLUMN_REDDIT_CREATED_UTC + " DESC";
        else sortOrder = ReddistEntry.COLUMN_REDDIT_SCORE + " DESC";
        Log.i(LOG_TAG, "changed preferences to: " + mSubreddit + ", " + mSublist);
        Uri uri = ReddistEntry.buildReddistWithSubredditAndSublist(mSubreddit, mSublist);
        return new CursorLoader(
                getActivity(),
                uri,
                LIST_COLUMNS,
                null,
                new String[] {mSubreddit, mSublist},
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            if(!zeroSyncAttempted) {
                ReddistSyncAdapter.syncImmediately(getActivity());
                zeroSyncAttempted = true;
            }
        } else zeroSyncAttempted = false;
        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(ReddistSyncAdapter.REDDIST_NOTIFICATION_ID);
        adapter.swapCursor(data);
        Log.i(LOG_TAG, "load finished:" + mSubreddit + " " + mSublist);
        if (mPosition != ListView.INVALID_POSITION && mPosition < data.getCount()) {
            //listView.setSelection(mPosition);
            highestScore = Utility.getHighestScore(getActivity());
            listView.setItemChecked(mPosition, true);
            data.moveToPosition(mPosition);
            ((Callback) getActivity()).onItemSelected(data.getLong(COL_ID), data.getPosition() + 1, false, highestScore);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public interface Callback {
        public void onItemSelected(long id, int position, boolean clicked, int highestScore);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", mPosition);
        outState.putString("subreddit", mSubreddit);
        outState.putString("sublist", mSublist);
    }
}
