package de.digisocken.twthaar;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MyTwitterApiClient  extends TwitterApiClient {
    public MyTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public FriendsService getFriendsService() {
        return getService(FriendsService.class);
    }

    interface FriendsService {
        @GET("/1.1/friends/list.json")
        Call<UsersCursor> friends(
            @Query("screen_name") String screen_name,
            @Query("cursor") Integer cursor,
            @Query("count") Integer count,
            @Query("skip_status") boolean skip_status,
            @Query("include_user_entities") boolean include_user_entities);
    }

    class UsersCursor {
        @SerializedName("previous_cursor")
        public final long previousCursor;

        @SerializedName("previous_cursor_str")
        public final String previousCursorStr;

        @SerializedName("users")
        public final List<User> users;

        @SerializedName("next_cursor")
        public final long nextCursor;

        public UsersCursor(int previousCursor, String previousCursorStr, long nextCursor, List<User> users) {
            this.previousCursor = previousCursor;
            this.nextCursor = nextCursor;
            this.users = users;
            this.previousCursorStr = previousCursorStr;
        }
    }
}
