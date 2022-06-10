package com.codepath.apps.restclienttemplate.models;

import android.content.SharedPreferences;
import android.text.format.DateUtils;

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


    static final String TWITTERFORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

   // SharedPreferences shared = this.getSharedPreferences()

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
        tweet.id = jsonObject.getInt("id");
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
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        //String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTERFORMAT, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
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
