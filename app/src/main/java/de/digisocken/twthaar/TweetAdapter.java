package de.digisocken.twthaar;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetcomposer.ComposerView;
import com.twitter.sdk.android.tweetui.TweetView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TweetAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Tweet> mDataSource;
    public boolean imageful;

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

        TextView tDate  = (TextView) rowView.findViewById(R.id.tweetDate);
        TextView tStats = (TextView) rowView.findViewById(R.id.tweetStats);
        TextView tText  = (TextView) rowView.findViewById(R.id.tweetText);
        TextView tUser  = (TextView) rowView.findViewById(R.id.tweetUser);
        TextView trt    = (TextView) rowView.findViewById(R.id.tweetRTname);
        ImageView tAvatar = (ImageView) rowView.findViewById(R.id.tweetAvatar);
        FrameLayout tContent = (FrameLayout) rowView.findViewById(R.id.tweetContent);

        tUser.setText(tweet.user.name);
        tUser.setOnClickListener(new SearchListener(mContext, tweet.user.screenName));
        trt.setText("");

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
        } else {
            tUser.setText(tweet.retweetedStatus.user.name);
            text = text.replace("RT " + nextHop + ": ","");
            trt.setText(tweet.user.name + " ⇆");
        }

        if (nextHop == null) nextHop = "@" + tweet.user.screenName;

        tStats.setText(String.format(
                mContext.getString(R.string.statsGoto),
                    tweet.retweetCount,
                    tweet.favoriteCount,
                    nextHop)
        );

        tDate.setOnClickListener(new SearchListener(mContext, nextHop));
        tStats.setOnClickListener(new SearchListener(mContext, nextHop));

        TweetView orgTweetView = new TweetView(mContext, tweet);
        ImageView avatar = (ImageView) orgTweetView.findViewById(R.id.tw__tweet_author_avatar);
        tAvatar.setBackground(avatar.getDrawable());

        if (imageful) {
            orgTweetView.setBackgroundColor(Color.TRANSPARENT);

            View t1 = orgTweetView.findViewById(R.id.tw__twitter_logo);
            View t2 = orgTweetView.findViewById(R.id.tw__tweet_retweeted_by);
            View t3 = orgTweetView.findViewById(R.id.tw__tweet_author_full_name);
            View t4 = orgTweetView.findViewById(R.id.tw__tweet_author_screen_name);
            View t5 = orgTweetView.findViewById(R.id.tw__tweet_timestamp);
            TextView t6 = (TextView) orgTweetView.findViewById(R.id.tw__tweet_text);
            FrameLayout fl = (FrameLayout) orgTweetView.findViewById(R.id.quote_tweet_holder);
            TextView qut0 = null;
            TextView qut1 = null;
            TextView qut2 = null;
            if (fl != null) {
                qut0 = (TextView) fl.findViewById(R.id.tw__tweet_text);
                qut1 = (TextView) fl.findViewById(R.id.tw__tweet_author_full_name);
                qut2 = (TextView) fl.findViewById(R.id.tw__tweet_author_screen_name);
            }
            t1.setVisibility(View.GONE);
            t2.setVisibility(View.GONE);
            t3.setVisibility(View.GONE);
            t4.setVisibility(View.GONE);
            t5.setVisibility(View.GONE);
            t6.setTextColor(ContextCompat.getColor(mContext, R.color.textFront));
            if (qut0 != null) {
                qut0.setTextColor(ContextCompat.getColor(mContext, R.color.textFront));
                qut1.setTextColor(ContextCompat.getColor(mContext, R.color.textFront));
                qut2.setTextColor(ContextCompat.getColor(mContext, R.color.textFrontLight));
            }
            avatar.setVisibility(View.GONE);
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
            return rtuser.replace("RT ", "");
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
            ImageView avatar = (ImageView) orgTweetView.findViewById(R.id.tw__tweet_author_avatar);
            orgTweetView.setBackgroundColor(Color.TRANSPARENT);
            View t1 = orgTweetView.findViewById(R.id.tw__twitter_logo);
            View t2 = orgTweetView.findViewById(R.id.tw__tweet_retweeted_by);
            View t3 = orgTweetView.findViewById(R.id.tw__tweet_author_full_name);
            View t4 = orgTweetView.findViewById(R.id.tw__tweet_author_screen_name);
            View t5 = orgTweetView.findViewById(R.id.tw__tweet_timestamp);
            TextView t6 = (TextView) orgTweetView.findViewById(R.id.tw__tweet_text);
            FrameLayout fl = (FrameLayout) orgTweetView.findViewById(R.id.quote_tweet_holder);
            TextView qut0 = null;
            TextView qut1 = null;
            TextView qut2 = null;
            if (fl != null) {
                qut0 = (TextView) fl.findViewById(R.id.tw__tweet_text);
                qut1 = (TextView) fl.findViewById(R.id.tw__tweet_author_full_name);
                qut2 = (TextView) fl.findViewById(R.id.tw__tweet_author_screen_name);
            }
            t1.setVisibility(View.GONE);
            t2.setVisibility(View.GONE);
            t3.setVisibility(View.GONE);
            t4.setVisibility(View.GONE);
            t5.setVisibility(View.GONE);
            t6.setTextColor(ContextCompat.getColor(mContext, R.color.textFront));
            if (qut0 != null) {
                qut0.setTextColor(ContextCompat.getColor(mContext, R.color.textFront));
                qut1.setTextColor(ContextCompat.getColor(mContext, R.color.textFront));
                qut2.setTextColor(ContextCompat.getColor(mContext, R.color.textFrontLight));
            }
            avatar.setVisibility(View.GONE);
            tContent.addView(orgTweetView);
        }
    }
}
