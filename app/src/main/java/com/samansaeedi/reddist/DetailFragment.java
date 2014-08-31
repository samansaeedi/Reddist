package com.samansaeedi.reddist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.samansaeedi.reddist.data.ReddistContract.ReddistEntry;

/**
 * Created by captain on 8/29/14.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private View rootView;
    private boolean twoPane;

    public static String[] REDDIST_COLUMNS = {
            ReddistEntry.TABLE_NAME + "." + ReddistEntry._ID,
            ReddistEntry.COLUMN_REDDIT_AUTHOR,
            ReddistEntry.COLUMN_SUBREDDIT,
            ReddistEntry.COLUMN_REDDIT_TITLE,
            ReddistEntry.COLUMN_REDDIT_SCORE,
            ReddistEntry.COLUMN_REDDIT_URL,
            ReddistEntry.COLUMN_REDDIT_UPS,
            ReddistEntry.COLUMN_REDDIT_DOWNS,
            ReddistEntry.COLUMN_REDDIT_NUM_COMMENTS
    };

    public static final int COL_ID = 0;
    public static final int COL_REDDIT_AUTHOR = 1;
    public static final int COL_REDDIT_SUBREDDIT = 2;
    public static final int COL_REDDIT_TITLE = 3;
    public static final int COL_REDDIT_SCORE = 4;
    public static final int COL_REDDIT_URL = 5;
    public static final int COL_REDDIT_UPS = 6;
    public static final int COL_REDDIT_DOWNS = 7;
    public static final int COL_REDDIT_NUM_COMMENTS = 8;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle b = getArguments();
        if(b!= null && b.getBoolean("twoPane")) {
            rootView = inflater.inflate(R.layout.fragment_detail_wide, container, false);
            twoPane = b.getBoolean("twoPane");
        }
        else
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        getLoaderManager().initLoader(1, null, this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        long id = -1;
        Bundle b = getArguments();
        if(b != null){
            id = b.getLong("id");
            Uri queryUri = ReddistEntry.buildReddistUri(id);

            return new CursorLoader(
                    getActivity(),
                    queryUri,
                    REDDIST_COLUMNS,
                    ReddistEntry._ID + " = ? ",
                    new String[] {String.valueOf(id)},
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor != null && cursor.moveToFirst()) {
            ViewHolder view = new ViewHolder(rootView, twoPane);
            Log.i(LOG_TAG, String.format("title: %s, link: %s", cursor.getString(COL_REDDIT_TITLE),
                    cursor.getString(COL_REDDIT_URL)));
            view.title.setText(cursor.getString(COL_REDDIT_TITLE));
            WebSettings settings = view.webView.getSettings();
            settings.setJavaScriptEnabled(true);
            view.webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            //code burrowed from http://stackoverflow.com/questions/11288611/how-to-load-a-url-to-webview-in-android
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

            final ProgressDialog progressBar = ProgressDialog.show(getActivity(), "WebView Example", "Loading...");

            view.webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i(LOG_TAG, "Processing view.webView url click...");
                    view.loadUrl(url);
                    return true;
                }

                public void onPageFinished(WebView view, String url) {
                    Log.i(LOG_TAG, "Finished loading URL: " +url);
                    if (progressBar!= null && progressBar.isShowing()) {
                        progressBar.dismiss();
                    }
                }

                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.e(LOG_TAG, "Error: " + description);
                    Toast.makeText(getActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage(description);
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    alertDialog.show();
                }
            });
            view.webView.loadUrl(cursor.getString(COL_REDDIT_URL));
            if (twoPane) {
                view.subreddit.setText(cursor.getString(COL_REDDIT_SUBREDDIT));
                view.ups.setText(cursor.getString(COL_REDDIT_UPS));
                view.numComments.setText(cursor.getString(COL_REDDIT_NUM_COMMENTS));
                view.downs.setText(cursor.getString(COL_REDDIT_DOWNS));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private static class ViewHolder{
        public final TextView title;
        public final WebView webView;
        public final TextView subreddit;
        public final TextView ups;
        public final TextView numComments;
        public final TextView downs;

        public ViewHolder(View view, boolean twoPane){

            title = (TextView)view.findViewById(R.id.detail_title_textview);
            webView = (WebView)view.findViewById(R.id.detail_webview);
            if(twoPane) {
                subreddit = (TextView) view.findViewById(R.id.detail_subreddit_textview);
                ups = (TextView) view.findViewById(R.id.detail_ups_textview);
                numComments = (TextView) view.findViewById(R.id.detail_numcomments_textview);
                downs = (TextView) view.findViewById(R.id.detail_downs_textview);
            }
            else{
                subreddit = ups = numComments = downs = null;
            }
        }

    }
}
