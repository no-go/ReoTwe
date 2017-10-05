package de.digisocken.twthaar;

import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;
import com.twitter.sdk.android.tweetui.TweetView;

import android.support.design.widget.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Stack;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public ListView mListView;
    public ArrayList<Tweet> tweetArrayList;
    public TweetAdapter adapter;
    public ImageButton button;
    public ImageButton buttonRevert;
    public ImageButton buttonAddStart;
    public EditText editText;
    public RelativeLayout searchBox;
    public Stack< String > history;
    private UiModeManager umm;

    private String iniQuery;

    TwitterLoginButton loginButton;
    TwitterSession session = null;
    MyTwitterApiClient twitterApiClient;
    MyTwitterApiClient.FriendsService fs;
    String username = "";
    String friendlist;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi2 = menu.findItem(R.id.action_allImages);
        mi2.setChecked(TwthaarApp.mPreferences.getBoolean("imageful", true));
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
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.together);
        umm = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);

        try {
            ActionBar ab = getSupportActionBar();
            if(ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setHomeButtonEnabled(true);
                ab.setDisplayUseLogoEnabled(true);
                ab.setLogo(R.mipmap.ic_launcher);
                if (BuildConfig.DEBUG) {
                    ab.setTitle(" " + getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
                } else {
                    ab.setTitle(" " + getString(R.string.app_name));
                }
                ab.setElevation(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchBox = (RelativeLayout) findViewById(R.id.searchBox);
        mListView = (ListView) findViewById(R.id.list);
        button = (ImageButton) findViewById(R.id.button);
        buttonRevert = (ImageButton) findViewById(R.id.cleanUpBtn);
        buttonAddStart = (ImageButton) findViewById(R.id.addStartBtn);
        editText = (EditText) findViewById(R.id.editText);
        loginButton = new TwitterLoginButton(this);

        iniQuery = TwthaarApp.mPreferences.getString("STARTUSERS", getString(R.string.defaultStarts));
        editText.setText(iniQuery);
        button.setOnClickListener(new SearchListener(this, ""));

        buttonRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        buttonAddStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] queries = editText.getText().toString().split(",");
                for (String q : queries) {
                    q = q.trim();
                    if (q.startsWith("#")) {
                        if(!iniQuery.contains(q)) {
                            iniQuery += "," + q;
                        }
                    } else if (q.startsWith("@")) {
                        if(!iniQuery.contains(q)) {
                            iniQuery += "," + q;
                        }
                    }
                }
                TextView favouriteList = (TextView) findViewById(R.id.favouriteList);
                favouriteList.setText(iniQuery.replace(",","\n"));
                TwthaarApp.mPreferences.edit().putString("STARTUSERS",iniQuery).apply();
                Toast.makeText(MainActivity.this, R.string.favAdded, Toast.LENGTH_SHORT).show();
            }
        });

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
            
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            twitterApiClient.getAccountService().verifyCredentials(true, true, false).enqueue(new Callback<User>() {
                @Override
                public void failure(TwitterException e) {}

                @Override
                public void success(Result<User> userResult) {
                    if (userResult != null) {
                        TextView meTxt = (TextView) findViewById(R.id.fullname);
                        TextView meScrTxt = (TextView) findViewById(R.id.screenname);
                        meScrTxt.setText("@" + username);
                        meTxt.setText(userResult.data.name);
                        TextView favouriteList = (TextView) findViewById(R.id.favouriteList);
                        favouriteList.setText(
                                TwthaarApp.mPreferences.getString("STARTUSERS", getString(R.string.defaultStarts)).replace(",","\n")
                        );
                    }
                }
            });
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        FloatingActionButton fabCam = (FloatingActionButton) findViewById(R.id.fabCam);
        int numberOfCameras = Camera.getNumberOfCameras();

        PackageManager pm = getPackageManager();
        final boolean deviceHasCameraFlag = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        if( !deviceHasCameraFlag || numberOfCameras==0 ) fabCam.setVisibility(View.GONE);

        fabCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.equals("")) {
                    loginButton.callOnClick();
                } else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (TwthaarApp.mPreferences.getString("CONSUMER_KEY","").equals("") || TwthaarApp.mPreferences.getString("CONSUMER_SECRET","").equals("") ) {
            ViewGroup hintView = (ViewGroup) getLayoutInflater().inflate(R.layout.tweet_item, null);
            mListView.addFooterView(hintView);
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

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        TextView favouriteList = (TextView) findViewById(R.id.favouriteList);
        favouriteList.setText(
                TwthaarApp.mPreferences.getString("STARTUSERS", getString(R.string.defaultStarts)).replace(",","\n")
        );
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_preferences) {
            Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }else if (id == R.id.action_info) {
            Intent intentProj= new Intent(Intent.ACTION_VIEW, Uri.parse(TwthaarApp.PROJECT_LINK));
            startActivity(intentProj);
        } else {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            Snackbar.make(
                    drawer,
                    "Replace with your own action",
                    Snackbar.LENGTH_LONG
            ).setAction("Action", null).show();
        }

        return true;
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // image preview
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();

            try {
                File mTmpFile = File.createTempFile("tmp", ".png", getCacheDir());
                FileOutputStream fos = new FileOutputStream(mTmpFile);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                Uri imgUri = Uri.fromFile(mTmpFile);

                // make image tweet
                final Intent intent = new ComposerActivity.Builder(MainActivity.this)
                        .session(session)
                        .image(imgUri)
                        .createIntent();
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            loginButton.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    protected void onResume() {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean night = mPreferences.getBoolean("nightmode_use", true);
        if (night) {
            int startH = mPreferences.getInt("nightmode_use_start", TwthaarApp.DEFAULT_NIGHT_START);
            int stopH = mPreferences.getInt("nightmode_use_stop", TwthaarApp.DEFAULT_NIGHT_STOP);
            if (TwthaarApp.inTimeSpan(startH, stopH) && umm.getNightMode() != UiModeManager.MODE_NIGHT_YES) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_YES);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            if (!TwthaarApp.inTimeSpan(startH, stopH) && umm.getNightMode() != UiModeManager.MODE_NIGHT_NO) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } else {
            if (umm.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        super.onResume();
    }

    public void realExit() {
        super.onBackPressed();
    }
}
