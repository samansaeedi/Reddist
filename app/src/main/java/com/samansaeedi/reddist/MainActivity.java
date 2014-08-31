package com.samansaeedi.reddist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.samansaeedi.reddist.sync.ReddistSyncAdapter;

/**
 * Created by captain on 8/29/14.
 */
public class MainActivity extends ActionBarActivity implements ListFragment.Callback{
    public boolean twoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.reddist_container, new ListFragment(),
                            String.valueOf(R.id.list_fragment))
                    .commit();
        }
        if(findViewById(R.id.reddist_detail_container) != null){
            twoPane = true;
            ListFragment fragment = new ListFragment();
            DetailFragment detailFragment = new DetailFragment();
            Bundle b = new Bundle();
            b.putBoolean("twoPane", twoPane);
            detailFragment.setArguments(b);
            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.reddist_detail_container, detailFragment)
                        .replace(R.id.reddist_container, fragment)
                        .commit();
            }
        }
        else {
            twoPane = false;
        }
        ReddistSyncAdapter.initializeSyncAdapter(this);
        ReddistSyncAdapter.syncImmediately(this);
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
    public void onItemSelected(long id) {
        if(twoPane){
            DetailFragment f = new DetailFragment();
            Bundle b = new Bundle();
            b.putLong("id", id);
            b.putBoolean("twoPane", twoPane);
            f.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.reddist_detail_container, f)
                    .commit();
        }
        else {
            Intent detailsIntent = new Intent(this, DetailActivity.class)
                    .putExtra("id", id);
            startActivity(detailsIntent);
        }
    }
}
