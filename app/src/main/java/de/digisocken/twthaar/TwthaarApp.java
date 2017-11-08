package de.digisocken.twthaar;

import android.app.Application;
import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.TextView;

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
import com.twitter.sdk.android.core.models.User;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TwthaarApp extends Application {
    public static final String TAG = TwthaarApp.class.getSimpleName();
    public static SharedPreferences mPreferences;

    public static final String PROJECT_LINK = "https://github.com/no-go/Twthaar";
    public static final String FLATTR_ID = "o6wo7q";
    public static String FLATTR_LINK;

    public static final int DEFAULT_MAX = 50;

    public static SimpleDateFormat formatIn = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
    public static SimpleDateFormat formatOut = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    public static final int DEFAULT_NIGHT_START = 18;
    public static final int DEFAULT_NIGHT_STOP = 6;

    public static UiModeManager umm;
    public static boolean night = false;
    public static TwitterSession session = null;
    public static String username = "";
    public static String realname = "";
    public static MyTwitterApiClient twitterApiClient;


    public void onCreate() {
        super.onCreate();

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
            int startH = mPreferences.getInt("nightmode_use_start", TwthaarApp.DEFAULT_NIGHT_START);
            int stopH = mPreferences.getInt("nightmode_use_stop", TwthaarApp.DEFAULT_NIGHT_STOP);
            if (inTimeSpan(startH, stopH) && umm.getNightMode() != UiModeManager.MODE_NIGHT_YES) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_YES);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                night = true;
            }
            if (!inTimeSpan(startH, stopH) && umm.getNightMode() != UiModeManager.MODE_NIGHT_NO) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                night = false;
            }
        } else {
            if (umm.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
                umm.setNightMode(UiModeManager.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                night = false;
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

        if (TwthaarApp.session != null) {
            username = TwthaarApp.session.getUserName();
            //twitterApiClient = TwitterCore.getInstance().getApiClient();

            twitterApiClient = new MyTwitterApiClient(
                    TwitterCore.getInstance().getSessionManager().getActiveSession()
            );

            twitterApiClient.getAccountService().verifyCredentials(true, true, false).enqueue(new Callback<User>() {
                @Override
                public void failure(TwitterException e) {}

                @Override
                public void success(Result<User> userResult) {
                    if (userResult != null) {
                        realname = userResult.data.name;
                    }
                }
            });
        }
    }

    public static boolean inTimeSpan(int startH, int stopH) {
        int nowH = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (startH == stopH && startH == nowH) return true;
        if (startH > stopH && (nowH <= stopH || nowH >= startH)) return true;
        if (startH < stopH && nowH >= startH && nowH <= stopH) return true;
        return false;
    }
}
