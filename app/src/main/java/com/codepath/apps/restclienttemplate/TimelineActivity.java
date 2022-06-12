package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.codepath.apps.restclienttemplate.models.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.models.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeDialogListener {

    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;
    private long minId = Long.MAX_VALUE;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this); // instantiating a twitter client

        // find the recycler view
        rvTweets = findViewById(R.id.rvTweets);

        // init the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // recycler view setup: layout manager and the adapter
            // sets the layout of the contents --> a list of repeating views in the recycler view.
            // recyclerView will not function without it.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                System.out.println("IN HERE");
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        // this is async
        setTweetAttributeAndPopulateTimeline();

        Button logoutButton = findViewById(R.id.button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // forget who's logged in
                TwitterApp.getRestClient(TimelineActivity.this).clearAccessToken();

                // navigate backwards to Login screen
                // we must fire an intent to switch activities
                Intent i = new Intent(TimelineActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // these flags make sure that the back button doesn't work
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish(); // finish kills the timeline activity so that we can't go back to it
            }
        });

        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { // this is messed up really bad
                fetchTimelineAsync(); // updates "tweets"
            }
        });

    }

    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance();
        composeDialogFragment.show(fm, "fragment_edit_name");
    }

    private void loadNextDataFromApi(int page) {
        // page = 1 at the beginning
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        //System.out.println("page = " + page);
        // Send an API request to retrieve appropriate paginated data
        setMinId();
        client.getMoreTweets(minId - 1, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    List<Tweet> olderTweets = Tweet.fromJsonArray(json.jsonArray);
                    tweets.addAll(olderTweets);
                    // `notifyItemRangeInserted()`: Notify any registered observers that the itemCount
                    // items starting at position positionStart have changed
                    adapter.notifyItemRangeInserted(25 * page, 25);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "getMoreTweets failed");
            }
        });
    }

    private void setMinId() {
        for (Tweet tweet : tweets) {
            if (tweet.id < minId) {
                minId = tweet.id;
            }
        }
    }

    public void fetchTimelineAsync() { // updating "tweets" into a refreshed list of tweets
        // get new tweets, set up tweets, then populate home timeline.
        client.getHomeTimeline(new JsonHttpResponseHandler() { // getting new tweets.
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                adapter.clear(); // clears all the tweets
                try {
                    tweets.addAll(Tweet.fromJsonArray(json.jsonArray));
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
                // got new tweets, now we must set it up & populate
                client.getLiked(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess liked tweets" + json.toString());
                        JSONArray jsonArray = json.jsonArray; // we have a list of liked tweet objects now
                        // go through "tweets" and toggle the like button for each one
                        //System.out.println("tweets: " + tweets);
                        for (Tweet tweet : tweets) {
                            //System.out.println("tweet id string: " + tweet.idString);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    //System.out.println("id string " + jsonArray.getJSONObject(i).getString("id_str"));
                                    if (Objects.equals(tweet.idString, jsonArray.getJSONObject(i).getString("id_str"))) {
                                        System.out.println("hello ");
                                        // set tweet attribute
                                        tweet.isLiked = true;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        adapter.notifyDataSetChanged(); // populating
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure!" + response, throwable);
                    }
                });
                swipeContainer.setRefreshing(false); // signals that refresh is done
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DEBUG", "Fetch timeline error: " + response);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true; // we must return true for the menu to be displayed
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose) {
            // compose icon has been selected
            Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();
            showEditDialog();
//            // navigate to the compose activity
//            Intent intent = new Intent(this, ComposeActivity.class);
//            startActivityForResult(intent, REQUEST_CODE); // deprecated?

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        // data is the data that results from going to the next activity
//        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
//            // Get data from the intent (tweet)
//            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
//            // Update the RV with the tweet
//            // modify data source of tweets
//            tweets.add(0, tweet);
//            // update the adapter
//            adapter.notifyItemInserted(0);
//            rvTweets.smoothScrollToPosition(0);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

//    private void populateHomeTimeline() { // this method is async
//        // twitter api method
//        client.getHomeTimeline(new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Headers headers, JSON json) {
//                Log.i(TAG, "onSuccess" + json.toString());
//                // json array of tweets
//                JSONArray jsonArray = json.jsonArray; // each tweet is a jsonObject
//                //System.out.println("json array: " + jsonArray);
//                try {
//                    tweets.addAll(Tweet.fromJsonArray(jsonArray)); // returns the full list of tweets from the api call
//                    setUpTweets();
//                    // this is what triggers the functions in the adapter?
//                    adapter.notifyDataSetChanged();
//                } catch (JSONException e) {
//                    Log.e(TAG, "Json exception", e);
//
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
//                Log.e(TAG, "onFailure!" + response, throwable);
//            }
//        });
//    }

    private void setTweetAttributeAndPopulateTimeline() {
        // .getLiked() function is async; handler happens afterwards.
        client.getLiked(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess liked tweets" + json.toString());
                JSONArray jsonArray = json.jsonArray; // we have a list of liked tweet objects now
                // go through "tweets" and toggle the like button for each one
                System.out.println("tweets: " + tweets);
                for (Tweet tweet : tweets) {
                    System.out.println("tweet id string: " + tweet.idString);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            //System.out.println("id string " + jsonArray.getJSONObject(i).getString("id_str"));
                            if (Objects.equals(tweet.idString, jsonArray.getJSONObject(i).getString("id_str"))) {
                                System.out.println("hello ");
                                // set tweet attribute
                                tweet.isLiked = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // home timeline is fetched and binded to app AFTER we set liked attribute.
                client.getHomeTimeline(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess" + json.toString());
                        // json array of tweets
                        JSONArray jsonArray = json.jsonArray; // each tweet is a jsonObject
                        //System.out.println("json array: " + jsonArray);
                        try {
                            tweets.addAll(Tweet.fromJsonArray(jsonArray)); // returns the full list of tweets from the api call
                            // this is what triggers the functions in the adapter?
                            adapter.notifyDataSetChanged(); // here, the tweets get liked or unliked
                        } catch (JSONException e) {
                            Log.e(TAG, "Json exception", e);

                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure!" + response, throwable);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure!" + response, throwable);
            }
        });
    }

    @Override
    public void onFinishEditDialog(Tweet tweet) {
        tweets.add(0, tweet);
        adapter.notifyItemInserted(0);
        rvTweets.smoothScrollToPosition(0);
    }
}