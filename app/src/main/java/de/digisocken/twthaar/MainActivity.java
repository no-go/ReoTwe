package de.digisocken.twthaar;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private ArrayList<Tweet> tweetArrayList;
    private TweetAdapter adapter;
    private ImageButton button;
    private EditText editText;

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
                ab.setElevation(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mListView = (ListView) findViewById(R.id.list);
        button = (ImageButton) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        editText.setText(TwthaarApp.mPreferences.getString("STARTUSER", TwthaarApp.DEFAULT_STARTUSER));
        button.setOnClickListener(new MyOnClickListener(""));
        button.callOnClick();

        tweetArrayList = new ArrayList<>();
        adapter = new TweetAdapter(this, tweetArrayList);
        mListView.setAdapter(adapter);
    }



    class MyOnClickListener implements View.OnClickListener {
        String iniQuery;

        public MyOnClickListener(String q) {
            iniQuery = q;
        }

        @Override
        public void onClick(View v) {
            String query = iniQuery;
            if (iniQuery.equals("")) {
                query = editText.getText().toString();
            }

            final ArrayList<String> maybeFriend = new ArrayList<>();
            UserTimeline userTimeline = new UserTimeline.Builder()
                    .screenName(query)
                    .maxItemsPerRequest(TwthaarApp.DEFAULT_MAXTIMELINE)
                    .build();

            userTimeline.next(null, new Callback<TimelineResult<Tweet>>() {

                @Override
                public void success(Result<TimelineResult<Tweet>> result) {
                    tweetArrayList.clear();
                    for(final Tweet tweet : result.data.items) {
                        tweetArrayList.add(tweet);
                        String rtuser = TweetAdapter.extractRT(tweet.text);
                        if (rtuser != null) maybeFriend.add(rtuser);
                    }
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void failure(TwitterException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
