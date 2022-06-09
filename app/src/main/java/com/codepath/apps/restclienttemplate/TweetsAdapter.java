package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

// adapter fills layout with twitter data
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;

    // pass in the context and the list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // for each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ViewGroup is a collection of Views (TextView, ImageView, ...)
        // parent is the recylingView (recyclingView is a viewGroup)
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false); // inflate parent with R.layout.item_tweet
        // layout inflater instantiates a layout XML file into its corresponding View objects.
        // a layout is a ViewGroup.
        return new ViewHolder(view);
    }

    // bind values based on the position of the element (recyling step)
    // binding data into view holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get the data at position
        Tweet tweet = tweets.get(position);
        // bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void clear() {
        tweets.clear();
        notifyDataSetChanged();;
    }

    public void addAll(List<Tweet> newTweets) {
        tweets.addAll(newTweets);
        notifyDataSetChanged();
    }




    // define a viewholder class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        ImageView ivTweetImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvTimestamp;
        ToggleButton tbLike;

        @Override
        public void onClick(View view) {
            // get item position
            int position = getAdapterPosition();
            // makes sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at the position
                Tweet tweet = tweets.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, DetailActivity.class);
                // serialize the tweet using parceler
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                // show the activity
                context.startActivity(intent);
            }
        }

        // ViewHolder represents a tweet; one row in the recycling view
        // constructor
        public ViewHolder(@NonNull View itemView) { // itemView is a tweet
            // ViewGroup is a subclass of View
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfile);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            ivTweetImage = itemView.findViewById(R.id.ivTweetImage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tbLike = itemView.findViewById(R.id.tbLike);

            itemView.setOnClickListener(this);
        }


        // binding tweet to the tweet layout
        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
            if (tweet.tweetImageURL != null) {
                ivTweetImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(tweet.tweetImageURL).into(ivTweetImage);
            } else {
                ivTweetImage.setVisibility(View.GONE);
            }
            tvTimestamp.setText(tweet.relativeTimestamp);

            tbLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send api request
                    TwitterClient client = new TwitterClient(context);
                    client.likeTweet(tweet, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            System.out.println("tweet successfully liked");
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            System.out.println("tweet did not liked :(((");
                        }
                    });

                }
            });

        }
    }
}
