package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    public String imageURL;
    public User user;
    public String tweetImageURL;

    // empty constructor needed by Parceler library
    public Tweet() {}

    // parsing each tweet represented by a jsonObject
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        if (jsonObject.has("full_text")) {
            tweet.body = jsonObject.getString("full_text");
        } else {
            tweet.body = jsonObject.getString("text");
        }
        tweet.createdAt = jsonObject.getString("created_at");
        // fromJson takes a JSONObect and converts to a User object.
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));

        // each tweet has "entities" section
        JSONObject entities = jsonObject.getJSONObject("entities");
        //System.out.println("entities: " + entities);
        if (entities.has("media")) {
            //System.out.println("media: " + entities.getJSONArray("media"));
            JSONArray media = entities.getJSONArray("media");
            JSONObject firstImage = (JSONObject) media.get(0);
            tweet.tweetImageURL = firstImage.getString("media_url_https");
        }

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
