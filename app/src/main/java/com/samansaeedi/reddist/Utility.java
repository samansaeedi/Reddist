package com.samansaeedi.reddist;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by captain on 8/29/14.
 */
public class Utility {

    public static String getPreferredSubreddit (Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("subreddit", "-");
    }

    public static String getPreferredSublist(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("sublist", "hot");
    }

    public static int getPreferredNumberOfListItems(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("numberOfListItems", 15);
    }

}
