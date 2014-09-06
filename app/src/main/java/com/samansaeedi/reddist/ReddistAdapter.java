package com.samansaeedi.reddist;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by captain on 8/30/14.
 */
public class ReddistAdapter extends CursorAdapter {
    private static final String LOG_TAG = ReddistAdapter.class.getSimpleName();

    public ReddistAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_reddist, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.numComments.setText(cursor.getString(ListFragment.COL_REDDIT_NUM_COMMENTS));
        viewHolder.title.setText(cursor.getString(ListFragment.COL_REDDIT_TITLE));
        viewHolder.author.setText(cursor.getString(ListFragment.COL_REDDIT_AUTHOR));
        viewHolder.score.setText(cursor.getString(ListFragment.COL_REDDIT_SCORE));
        int position = cursor.getPosition();
        cursor.moveToFirst();
        int high = cursor.getInt(ListFragment.COL_REDDIT_SCORE);
        cursor.moveToPosition(position);
        int value = cursor.getInt(ListFragment.COL_REDDIT_SCORE);
        viewHolder.score.setTextColor(Utility.getColorFromRange(value, high));
        viewHolder.order.setText(String.valueOf(cursor.getPosition() + 1));
        viewHolder.date.setText(Utility.getFormattedDate(cursor.getLong(ListFragment.COL_REDDIT_CREATED_UTC)));
        Log.i(LOG_TAG, "binded view");
    }

    private static class ViewHolder{
        public final TextView numComments;
        public final TextView title;
        public final TextView author;
        public final TextView score;
        public final LinearLayout listItem;
        public final TextView date;
        public final TextView order;

        public ViewHolder(View view){
            listItem = (LinearLayout) view.findViewById(R.id.list_item);
            numComments = (TextView) view.findViewById(R.id.list_item_numcomments);
            title = (TextView) view.findViewById(R.id.list_item_title);
            author = (TextView) view.findViewById(R.id.list_item_author);
            score = (TextView) view.findViewById(R.id.list_item_score);
            date = (TextView) view.findViewById(R.id.list_item_date);
            order = (TextView) view.findViewById(R.id.list_item_order);
        }
    }
}
