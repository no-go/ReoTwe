package de.digisocken.ReoTwe;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

public class SearchListener implements View.OnClickListener {
    MainActivity mainActivity;
    volatile int loaded;
    private String _query;

    public SearchListener(Context mContext, String q) {
        mainActivity = (MainActivity) mContext;
        _query = q;
        loaded = 0;
    }

    public void run(String query) {

        if (mainActivity.tweetArrayList != null) {
            mainActivity.tweetArrayList.clear();
        }

        if (query != null) _query = query.replace(" ", ",");
        final String[] queries = _query.split(",");
        loaded = 0;

        for (String q : queries) {
            q = q.trim();
            if (q.startsWith("#")) {
                q = q.replace("#","");
                SearchTimeline searchTimeline = new SearchTimeline.Builder()
                        .query(q)
                        .resultType(SearchTimeline.ResultType.POPULAR)
                        .maxItemsPerRequest(App.DEFAULT_MAX)
                        .build();

                searchTimeline.next(null, new Callback<TimelineResult<Tweet>>() {
                    @Override
                    public void success(Result<TimelineResult<Tweet>> result) {
                        for(final Tweet tweet : result.data.items) {
                            mainActivity.tweetArrayList.add(tweet);
                            if (BuildConfig.DEBUG) Log.d(App.TAG, "tweet created at " + tweet.createdAt);
                        }
                        loaded++;
                        if (loaded == queries.length) {
                            Toast.makeText(mainActivity.getApplicationContext(), R.string.ready, Toast.LENGTH_SHORT).show();
                        }
                        mainActivity.sortTweetArrayList();
                        mainActivity.adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void failure(TwitterException e) {
                        if (
                                !App.mPreferences.getString("CONSUMER_KEY","").equals("") ||
                                        !App.mPreferences.getString("CONSUMER_SECRET","").equals("")
                                ) {
                            View v = mainActivity.getLayoutInflater().inflate(R.layout.tweet_oops, null);
                            mainActivity.mListView.addFooterView(v);
                        }
                    }
                });

            } else {
                q = q.replace("@","");
                UserTimeline userTimeline = new UserTimeline.Builder()
                        .screenName(q)
                        .maxItemsPerRequest(App.DEFAULT_MAX)
                        .build();

                userTimeline.next(null, new Callback<TimelineResult<Tweet>>() {
                    @Override
                    public void success(Result<TimelineResult<Tweet>> result) {
                        for(final Tweet tweet : result.data.items) {
                            mainActivity.tweetArrayList.add(tweet);
                            if (BuildConfig.DEBUG) Log.d(App.TAG, "tweet created at " + tweet.createdAt);
                        }
                        loaded++;
                        if (loaded == queries.length) {
                            Toast.makeText(mainActivity.getApplicationContext(), R.string.ready, Toast.LENGTH_SHORT).show();
                        }
                        mainActivity.sortTweetArrayList();
                        mainActivity.adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void failure(TwitterException e) {
                        if (
                                !App.mPreferences.getString("CONSUMER_KEY","").equals("") ||
                                        !App.mPreferences.getString("CONSUMER_SECRET","").equals("")
                                ) {
                            View v = mainActivity.getLayoutInflater().inflate(R.layout.tweet_oops, null);
                            mainActivity.mListView.addFooterView(v);
                        }
                    }
                });
            }
        }

    }

    @Override
    public void onClick(View v) {
        run(null);
    }
}
