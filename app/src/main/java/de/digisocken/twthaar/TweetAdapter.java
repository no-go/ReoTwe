package de.digisocken.twthaar;

import android.content.Context;
import android.graphics.Color;
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
        TextView tGoto = (TextView) rowView.findViewById(R.id.tweetGoto);
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

        String nextHop = extractRT(text);
        if (nextHop == null) {
            ArrayList<String> dummy = extractUsers(text);

            if (dummy.size() > 0) {
                nextHop = dummy.get(0);
            } else {
                dummy = extractTag(text);
                if (dummy.size() > 0) nextHop = dummy.get(0);
            }
        }

        if (nextHop == null) nextHop = "@" + tweet.user.screenName;

        tGoto.setText(String.format(mContext.getString(R.string.gotoHint), nextHop));
        tGoto.setOnClickListener(new SearchListener(mContext, nextHop));

        if (imageful) {
            TweetView orgTweetView = new TweetView(mContext, tweet);
            orgTweetView.setBackgroundColor(Color.TRANSPARENT);
            tContent.addView(orgTweetView);
            tText.setVisibility(View.GONE);
        } else {
            tText.setText(text);
            tText.setVisibility(View.VISIBLE);
            tText.setOnClickListener(new TweetViewOnClick(tContent, tweet));
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

    public static ArrayList<String> extractTag(String text) {
        ArrayList<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("(.*)(#\\w{1,25})(\\b)").matcher(text);
        while (m.find()) {
            allMatches.add(m.group(2));
        }
        return allMatches;
    }

    class TweetViewOnClick implements View.OnClickListener {
        private Tweet tweet;
        private FrameLayout tContent;

        public TweetViewOnClick(FrameLayout fl, Tweet tw) {
            tContent = fl;
            tweet = tw;
        }

        @Override
        public void onClick(View v) {
            tContent.removeAllViews();
            TweetView orgTweetView = new TweetView(mContext, tweet);
            orgTweetView.setBackgroundColor(Color.TRANSPARENT);
            tContent.addView(orgTweetView);
        }
    }
}
