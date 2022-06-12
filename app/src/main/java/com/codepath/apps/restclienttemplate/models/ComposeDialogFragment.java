package com.codepath.apps.restclienttemplate.models;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.ComposeActivity;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TimelineActivity;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class ComposeDialogFragment extends DialogFragment {

    public static final String TAG = "ComposeDialogFragment";
    public static final int MAX_TWEET_LENGTH = 280;

    private Button btnTweet;
    private EditText etCompose;
    private TwitterClient client;
    private List<Tweet> tweets;
    private ProgressBar pbLoading;

    public interface ComposeDialogListener {
        void onFinishEditDialog(Tweet tweet);
    }

    public ComposeDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ComposeDialogFragment newInstance() {
        ComposeDialogFragment frag = new ComposeDialogFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_compose, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        client = TwitterApp.getRestClient(getContext());
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        //mEditText = (EditText) view.findViewById(R.id.txt_your_name);
        btnTweet = view.findViewById(R.id.btnTweet);
        etCompose = view.findViewById(R.id.etCompose);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);

        // set click listener on button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ProgressBar pb = (ProgressBar) view.findViewById(R.id.pbLoading);
                pbLoading.setVisibility(ProgressBar.VISIBLE);

                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(getContext(), "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getContext(), "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                // valid.
                Toast.makeText(getContext(), tweetContent, Toast.LENGTH_LONG).show();
                // make an api call to twitter to publish the tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        pbLoading.setVisibility(ProgressBar.INVISIBLE);
                        Log.i(TAG,"onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Publish tweet says: " + tweet.body);

                            // we have to update tweets now?? -> how tf do u do this
                            ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                            listener.onFinishEditDialog(tweet);
                            dismiss();
//                            Intent intent = new Intent();
//                            intent.putExtra("tweet", Parcels.wrap(tweet));
//                            setResult(RESULT_OK, intent); // set the result code and bundle data for response
//                            finish(); // closes the activity, pass data back to parent

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });
            }
        });
        // Fetch arguments from bundle and set title
        //String title = getArguments().getString("title", "Enter Name");
        //getDialog().setTitle(title);


        // Show soft keyboard automatically and request focus to field
        //mEditText.requestFocus();
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
