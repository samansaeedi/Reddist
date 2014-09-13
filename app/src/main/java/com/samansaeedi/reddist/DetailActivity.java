package com.samansaeedi.reddist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by captain on 8/30/14.
 */
public class DetailActivity extends ActionBarActivity {
    private InterstitialAd interstitial;

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setHomeButtonEnabled(true);

        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-3551699255306011/6284426089");

        // Create ad request.
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("014038006382687")
                .build();

        // Begin loading your interstitial.
        interstitial.loadAd(adRequest);

        if (savedInstanceState == null) {
            Bundle b = new Bundle();
            b.putLong("id", getIntent().getLongExtra("id", 0));
            b.putBoolean("twoPane", false);
            String sublistPreference = Utility.getPreferredSublist(this);
            String subredditPreference = Utility.getPreferredSubreddit(this);
            String title;
            if(!subredditPreference.equals("-"))
                title = subredditPreference + " | " + sublistPreference +
                        " #" + getIntent().getIntExtra("position", 1);
            else title = sublistPreference + " #" + getIntent().getIntExtra("position", 1);
            getSupportActionBar().setTitle(title);
            DetailFragment f = new DetailFragment();
            f.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.reddist_detail_container, f)
                    .commit();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
