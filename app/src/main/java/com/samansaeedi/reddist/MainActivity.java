package com.samansaeedi.reddist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    public void onItemSelected(long id, int position, boolean clicked) {
        if(clicked) {
            if (twoPane) {
                DetailFragment f = new DetailFragment();
                Bundle b = new Bundle();
                b.putLong("id", id);
                b.putBoolean("twoPane", twoPane);
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
            m.setData(b);
            m.what = 1;
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
                    Bundle b = new Bundle();
                    b.putLong("id", msg.getData().getLong("id"));
                    b.putBoolean("twoPane", twoPane);
                    f.setArguments(b);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.reddist_detail_container, f)
                            .commit();
                }
            }
        }
    };
}
