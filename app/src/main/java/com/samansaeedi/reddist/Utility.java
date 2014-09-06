package com.samansaeedi.reddist;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by captain on 8/29/14.
 */
public class Utility {

    public static String getPreferredSubreddit (Context context){
        String subrreddit = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("subreddit", "-");
        if(subrreddit.isEmpty())
            return "-";
        else return subrreddit;
    }

    public static String getPreferredSublist(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("sublist", "hot");
    }

    public static int getPreferredNumberOfListItems(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("numberOfItems", 15);
    }

    public static int getColorFromRange(int value, int high){
        if(value > high)
            value = high;
        double theta = (1 - ((double)value) / ((double)high)) * Math.PI/2;
        return Color.rgb((int)Math.floor(255 * Math.cos(theta)),
                (int)Math.floor(127 * Math.sin(theta)),
                (int)Math.floor(255 * Math.sin(theta)));
    }

    public static void expand(final TextView v) {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.height = v.getLineHeight() * v.getLineCount() + 10;
        v.setLayoutParams(params);
    }

    public static void collapse(final TextView v, final int initialHeight) {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.height = initialHeight;
        v.setLayoutParams(params);
    }

    public static String getFormattedDate(long epoch){
//        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
//        String formattedDate = sdf.format(new Date(epoch * 1000));
//        String today = sdf.format(new Date());
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DATE, -1);
//        String yesterday = sdf.format(cal.getTime());
//        if(formattedDate.equals(today))
//            return "today";
//        else if (formattedDate.equals(yesterday))
//            return "yesterday";
//        return formattedDate;

        String formattedDate;
        long elapsed = ((Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime()).getTime() - epoch * 1000);
        long result;
        if((result = elapsed/31556916000L) != 0)
            formattedDate = String.format("%d year%s ago", result, result > 1 ? "s" : "");
        else if((result = elapsed/2629742400L) != 0)
            formattedDate = String.format("%d month%s ago", result, result > 1 ? "s" : "");
        else if((result = elapsed/86400000L) != 0)
            formattedDate = String.format("%d day%s ago", result, result > 1 ? "s" : "");
        else if((result = elapsed/3600000L) != 0)
            formattedDate = String.format("%d hour%s ago", result, result > 1 ? "s" : "");
        else if((result = elapsed/60000L) != 0)
            formattedDate = String.format("%d minute%s ago", result, result > 1 ? "s" : "");
        else if((result = elapsed/1000L) != 0)
            formattedDate = String.format("%d second%s ago", result, result > 1 ? "s" : "");
        else formattedDate = "just now";

        return formattedDate;

    }
}
