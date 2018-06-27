package de.digisocken.ReoTwe;

import android.app.UiModeManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.tv.TvContract;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import de.digisocken.ReoTwe.tweetcomposer.ComposerActivity;

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

public class MainActivity extends AppCompatActivity {

    public ListView mListView;
    public ArrayList<Tweet> tweetArrayList;
    public TweetAdapter adapter;
    public SearchListener searchListener;
    public String iniQuery;
    TwitterLoginButton loginButton;


    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        super.onCreateOptionsMenu(menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        searchListener.run(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) { return false;}
                });

        MenuItemCompat.setOnActionExpandListener(
                searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        searchListener.run(iniQuery);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }
                });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi2 = menu.findItem(R.id.action_allImages);
        mi2.setChecked(App.mPreferences.getBoolean("imageful", true));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences2:
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            case R.id.action_allImages:
                if (item.isChecked()) {
                    adapter.imageful = false;
                    App.mPreferences.edit().putBoolean("imageful", false).apply();
                    item.setChecked(false);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.imageful = true;
                    App.mPreferences.edit().putBoolean("imageful", true).apply();
                    item.setChecked(true);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.action_flattr:
                Intent intentFlattr = new Intent(Intent.ACTION_VIEW, Uri.parse(App.FLATTR_LINK));
                startActivity(intentFlattr);
                break;
            case R.id.action_intro:
                Intent intentIntro = new Intent(MainActivity.this, IntroActivity.class);
                startActivity(intentIntro);
                break;
            case R.id.action_user_tags:
                Intent intentUt = new Intent(MainActivity.this, FeedSourcesActivity.class);
                startActivity(intentUt);
                break;
            case R.id.action_project:
                Intent intentProj= new Intent(Intent.ACTION_VIEW, Uri.parse(App.PROJECT_LINK));
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
        overridePendingTransition(0, 0);
        setContentView(R.layout.together);

        if (App.mPreferences.getBoolean("show_intro", true)) {
            App.mPreferences.edit().putBoolean("show_intro", false).commit();
            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, LogoActivity.class);
            startActivity(intent);
        }

        try {
            ActionBar ab = getSupportActionBar();
            if(ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setHomeButtonEnabled(true);
                ab.setDisplayUseLogoEnabled(true);
                ab.setLogo(R.mipmap.ic_launcher_white);
                ab.setTitle(" " + getString(R.string.app_name));
                ab.setElevation(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mListView = (ListView) findViewById(R.id.list);
        loginButton = new TwitterLoginButton(this);

        iniQuery = App.mPreferences.getString("STARTUSERS", getString(R.string.defaultStarts));
        searchListener = new SearchListener(this, iniQuery);

        tweetArrayList = new ArrayList<>();
        adapter = new TweetAdapter(this, tweetArrayList);
        mListView.setAdapter(adapter);

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                App.session = result.data;
                App.username = App.session.getUserName();
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, getString(R.string.youNeedApiKey), Toast.LENGTH_LONG).show();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.username.equals("")) {
                    loginButton.callOnClick();
                } else {
                    final Intent intent;

                    if (App.umm.getNightMode() == UiModeManager.MODE_NIGHT_YES) {

                        intent = new ComposerActivity.Builder(MainActivity.this)
                                .session(App.session)
                                .darkTheme()
                                .createIntent();
                    } else {
                        intent = new ComposerActivity.Builder(MainActivity.this)
                                .session(App.session)
                                .createIntent();
                    }
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
                if (App.username.equals("")) {
                    loginButton.callOnClick();
                } else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        if (
                App.mPreferences.getString("CONSUMER_KEY","").equals("") ||
                App.mPreferences.getString("CONSUMER_SECRET","").equals("")
        ) {
            ViewGroup hintView = (ViewGroup) getLayoutInflater().inflate(R.layout.tweet_item, null);
            mListView.addFooterView(hintView);
        }
        searchListener.run(null);
    }

    public void sortTweetArrayList() {
        Collections.sort(tweetArrayList, new Comparator<Tweet>() {
            @Override
            public int compare(Tweet t2, Tweet t1) {
                try {
                    Date parsedDate1 = App.formatIn.parse(t1.createdAt);
                    Date parsedDate2 = App.formatIn.parse(t2.createdAt);
                    return parsedDate1.compareTo(parsedDate2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }


    @Override
    public void onBackPressed() {

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

                final Intent intent;

                // make image tweet
                if (App.umm.getNightMode() == UiModeManager.MODE_NIGHT_YES) {

                    intent = new ComposerActivity.Builder(MainActivity.this)
                            .session(App.session)
                            .image(imgUri)
                            .darkTheme()
                            .createIntent();
                } else {
                    intent = new ComposerActivity.Builder(MainActivity.this)
                            .session(App.session)
                            .image(imgUri)
                            .createIntent();
                }
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            loginButton.onActivityResult(requestCode, resultCode, data);
        }

    }

    public void realExit() {
        super.onBackPressed();
    }

}
