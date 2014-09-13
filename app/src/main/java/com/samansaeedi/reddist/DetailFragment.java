package com.samansaeedi.reddist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.InterstitialAd;
import com.samansaeedi.reddist.data.ReddistContract.ReddistEntry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

;
;

/**
 * Created by captain on 8/29/14.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

    private View rootView;
    private boolean twoPane;
    private String url;
    private String title;
    private ViewHolder viewHolder;
    private int highestScore;
    private InterstitialAd interstitial;
    private Activity activity;
    private CountDownTimer timer = null;
    
    public static String[] REDDIST_COLUMNS = {
            ReddistEntry.TABLE_NAME + "." + ReddistEntry._ID,
            ReddistEntry.COLUMN_REDDIT_AUTHOR,
            ReddistEntry.COLUMN_SUBREDDIT,
            ReddistEntry.COLUMN_REDDIT_TITLE,
            ReddistEntry.COLUMN_REDDIT_SCORE,
            ReddistEntry.COLUMN_REDDIT_URL,
            ReddistEntry.COLUMN_REDDIT_UPS,
            ReddistEntry.COLUMN_REDDIT_DOWNS,
            ReddistEntry.COLUMN_REDDIT_NUM_COMMENTS,
            ReddistEntry.COLUMN_REDDIT_CREATED_UTC
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
    public static final int COL_REDDIT_CREATED_UTC = 9;

    public DetailFragment(){
        setHasOptionsMenu(true);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail, menu);
        MenuItem item = menu.findItem(R.id.action_share_id);
        ShareActionProvider sap = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if(sap != null) {
            sap.setShareIntent(new Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                    .putExtra(Intent.EXTRA_SUBJECT, "Found this on reddit")
                    .putExtra(Intent.EXTRA_TEXT, title + " #Reddist " + url));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_view_on_browser_id){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                startActivity(intent);
            }
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AudioManager) activity.getSystemService(
                Context.AUDIO_SERVICE)).requestAudioFocus(
                new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                    }
                }, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if(timer != null)
            timer.cancel();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle b = getArguments();
        highestScore = b.getInt("highestScore");
        if(b!= null && b.getBoolean("twoPane")) {
            rootView = inflater.inflate(R.layout.fragment_detail_wide, container, false);
            twoPane = b.getBoolean("twoPane");
        }
        else
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        if(new Random(System.currentTimeMillis()).nextInt(100) < 35 ) {
            timer = new CountDownTimer(10000, 10000) {
                @Override
                public void onFinish() {
                    if(activity != null) {
                        if (twoPane)
                            ((MainActivity) activity).displayInterstitial();
                        else ((DetailActivity) activity).displayInterstitial();
                    }
                }
                @Override
                public void onTick(long millisUntilFinished) {

                }
            };
            timer.start();
        }
        viewHolder = new ViewHolder(rootView, twoPane);
        WebSettings settings = viewHolder.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        settings.setPluginState(WebSettings.PluginState.ON);
        if(Build.VERSION.SDK_INT > 15) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        //settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        if(Build.VERSION.SDK_INT > 18)
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
//        settings.setAppCachePath("/data/data/com.samansaeedi.reddist/cache");
//        settings.setAppCacheEnabled(true);
//        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        viewHolder.webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);        
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
                    activity,
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
            try {
                URL oldUrl = new URL(cursor.getString(COL_REDDIT_URL));
                if(oldUrl.getHost().equals("youtube.com") || oldUrl.getHost().endsWith(".youtube.com"))
                    url = new URL("http", oldUrl.getHost(), oldUrl.getPort(), oldUrl.getFile()).toString();
                else url = oldUrl.toString();
            }
            catch(MalformedURLException e){
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            title = cursor.getString(COL_REDDIT_TITLE);
            ActivityCompat.invalidateOptionsMenu(activity);
            //activity.invalidateOptionsMenu();
            Log.i(LOG_TAG, String.format("title: %s, link: %s", cursor.getString(COL_REDDIT_TITLE),
                    cursor.getString(COL_REDDIT_URL)));            
            //code burrowed from http://stackoverflow.com/questions/11288611/how-to-load-a-url-to-webview-in-android
            final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

            final ProgressDialog progressBar = ProgressDialog.show(activity, "3 2 1", "Loading...");
            progressBar.setCanceledOnTouchOutside(true);

            if(twoPane){
                viewHolder.ups.setText(cursor.getString(COL_REDDIT_UPS));
                viewHolder.downs.setText(cursor.getString(COL_REDDIT_DOWNS));
                viewHolder.score.setText(cursor.getString(COL_REDDIT_SCORE));
                viewHolder.score.setTextColor(Utility.getColorFromRange(cursor.getInt(COL_REDDIT_SCORE), highestScore));
                viewHolder.author.setText(cursor.getString(COL_REDDIT_AUTHOR));
                viewHolder.numComments.setText(cursor.getString(COL_REDDIT_NUM_COMMENTS));
                viewHolder.date.setText(Utility.getFormattedDate(cursor.getLong(COL_REDDIT_CREATED_UTC)));
            }

            //viewHolder.webView.setWebChromeClient(new WebChromeClient());
            viewHolder.webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i(LOG_TAG, "Processing view.webView url click...");
                        view.setWebChromeClient(new WebChromeClient() {

                            private View mCustomView;

                            @Override
                            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
                            {
                                // if a view already exists then immediately terminate the new one
                                if (mCustomView != null)
                                {
                                    callback.onCustomViewHidden();
                                    return;
                                }

                                // Add the custom view to its container.
                                viewHolder.customViewContainer.addView(view, COVER_SCREEN_GRAVITY_CENTER);
                                mCustomView = view;
                                mCustomViewCallback = callback;

                                // hide main browser view
                                viewHolder.contentView.setVisibility(View.GONE);

                                // Finally show the custom view container.
                                viewHolder.customViewContainer.setVisibility(View.VISIBLE);
                                viewHolder.customViewContainer.bringToFront();
                            }

                            @Override
                            public void onHideCustomView()
                            {
                                if (mCustomView == null)
                                    return;

                                // Hide the custom view.
                                mCustomView.setVisibility(View.GONE);
                                // Remove the custom view from its container.
                                viewHolder.customViewContainer.removeView(mCustomView);
                                mCustomView = null;
                                viewHolder.customViewContainer.setVisibility(View.GONE);
                                mCustomViewCallback.onCustomViewHidden();

                                // Show the content view.
                                viewHolder.contentView.setVisibility(View.VISIBLE);
                            }

                        });
                    view.loadUrl(url);
                    return true;
                }
                public void onPageFinished(WebView view, String url) {
                    Log.i(LOG_TAG, "Finished loading URL: " + url);
                    try {
                        if (progressBar != null && progressBar.isShowing()) {
                            progressBar.dismiss();
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }

                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.e(LOG_TAG, "Error: " + description);
                    Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
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
            viewHolder.webView.loadUrl(url);
            if (twoPane) {
                viewHolder.ups.setText(cursor.getString(COL_REDDIT_UPS));
                viewHolder.numComments.setText(cursor.getString(COL_REDDIT_NUM_COMMENTS));
                viewHolder.downs.setText(cursor.getString(COL_REDDIT_DOWNS));
            }
            else {
                viewHolder.title.setText(cursor.getString(COL_REDDIT_TITLE));
                viewHolder.title.setOnTouchListener(new View.OnTouchListener() {
                    boolean expanded = false;
                    int initialHeight = 0;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                            if (initialHeight == 0 && expanded == false)
                                initialHeight = v.getHeight();
                            if (!expanded)
                                Utility.expand((TextView) v);
                            else Utility.collapse((TextView) v, initialHeight);
                            expanded = !expanded;
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private static class ViewHolder{
        public final TextView title;
        public final WebView webView;
        public final TextView ups;
        public final TextView numComments;
        public final TextView downs;
        public final TextView date;
        public final TextView author;
        public final TextView score;
        public final LinearLayout contentView;
        public final FrameLayout customViewContainer;

        public ViewHolder(View view, boolean twoPane){

            title = (TextView)view.findViewById(R.id.detail_title_textview);
            webView = (WebView)view.findViewById(R.id.detail_webview);
            contentView = (LinearLayout) view.findViewById(R.id.webview_container);
            customViewContainer = (FrameLayout) view.findViewById(R.id.fullscreen_custom_content);
            if(twoPane) {
                ups = (TextView) view.findViewById(R.id.detail_ups_textview);
                numComments = (TextView) view.findViewById(R.id.detail_numcomments_textview);
                downs = (TextView) view.findViewById(R.id.detail_downs_textview);
                author = (TextView) view.findViewById(R.id.detail_author_textview);
                date = (TextView) view.findViewById(R.id.detail_date_textview);
                score = (TextView) view.findViewById(R.id.detail_score_textview);
            }
            else{
                date = author = ups = numComments = downs = score = null;
            }
        }

    }
}
