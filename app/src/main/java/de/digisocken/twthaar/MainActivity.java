package de.digisocken.twthaar;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class MainActivity extends AppCompatActivity {
    LinearLayout list;
    ImageButton button;
    EditText editText;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_flattr:
                Intent intentFlattr = new Intent(Intent.ACTION_VIEW, Uri.parse(TwthaarApp.FLATTR_LINK));
                startActivity(intentFlattr);
                break;
            case R.id.action_project:
                Intent intentProj= new Intent(Intent.ACTION_VIEW, Uri.parse(TwthaarApp.PROJECT_LINK));
                startActivity(intentProj);
                break;
            case R.id.action_preferences:
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            ActionBar ab = getSupportActionBar();
            if(ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setHomeButtonEnabled(true);
                ab.setDisplayUseLogoEnabled(true);
                ab.setLogo(R.mipmap.ic_launcher);
                ab.setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        list = (LinearLayout) findViewById(R.id.list);
        button = (ImageButton) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        editText.setText(TwthaarApp.mPreferences.getString("STARTUSER", TwthaarApp.DEFAULT_STARTUSER));
        button.setOnClickListener(new MyOnClickListener(""));
        button.callOnClick();
    }



    class MyOnClickListener implements View.OnClickListener {
        String query;

        public MyOnClickListener(String q) {
            query = q;
            if (q.equals("")) query = editText.getText().toString();
        }

        @Override
        public void onClick(View v) {
            editText.setText(query);
            list.removeAllViews();
            final ArrayList<View> tweetViews = new ArrayList<>();
            final ArrayList<String> maybeFriend = new ArrayList<>();
            UserTimeline userTimeline = new UserTimeline.Builder()
                    .screenName(query)
                    .maxItemsPerRequest(TwthaarApp.DEFAULT_MAXTIMELINE)
                    .build();

            userTimeline.next(null, new Callback<TimelineResult<Tweet>>() {
                SimpleDateFormat formatIn = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
                SimpleDateFormat formatOut = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
                @Override
                public void success(Result<TimelineResult<Tweet>> result) {
                    int i = 0;
                    for(final Tweet tweet : result.data.items) {

                        TextView td = new TextView(getApplicationContext());
                        td.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                        td.setTypeface(null, Typeface.BOLD);
                        td.setPadding(10,5,10,5);

                        TextView tv = new TextView(getApplicationContext());
                        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.tweetText));
                        tv.setText(tweet.text);
                        tv.setTypeface(null, Typeface.NORMAL);
                        tv.setPadding(15,0,15,10);

                        int rtindex = tweet.text.indexOf("RT @");
                        if (rtindex >= 0) {
                            String rtuser = tweet.text.substring(
                                    rtindex,
                                    tweet.text.indexOf(": ", rtindex)
                            );
                            rtuser = rtuser.replace("RT @", "");
                            maybeFriend.add(rtuser);
                            tv.setOnClickListener(new MyOnClickListener(rtuser));
                        }

                        Date parsedDate = null;
                        String rtAndLikes = " ⇆ " +
                                Integer.toString(tweet.retweetCount) +
                                " ★ " + Integer.toString(tweet.favoriteCount);
                        try {
                            parsedDate = formatIn.parse(tweet.createdAt);
                            td.setText(formatOut.format(parsedDate) + rtAndLikes);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            td.setText(tweet.createdAt + rtAndLikes);
                        }
                        tweetViews.add(td);
                        tweetViews.add(tv);

                        i++;
                    }
                    for (String friend : maybeFriend) {
                        Log.d(TwthaarApp.TAG, friend);
                        Button btn = new Button(getApplicationContext());
                        btn.setOnClickListener(new MyOnClickListener(friend));
                        list.addView(btn);
                    }
                    for (View view : tweetViews) {
                        list.addView(view);
                    }
                }
                @Override
                public void failure(TwitterException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
