package de.digisocken.twthaar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {
    public ListView mListView;
    public ArrayList<Tweet> tweetArrayList;
    public TweetAdapter adapter;
    public ImageButton button;
    public EditText editText;
    public RelativeLayout searchBox;
    public Stack< String > history;

    private String iniQuery;

    TwitterLoginButton loginButton;
    TwitterSession session = null;
    MyTwitterApiClient twitterApiClient;
    MyTwitterApiClient.FriendsService fs;
    String username = "";
    String friendlist;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi2 = menu.findItem(R.id.action_allImages);
        mi2.setChecked(TwthaarApp.mPreferences.getBoolean("imageful", false));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggleSearch:
                if (searchBox.getVisibility() == View.GONE) {
                    try {
                        ActionBar ab = getSupportActionBar();
                        if(ab != null) ab.setElevation(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    searchBox.setVisibility(View.VISIBLE);
                } else {
                    try {
                        ActionBar ab = getSupportActionBar();
                        if(ab != null) ab.setElevation(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    searchBox.setVisibility(View.GONE);
                }
                break;
            case R.id.action_makeTweet:
                if (username.equals("")) {
                    loginButton.callOnClick();
                } else {
                    String[] qeris = editText.getText().toString().split(",");
                    final Intent intent = new ComposerActivity.Builder(MainActivity.this)
                            .session(session)
                            .text(qeris[0])
                            .createIntent();
                    startActivity(intent);
                }
                break;
            case R.id.action_allImages:
                if (item.isChecked()) {
                    adapter.imageful = false;
                    TwthaarApp.mPreferences.edit().putBoolean("imageful", false).apply();
                    item.setChecked(false);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.imageful = true;
                    TwthaarApp.mPreferences.edit().putBoolean("imageful", true).apply();
                    item.setChecked(true);
                    adapter.notifyDataSetChanged();
                }
                break;
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
                ab.setTitle(" " + getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
                ab.setElevation(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchBox = (RelativeLayout) findViewById(R.id.searchBox);
        mListView = (ListView) findViewById(R.id.list);
        button = (ImageButton) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        loginButton = new TwitterLoginButton(this);

        iniQuery = TwthaarApp.mPreferences.getString("STARTUSERS", getString(R.string.defaultStarts));
        editText.setText(iniQuery);
        button.setOnClickListener(new SearchListener(this, ""));

        tweetArrayList = new ArrayList<>();
        history = new Stack<>();
        history.push(iniQuery);
        adapter = new TweetAdapter(this, tweetArrayList);
        mListView.setAdapter(adapter);

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                session = result.data;
                username = session.getUserName();
                startGetFriendlist(username);
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, getString(R.string.youNeedApiKey), Toast.LENGTH_LONG).show();
            }
        });

        session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (session != null) {
            username = session.getUserName();
        }

        button.callOnClick();
    }

    public void sortTweetArrayList() {
        Collections.sort(tweetArrayList, new Comparator<Tweet>() {
            @Override
            public int compare(Tweet t2, Tweet t1) {
                try {
                    Date parsedDate1 = TwthaarApp.formatIn.parse(t1.createdAt);
                    Date parsedDate2 = TwthaarApp.formatIn.parse(t2.createdAt);
                    return parsedDate1.compareTo(parsedDate2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    private void startGetFriendlist(String query) {
        friendlist = "";
        twitterApiClient = new MyTwitterApiClient(
                TwitterCore.getInstance().getSessionManager().getActiveSession()
        );
        fs = twitterApiClient.getFriendsService();
        Call<MyTwitterApiClient.UsersCursor> call = fs.friends(
                query,
                null,
                TwthaarApp.DEFAULT_MAX,
                true,
                false
        );
        call.enqueue(new FriendCallback(query));
    }


    class FriendCallback extends Callback<MyTwitterApiClient.UsersCursor> {
        String _query;

        FriendCallback(String query) {
            _query = query;
        }

        @Override
        public void success(Result<MyTwitterApiClient.UsersCursor> result) {
            for (final User user: result.data.users) {
                friendlist += "@" + user.screenName + ",";
            }
            if (result.data.nextCursor > 0) {
                Call<MyTwitterApiClient.UsersCursor> call = fs.friends(
                        _query,
                        (int) result.data.nextCursor,
                        TwthaarApp.DEFAULT_MAX,
                        true,
                        false
                );
                call.enqueue(new FriendCallback(_query));
            } else {
                // friendlist is ready
                if (!friendlist.equals("")) {
                    friendlist = "@" + username + "," + friendlist;
                    friendlist = friendlist.substring(0, friendlist.lastIndexOf(","));
                    TwthaarApp.mPreferences.edit().putString("STARTUSERS", friendlist).apply();
                }
            }
        }

        @Override
        public void failure(TwitterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (history.size() > 0) {
            editText.setText(history.pop());
            button.callOnClick();
        } else {
            history.push(iniQuery);
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(String.format(getString(R.string.closing), getString(R.string.app_name)))
                    .setMessage(getString(R.string.sureToClose))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            realExit();
                        }

                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    public void realExit() {
        super.onBackPressed();
    }
}
