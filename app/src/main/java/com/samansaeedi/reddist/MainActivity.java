package com.samansaeedi.reddist;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by captain on 8/29/14.
 */
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.reddit_list_container, new ListFragment(),
                            String.valueOf(R.id.list_fragment))
                    .commit();
        }
    }
}
