package com.samansaeedi.reddist;

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

import com.samansaeedi.reddist.data.ReddistContract.ReddistEntry;
import com.samansaeedi.reddist.sync.ReddistSyncAdapter;

/**
 * Created by captain on 8/29/14.
 */
public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ListFragment.class.getSimpleName();

    private CursorAdapter adapter;
    private ListView listView;
    private int mPosition;
    private String mSublist;
    private String mSubreddit;

    public static final int REDDIST_LOADER = 0;

    public static final String[] LIST_COLUMNS = {
            ReddistEntry.TABLE_NAME + "." + ReddistEntry._ID,
            ReddistEntry.COLUMN_REDDIT_TITLE,
            ReddistEntry.COLUMN_REDDIT_AUTHOR,
            ReddistEntry.COLUMN_REDDIT_NUM_COMMENTS,
            ReddistEntry.COLUMN_REDDIT_SCORE
    };

    public static final int COL_ID = 0;
    public static final int COL_REDDIT_TITLE = 1;
    public static final int COL_REDDIT_AUTHOR = 2;
    public static final int COL_REDDIT_NUM_COMMENTS = 3;
    public static final int COL_REDDIT_SCORE = 4;

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
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSublist != null && mSubreddit != null &&
                !mSublist.equals(Utility.getPreferredSublist(getActivity())) &&
                !mSubreddit.equals(Utility.getPreferredSubreddit(getActivity()))) {
            getLoaderManager().restartLoader(REDDIST_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        if(adapter == null)
            adapter = new ReddistAdapter(getActivity(), null, 0);
        listView = (ListView) rootView.findViewById(R.id.listview_reddist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = ((CursorAdapter)parent.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                //boolean isMetric = Utility.isMetric(getActivity());
                mPosition = position;
                ((Callback) getActivity()).onItemSelected(cursor.getLong(COL_ID));
            }
        });
        return rootView;
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        String sortOrder = ReddistEntry.COLUMN_REDDIT_SCORE + " DESC";
        mSubreddit = Utility.getPreferredSubreddit(getActivity());
        mSublist = Utility.getPreferredSublist(getActivity());
        Uri uri;
        if(mSubreddit.equals("-")) {
            uri = ReddistEntry.buildReddistWithSublist(mSublist);
            return new CursorLoader(
                    getActivity(),
                    uri,
                    LIST_COLUMNS,
                    null,
                    new String[] {mSublist},
                    sortOrder
            );
        }
        else
        {
            uri = ReddistEntry.buildReddistWithSubredditAndSublist(mSubreddit, mSublist);
            return new CursorLoader(
                    getActivity(),
                    uri,
                    LIST_COLUMNS,
                    null,
                    new String[] {mSubreddit, mSublist},
                    sortOrder
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            ReddistSyncAdapter.syncImmediately(getActivity());
        }
        adapter.swapCursor(data);
        Log.i(LOG_TAG, "load finished");
        if(mPosition != ListView.INVALID_POSITION)
            listView.setSelection(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public interface Callback {
        public void onItemSelected(long id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", mPosition);
        outState.putString("subreddit", mSubreddit);
        outState.putString("sublist", mSublist);
    }
}
