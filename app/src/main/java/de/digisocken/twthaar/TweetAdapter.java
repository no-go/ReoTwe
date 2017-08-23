package de.digisocken.twthaar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

class TweetAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Tweet> mDataSource;
    private SimpleDateFormat formatIn = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
    private SimpleDateFormat formatOut = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    public TweetAdapter(Context context, ArrayList<Tweet> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = mInflater.inflate(R.layout.tweet_item, parent, false);
        if (position == 0) {
            rowView.setPadding(0,22,0,0);
        }

        TextView tDate = (TextView) rowView.findViewById(R.id.tweetDate);
        TextView tStats = (TextView) rowView.findViewById(R.id.tweetStats);
        TextView tText = (TextView) rowView.findViewById(R.id.tweetText);

        Tweet tweet = (Tweet) getItem(position);
        tText.setText(tweet.text);

        tStats.setText(
                " ⇆ " +
                Integer.toString(tweet.retweetCount) +
                " ★ " + Integer.toString(tweet.favoriteCount)
        );

        try {
            Date parsedDate = formatIn.parse(tweet.createdAt);
            tDate.setText(formatOut.format(parsedDate));
        } catch (ParseException e) {
            e.printStackTrace();
            tDate.setText(tweet.createdAt);
        }

        String rtuser = extractRT(tweet.text);
        if (rtuser != null) {
            tText.setOnClickListener(new MyOnClickListener(rtuser));
        }

        return rowView;
    }

    public static String extractRT(String txt) {
        int rtindex = txt.indexOf("RT @");
        if (rtindex >= 0) {
            String rtuser = txt.substring(
                    rtindex,
                    txt.indexOf(": ", rtindex)
            );
            return rtuser.replace("RT @", "");
        } else {
            return null;
        }
    }

    class MyOnClickListener implements View.OnClickListener {
        String query;

        public MyOnClickListener(String q) {
            query = q;
        }

        @Override
        public void onClick(View v) {
            UserTimeline userTimeline = new UserTimeline.Builder()
                    .screenName(query)
                    .maxItemsPerRequest(TwthaarApp.DEFAULT_MAXTIMELINE)
                    .build();

            userTimeline.next(null, new Callback<TimelineResult<Tweet>>() {

                @Override
                public void success(Result<TimelineResult<Tweet>> result) {
                    mDataSource.clear();
                    for(final Tweet tweet : result.data.items) {
                        mDataSource.add(tweet);
                    }
                    notifyDataSetChanged();
                }
                @Override
                public void failure(TwitterException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
