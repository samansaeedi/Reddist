package com.samansaeedi.reddist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.samansaeedi.reddist.sync.ReddistSyncAdapter;

/**
 * Created by captain on 8/29/14.
 */
public class MainActivity extends ActionBarActivity implements ListFragment.Callback{
    public boolean twoPane;
    public int highestScore;
    private InterstitialAd interstitial;

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE &&
//                dpWidth >= 600) {
//            setContentView(R.layout.activity_main_wide);
//        } else {
//            setContentView(R.layout.activity_main);
//        }
//
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        setContentView(R.layout.activity_main);
        Bundle b = new Bundle();
        if(findViewById(R.id.reddist_detail_container) != null) {
            twoPane = true;
            interstitial = new InterstitialAd(this);
            interstitial.setAdUnitId("ca-app-pub-3551699255306011/6284426089");

            // Create ad request.
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("014038006382687")
                    .build();

            // Begin loading your interstitial.
            interstitial.loadAd(adRequest);

            String sublistPreference = Utility.getPreferredSublist(this);
            String subredditPreference = Utility.getPreferredSubreddit(this);
            String title;
            if(!subredditPreference.equals("-"))
                title = "Reddist|" + subredditPreference + "|" + sublistPreference;
            else title = "Reddist|" + sublistPreference;
            getSupportActionBar().setTitle(title);


            b.putBoolean("twoPane", twoPane);
            ListFragment fragment = new ListFragment();
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(b);
            fragment.setArguments(b);
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.reddist_detail_container, detailFragment)
                        .replace(R.id.reddist_container, fragment)
                        .commit();
            }
        }
        else {
            twoPane = false;
            b.putBoolean("twoPane", twoPane);
        }
        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(b);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.reddist_container, listFragment,
                            String.valueOf(R.id.list_fragment))
                    .commit();
        }
        ReddistSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(long id, int position, boolean clicked, int highestScore) {
        if(clicked) {
            if (twoPane) {
                this.highestScore = highestScore;
                DetailFragment f = new DetailFragment();
                Bundle b = new Bundle();
                b.putLong("id", id);
                b.putBoolean("twoPane", twoPane);
                b.putInt("highestScore", highestScore);
                f.setArguments(b);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.reddist_detail_container, f)
                        .commit();
            } else {
                Intent detailsIntent = new Intent(this, DetailActivity.class)
                        .putExtra("id", id).putExtra("position", position);
                startActivity(detailsIntent);
            }
        }
        else {
            Message m = new Message();
            Bundle b = new Bundle();
            b.putLong("id", id);
            b.putBoolean("twoPane", twoPane);
            b.putInt("highestScore", highestScore);
            m.setData(b);
            m.what = 1;
            this.highestScore = highestScore;
            handler.sendMessage(m);
        }
    }
    // code from http://stackoverflow.com/questions/12276243/commit-fragment-from-onloadfinished-within-activity
    private Handler handler = new Handler()  // handler for commiting fragment after data is loaded
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 1){
                if (twoPane) {
                    DetailFragment f = new DetailFragment();
                    Bundle b = msg.getData();
                    f.setArguments(b);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.reddist_detail_container, f)
                            .commit();
                }
            }
        }
    };
}
