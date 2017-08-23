package de.digisocken.twthaar;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {
    LinearLayout list;
    ImageButton button;
    EditText editText;
    TwitterLoginButton loginButton;
    TwitterSession session;
    MyTwitterApiClient twitterApiClient;
    MyTwitterApiClient.FriendsService fs;
    String username = "";

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
        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                session = result.data;
                username = session.getUserName();
                editText.setText(username);
                loginButton.setVisibility(View.GONE);
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
                TextView tv = new TextView(getApplicationContext());
                tv.setText(getString(R.string.ups));
                list.addView(tv);
            }
        });

        if (TwitterCore.getInstance().getSessionManager().getActiveSession() != null) {
            username = TwitterCore.getInstance().getSessionManager().getActiveSession().getUserName();
            editText.setText(username);
        } else {
            loginButton.setVisibility(View.VISIBLE);
        }

        button.setOnClickListener(new MyOnClickListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    class FriendCallback extends Callback<MyTwitterApiClient.UsersCursor> {
        String _query;

        FriendCallback(String query) {
            _query = query;
        }

        @Override
        public void success(Result<MyTwitterApiClient.UsersCursor> result) {
            for (final User user: result.data.users) {
                Button btn = new Button(getApplicationContext());
                btn.setText(user.screenName);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setText(user.screenName);
                    }
                });
                list.addView(btn);
            }
            if (result.data.nextCursor > 0) {
                Call<MyTwitterApiClient.UsersCursor> call = fs.friends(
                        _query,
                        (int) result.data.nextCursor,
                        TwthaarApp.mPreferences.getInt("MAXFRIENDS", TwthaarApp.DEFAULT_MAXFRIENDS),
                        true,
                        false
                );
                call.enqueue(new FriendCallback(_query));
            }
        }

        @Override
        public void failure(TwitterException e) {
            e.printStackTrace();
            TextView tv = new TextView(getApplicationContext());
            tv.setText(getString(R.string.ups));
            list.addView(tv);
        }
    }

    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            list.removeAllViews();
            String query = editText.getText().toString().replace("@", "");
            UserTimeline userTimeline = new UserTimeline.Builder()
                    .screenName(query)
                    .maxItemsPerRequest(
                            TwthaarApp.mPreferences.getInt("MAXTIMELINE", TwthaarApp.DEFAULT_MAXTIMELINE)
                    )
                    .build();

            if (!Objects.equals(username, "")) {
                twitterApiClient = new MyTwitterApiClient(
                        TwitterCore.getInstance().getSessionManager().getActiveSession()
                );
                fs = twitterApiClient.getFriendsService();
                Call<MyTwitterApiClient.UsersCursor> call = fs.friends(
                        query,
                        null,
                        TwthaarApp.mPreferences.getInt("MAXFRIENDS", TwthaarApp.DEFAULT_MAXFRIENDS),
                        true,
                        false
                );
                call.enqueue(new FriendCallback(query));
            }

            userTimeline.next(null, new Callback<TimelineResult<Tweet>>() {
                SimpleDateFormat formatIn = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
                SimpleDateFormat formatOut = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
                @Override
                public void success(Result<TimelineResult<Tweet>> result) {
                    int i = 0;
                    for(final Tweet tweet : result.data.items) {

                        TextView td = new TextView(getApplicationContext());
                        td.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.tweetDate));
                        td.setTypeface(null, Typeface.BOLD);
                        td.setPadding(10,5,10,5);

                        TextView tv = new TextView(getApplicationContext());
                        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.tweetText));
                        tv.setText(tweet.text);
                        tv.setTypeface(null, Typeface.NORMAL);
                        tv.setPadding(15,0,15,10);
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int rtindex = tweet.text.indexOf("RT @");
                                if (rtindex >= 0) {
                                    String rtuser = tweet.text.substring(
                                            rtindex,
                                            tweet.text.indexOf(": ",rtindex)
                                    );
                                    editText.setText(rtuser.replace("RT @",""));
                                }
                            }
                        });

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
                        if (i%2 == 0) {
                            td.setBackgroundColor(
                                    ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)
                            );
                            tv.setBackgroundColor(
                                    ContextCompat.getColor(getApplicationContext(), R.color.tweetBack2)
                            );
                        } else {
                            td.setBackgroundColor(
                                    ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)
                            );
                            tv.setBackgroundColor(
                                    ContextCompat.getColor(getApplicationContext(), R.color.tweetBack1)
                            );
                        }
                        list.addView(td);
                        list.addView(tv);

                        i++;
                    }
                }
                @Override
                public void failure(TwitterException e) {
                    e.printStackTrace();
                    TextView tv = new TextView(getApplicationContext());
                    tv.setText(getString(R.string.ups));
                    list.addView(tv);
                }
            });
        }
    }
}
