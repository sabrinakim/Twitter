package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

public class DetailActivity extends AppCompatActivity {

    Tweet tweet;
    ImageView ivProfileDetail;
    ImageView ivMediaDetail;
    TextView tvUsernameDetail;
    TextView tvTweetDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_detail);

        // unwrap the tweet passed in via intent
        tweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        ivProfileDetail = findViewById(R.id.ivProfileDetail);
        ivMediaDetail = findViewById(R.id.ivMediaDetail);
        tvUsernameDetail = findViewById(R.id.tvUsernameDetail);
        tvTweetDetail = findViewById(R.id.tvTweetDetail);
        //ibLikeDetail = findViewById(R.id.ibLikeDetail);

        Glide.with(this).load(tweet.user.profileImageUrl).into(ivProfileDetail);
        Glide.with(this).load(tweet.getTweetImageURL()).into(ivMediaDetail);

        tvUsernameDetail.setText(tweet.getUser().name);
        tvTweetDetail.setText(tweet.body);

    }
}
