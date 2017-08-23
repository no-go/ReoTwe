package de.digisocken.twthaar;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.io.UnsupportedEncodingException;

public class TwthaarApp extends Application {
    public static final String TAG = TwthaarApp.class.getSimpleName();
    public static SharedPreferences mPreferences;

    public static final String PROJECT_LINK = "https://github.com/no-go/Twthaar";
    public static final String FLATTR_ID = "o6wo7q";
    public static String FLATTR_LINK;

    public static final int DEFAULT_MAXFRIENDS  = 15;
    public static final int DEFAULT_MAXTIMELINE = 90;

    public void onCreate() {
        super.onCreate();

        try {
            FLATTR_LINK = "https://flattr.com/submit/auto?fid="+FLATTR_ID+"&url="+
                    java.net.URLEncoder.encode(PROJECT_LINK, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(
                        mPreferences.getString("CONSUMER_KEY",""),
                        mPreferences.getString("CONSUMER_SECRET","")
                ))
                .debug(BuildConfig.DEBUG)
                .build();
        Twitter.initialize(config);
    }
}
