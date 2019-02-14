/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package de.digisocken.ReoTwe.tweetcomposer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.twitter.sdk.android.core.GuestSessionProvider;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.internal.network.UrlUtils;
import com.twitter.sdk.android.tweetcomposer.BuildConfig;

import java.net.URL;
import java.util.List;

/**
 * The TweetComposer Kit provides a lightweight mechanism for creating intents to interact with the installed Twitter app or a browser.
 */
public class TweetComposer {
    @SuppressLint("StaticFieldLeak")
    static volatile TweetComposer instance;
    private static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
    private static final String MIME_TYPE_JPEG = "image/jpeg";
    private static final String TWITTER_PACKAGE_NAME = "com.twitter.android";
    private static final String WEB_INTENT = "https://twitter.com/intent/tweet?text=%s&url=%s";

    SessionManager<TwitterSession> sessionManager;
    GuestSessionProvider guestSessionProvider;
    Context context;

    public static TweetComposer getInstance() {
        if (instance == null) {
            synchronized (TweetComposer.class) {
                if (instance == null) {
                    instance = new TweetComposer();
                }
            }
        }
        return instance;
    }

    TweetComposer() {
        sessionManager = TwitterCore.getInstance().getSessionManager();
        guestSessionProvider = TwitterCore.getInstance().getGuestSessionProvider();
        context = Twitter.getInstance().getContext(getIdentifier());
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_NUMBER;
    }

    public String getIdentifier() {
        return BuildConfig.GROUP + ":" + BuildConfig.ARTIFACT_ID;
    }

    /**
     * The TweetComposer Builder will use the installed Twitter instance and fall back to a browser
     */
    public static class Builder {
        private final Context context;
        private String text;
        private URL url;
        private Uri imageUri;

        /**
         * Initializes a new {@link com.twitter.sdk.android.tweetcomposer.TweetComposer.Builder}
         */
        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context;
        }

        /**
         * Sets Text for Tweet Intent, no length validation is performed
         */
        public Builder text(String text) {
            if (text == null) {
                throw new IllegalArgumentException("text must not be null.");
            }

            if (this.text != null) {
                throw new IllegalStateException("text already set.");
            }
            this.text = text;

            return this;
        }

        /**
         * Sets URL for Tweet Intent, no length validation is performed
         */
        public Builder url(URL url) {
            if (url == null) {
                throw new IllegalArgumentException("url must not be null.");
            }

            if (this.url != null) {
                throw new IllegalStateException("url already set.");
            }
            this.url = url;

            return this;
        }
        /**
         * Sets Image {@link Uri} for the Tweet. Only valid if the Twitter App is
         * installed.
         * The Uri should be a file Uri to a local file (e.g. <pre><code>Uri.fromFile(someExternalStorageFile)</code></pre>))
         */
        public Builder image(Uri imageUri) {
            if (imageUri == null) {
                throw new IllegalArgumentException("imageUri must not be null.");
            }

            if (this.imageUri != null) {
                throw new IllegalStateException("imageUri already set.");
            }
            this.imageUri = imageUri;

            return this;
        }

        /**
         * Creates {@link Intent} based on data in {@link com.twitter.sdk.android.tweetcomposer.TweetComposer.Builder}
         * @return an Intent to the Twitter for Android or a web intent.
         */
        public Intent createIntent() {
            Intent intent = createTwitterIntent();

            if (intent == null) {
                intent = createWebIntent();
            }

            return intent;
        }

        Intent createTwitterIntent() {
            final Intent intent = new Intent(Intent.ACTION_SEND);

            final StringBuilder builder = new StringBuilder();

            if (!TextUtils.isEmpty(text)) {
                builder.append(text);
            }

            if (url != null) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(url.toString());
            }

            intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
            intent.setType(MIME_TYPE_PLAIN_TEXT);

            if (imageUri != null) {
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.setType(MIME_TYPE_JPEG);
            }

            final PackageManager packManager = context.getPackageManager();
            final List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo: resolvedInfoList){
                if (resolveInfo.activityInfo.packageName.startsWith(TWITTER_PACKAGE_NAME)){
                    intent.setClassName(resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name);
                    return intent;
                }
            }

            return null;
        }

        Intent createWebIntent() {
            final String url = (this.url == null ? "" : this.url.toString());

            final String tweetUrl =
                    String.format(WEB_INTENT, UrlUtils.urlEncode(text), UrlUtils.urlEncode(url));
            return new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));
        }

        /**
         * Starts the intent created in {@link com.twitter.sdk.android.tweetcomposer.TweetComposer.Builder#createIntent()}
         */
        public void show() {
            final Intent intent = createIntent();
            context.startActivity(intent);
        }
    }
}
