package de.digisocken.ReoTwe;

import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;

public class App extends Application {
    public static final String TAG = App.class.getSimpleName();
    public static SharedPreferences mPreferences;

    public static final String PROJECT_LINK = "https://no-go.github.io/ReoTwe/";
    public static final String FLATTR_ID = "o6wo7q";
    public static String FLATTR_LINK;

    public static final int DEFAULT_MAX = 40;

    public static SimpleDateFormat formatIn = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    public static SimpleDateFormat formatOut = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    public static final int DEFAULT_NIGHT_START = 18;
    public static final int DEFAULT_NIGHT_STOP = 6;

    public static UiModeManager umm;
    public static TwitterSession session = null;
    public static String username = "";
    public static String realname = "";
    public static MyTwitterApiClient twitterApiClient;

    public MyTwitterApiClient.FriendsService fs;
    public String friendlist;
    private static Context contextOfApplication;

    public void onCreate() {
        super.onCreate();
        contextOfApplication = getApplicationContext();

        try {
            FLATTR_LINK = "https://flattr.com/submit/auto?fid="+FLATTR_ID+"&url="+
                    java.net.URLEncoder.encode(PROJECT_LINK, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        umm = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);

        if (!mPreferences.contains("nightmode_use_start")) {
            mPreferences.edit().putInt("nightmode_use_start", DEFAULT_NIGHT_START).commit();
        }
        if (!mPreferences.contains("show_intro")) {
            mPreferences.edit().putBoolean("show_intro", true).commit();
        }
        if (!mPreferences.contains("nightmode_use_stop")) {
            mPreferences.edit().putInt("nightmode_use_stop", DEFAULT_NIGHT_STOP).commit();
        }
        if (!mPreferences.contains("STARTUSERS")) {
            mPreferences.edit().putString("STARTUSERS", getString(R.string.defaultStarts)).commit();
        }
        if (!mPreferences.contains("nightmode_use")) {
            mPreferences.edit().putBoolean("nightmode_use", true).commit();
        }
        if (!mPreferences.contains("imageful")) {
            mPreferences.edit().putBoolean("imageful", true).commit();
        }

        if (mPreferences.getBoolean("nightmode_use", true)) {
            int startH = mPreferences.getInt("nightmode_use_start", App.DEFAULT_NIGHT_START);
            int stopH = mPreferences.getInt("nightmode_use_stop", App.DEFAULT_NIGHT_STOP);
            if (inTimeSpan(startH, stopH) && umm.getNightMode() != UiModeManager.MODE_NIGHT_YES) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_YES);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            if (!inTimeSpan(startH, stopH) && umm.getNightMode() != UiModeManager.MODE_NIGHT_NO) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } else {
            if (umm.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(
                        mPreferences.getString("CONSUMER_KEY",""),
                        mPreferences.getString("CONSUMER_SECRET","")
                ))
                .debug(BuildConfig.DEBUG)
                .build();
        Twitter.initialize(config);

        session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        if (App.session != null) {
            username = App.session.getUserName();
            //twitterApiClient = TwitterCore.getInstance().getApiClient();

            twitterApiClient = new MyTwitterApiClient(session);

            twitterApiClient.getAccountService().verifyCredentials(true, true, false).enqueue(new Callback<User>() {
                @Override
                public void failure(TwitterException e) {}

                @Override
                public void success(Result<User> userResult) {
                    if (userResult != null) {
                        realname = userResult.data.name;
                        startGetFriendlist(username);
                    }
                }
            });
        }
    }

    private void startGetFriendlist(String query) {
        friendlist = "";
        fs = App.twitterApiClient.getFriendsService();
        Call<MyTwitterApiClient.UsersCursor> call = fs.friends(
                query,
                null,
                App.DEFAULT_MAX,
                true,
                false
        );
        call.enqueue(new FriendCallback(query));
    }

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

    public static boolean inTimeSpan(int startH, int stopH) {
        int nowH = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (startH == stopH && startH == nowH) return true;
        if (startH > stopH && (nowH <= stopH || nowH >= startH)) return true;
        if (startH < stopH && nowH >= startH && nowH <= stopH) return true;
        return false;
    }

    public class FriendCallback extends Callback<MyTwitterApiClient.UsersCursor> {
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
                        App.DEFAULT_MAX,
                        true,
                        false
                );
                call.enqueue(new FriendCallback(_query));
            } else {
                // friendlist is ready
                if (!friendlist.equals("")) {
                    friendlist = "@" + username + "," + friendlist;
                    friendlist = friendlist.substring(0, friendlist.lastIndexOf(","));
                    App.mPreferences.edit().putString("STARTUSERS", friendlist).apply();
                }
            }
        }

        @Override
        public void failure(TwitterException e) {
            e.printStackTrace();
        }
    }
}
