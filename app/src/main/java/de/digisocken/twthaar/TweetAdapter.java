package de.digisocken.twthaar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetView;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TweetAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Tweet> mDataSource;
    private boolean imageful;

    public TweetAdapter(Context context, ArrayList<Tweet> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageful = TwthaarApp.mPreferences.getBoolean("imageful", false);
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
        Tweet tweet = (Tweet) getItem(position);
        String text = tweet.text;
        View rowView = mInflater.inflate(R.layout.tweet_item, parent, false);
        if (position == 0) {
            rowView.setPadding(0,22,0,0);
        }

        TextView tDate = (TextView) rowView.findViewById(R.id.tweetDate);
        TextView tStats = (TextView) rowView.findViewById(R.id.tweetStats);
        TextView tText = (TextView) rowView.findViewById(R.id.tweetText);
        TextView tUser = (TextView) rowView.findViewById(R.id.tweetUser);
        FrameLayout tContent = (FrameLayout) rowView.findViewById(R.id.tweetContent);

        tUser.setText("@" + tweet.user.screenName);

        tStats.setText(
                " ⇆ " +
                Integer.toString(tweet.retweetCount) +
                " ★ " + Integer.toString(tweet.favoriteCount)
        );

        try {
            Date parsedDate = TwthaarApp.formatIn.parse(tweet.createdAt);
            tDate.setText(TwthaarApp.formatOut.format(parsedDate));
        } catch (ParseException e) {
            e.printStackTrace();
            tDate.setText(tweet.createdAt);
        }

        String nextUser = extractRT(text);
        if (nextUser == null) {
            ArrayList<String> dummy = extractUsers(text);
            if (dummy.size() > 0) nextUser = dummy.get(0);
        }
        if (nextUser == null) nextUser = tweet.user.screenName;

        tDate.setOnClickListener(new MyOnClickListener(nextUser));
        tStats.setOnClickListener(new MyOnClickListener(nextUser));

        if (imageful) {
            tDate.setPadding(10,10,10,10);
            tDate.setTextColor(Color.WHITE);
            tDate.setBackgroundColor(ContextCompat.getColor(mContext, R.color.tw__blue_pressed));
            tStats.setPadding(10,10,10,10);
            tStats.setTextColor(Color.WHITE);
            tStats.setBackgroundColor(ContextCompat.getColor(mContext, R.color.tw__blue_pressed));

            TweetView orgTweetView = new TweetView(mContext, tweet);
            orgTweetView.setBackgroundColor(Color.TRANSPARENT);
            tContent.addView(orgTweetView);
            tText.setVisibility(View.GONE);
        } else {
            tText.setText(text);
            tText.setVisibility(View.VISIBLE);
            tText.setOnClickListener(new MyOnClickListener(nextUser));
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

    public static ArrayList<String> extractUsers(String text) {
        ArrayList<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("(.*)(@\\w{1,15})(\\b)").matcher(text);
        while (m.find()) {
            allMatches.add(m.group(2));
        }
        return allMatches;
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
                    .maxItemsPerRequest(TwthaarApp.DEFAULT_MAX)
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
