package com.codepath.apps.restclienttemplate.models;

import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    //public String imageURL;
    public User user;
    public String tweetImageURL;
    public String relativeTimestamp;
    public long id;
    public String idString;
    // default status is false
    public boolean isLiked = false;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    static final String TWITTERFORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    // empty constructor needed by Parceler library
    public Tweet() {}

    // parsing each tweet represented by a jsonObject
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        //System.out.println("json object: " + jsonObject);

        if (jsonObject.has("full_text")) {
            tweet.body = jsonObject.getString("full_text");
        } else {
            tweet.body = jsonObject.getString("text");
        }
        tweet.createdAt = jsonObject.getString("created_at");

        // fromJson takes a JSONObect and converts to a User object.
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.id = jsonObject.getLong("id");
        tweet.idString = jsonObject.getString("id_str");

        // each tweet has "entities" section
        JSONObject entities = jsonObject.getJSONObject("entities");
        //System.out.println("entities: " + entities);
        if (entities.has("media")) {
            //System.out.println("media: " + entities.getJSONArray("media"));
            JSONArray media = entities.getJSONArray("media");
            JSONObject firstImage = (JSONObject) media.get(0);
            tweet.tweetImageURL = firstImage.getString("media_url_https");
        }

        tweet.relativeTimestamp = tweet.getRelativeTimeAgo(tweet.createdAt);

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Tweet tweet = fromJson(jsonArray.getJSONObject(i));
            tweets.add(tweet);
        }
        return tweets;
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            Log.i("TAG", "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }

    public String getBody() {
        return body;
    }

    public String getImageURL() {
        return tweetImageURL;
    }

    public String getTweetImageURL() {
        return tweetImageURL;
    }

    public User getUser() {
        return user;
    }

    public long getId() {
        return id;
    }
}
