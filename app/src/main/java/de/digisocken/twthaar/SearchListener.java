package de.digisocken.twthaar;

import android.content.Context;
import android.view.View;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

public class SearchListener implements View.OnClickListener {
    MainActivity mainActivity;
    String iniQuery;

    public SearchListener(Context mContext, String q) {
        mainActivity = (MainActivity) mContext;
        iniQuery = q;
    }

    @Override
    public void onClick(View v) {
        if (mainActivity.tweetArrayList != null) mainActivity.tweetArrayList.clear();

        String query = iniQuery;
        if (iniQuery.equals("")) {
            query = mainActivity.editText.getText().toString();
        } else {
            mainActivity.editText.setText(query);
        }
        String[] queries = query.split(",");

        for (String q : queries) {
            q = q.trim();
            if (q.contains("#")) {
                q = q.replace("#","");
                SearchTimeline searchTimeline = new SearchTimeline.Builder()
                        .query(q)
                        .maxItemsPerRequest(TwthaarApp.DEFAULT_MAX)
                        .build();

                searchTimeline.next(null, new Callback<TimelineResult<Tweet>>() {
                    @Override
                    public void success(Result<TimelineResult<Tweet>> result) {
                        for(final Tweet tweet : result.data.items) {
                            mainActivity.tweetArrayList.add(tweet);
                        }
                        mainActivity.sortTweetArrayList();
                        mainActivity.adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void failure(TwitterException e) {
                        e.printStackTrace();
                    }
                });

            } else {
                q = q.replace("@","");
                UserTimeline userTimeline = new UserTimeline.Builder()
                        .screenName(q)
                        .maxItemsPerRequest(TwthaarApp.DEFAULT_MAX)
                        .build();

                userTimeline.next(null, new Callback<TimelineResult<Tweet>>() {
                    @Override
                    public void success(Result<TimelineResult<Tweet>> result) {
                        for(final Tweet tweet : result.data.items) {
                            mainActivity.tweetArrayList.add(tweet);
                        }
                        mainActivity.sortTweetArrayList();
                        mainActivity.adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void failure(TwitterException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
