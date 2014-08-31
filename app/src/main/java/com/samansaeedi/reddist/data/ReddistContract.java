package com.samansaeedi.reddist.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by captain on 8/29/14.
 */
public class ReddistContract {

    public static final String CONTENT_AUTHORITY = "com.samansaeedi.reddist";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_REDDIST = "reddist";

    public static final class ReddistEntry implements BaseColumns {
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_REDDIST;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_REDDIST;
        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REDDIST).build();

        public static final String TABLE_NAME = "reddist";
        public static final String COLUMN_REDDIT_ID = "id";
        public static final String COLUMN_SUBREDDIT = "subreddit";
        public static final String COLUMN_SUBLIST = "sublist";
        public static final String COLUMN_REDDIT_AUTHOR = "author";
        public static final String COLUMN_REDDIT_SCORE = "score";
        public static final String COLUMN_REDDIT_UPS = "ups";
        public static final String COLUMN_REDDIT_DOWNS = "downs";
        public static final String COLUMN_REDDIT_TITLE = "title";
        public static final String COLUMN_REDDIT_CREATED_UTC = "created_utc";
        public static final String COLUMN_FETCHED = "fetched";
        public static final String COLUMN_REDDIT_LINK = "permalink";
        public static final String COLUMN_REDDIT_URL = "url";
        public static final String COLUMN_REDDIT_NUM_COMMENTS = "num_comments";
        public static final String COLUMN_REDDIT_DOMAIN = "domain";
        public static final String COLUMN_REDDIT_CONTENT_TEXT = "selftext";
        public static final String COLUMN_REDDIT_CONTENT_HTML = "selftext_html";
        public static final String COLUMN_REDDIT_THUMBNAIL = "thumbnail";

        public static Uri buildReddistUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildReddistWithSublist(String sublist){
            return CONTENT_URI.buildUpon().appendPath(sublist).build();
        }
        public static Uri buildReddistWithSubredditAndSublist(String subreddit, String sublist){
            return CONTENT_URI.buildUpon().appendPath("r")
                    .appendPath(subreddit).appendPath(sublist).build();
        }

    }


}
