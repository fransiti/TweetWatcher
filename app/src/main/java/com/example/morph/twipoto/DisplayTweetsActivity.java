package com.example.morph.twipoto;


import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.twitter.sdk.android.core.TwitterException;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;


public class DisplayTweetsActivity extends ListActivity implements GetResultForPublicTweets{

    private Query query;
    private double radius = 5;  //query for tweets within 1 km radius

    //layout objects
    private ListView tweetListView;
    private List<Status> tweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweets_display_activity);
        initializeLayoutObject();
        setCallbackContext();
        LocationController.getInstance().getLocation();
        LocationController.getInstance().setRadius(radius);
        TwitterRequestController.getInstance().performQueryRequest();
    }

    //initializes layout objects
    public void initializeLayoutObject(){
        tweetListView = (ListView) findViewById(android.R.id.list);
    }


    public void setCallbackContext(){
        TwitterRequestController.getInstance().resultForPublicTweets = this;
        LocationController.getInstance().mContext = this;
    }

    @Override
    public void onTweetResponse(QueryResult result) {
        try{
                tweets = result.getTweets();
                System.out.print("SIZE "+tweets.size());
                TweetListAdapter adapter = new TweetListAdapter(DisplayTweetsActivity.this);
                tweetListView.setAdapter(adapter);
        } catch (TwitterException te) {
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }
    }

    //declaring an array adapter to manage tweet lists
    private class TweetListAdapter extends ArrayAdapter{

        private Context mContext;

        private TextView userNameTextView,userStatusTextView;

        // view holder class object holds reference to a particular
        // rowView which avoids delay caused by findViewById calls
        // helps in smooth scrolling
        public class ViewHolder {
            public TextView userNameText;
            public TextView userStatusText;
        }

        public TweetListAdapter(Context context){
            super(context,R.layout.tweet_details_layout,tweets);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            //if convertView is null which means the row was not inflated yet
            if (rowView == null){
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.tweet_details_layout, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.userNameText = (TextView) rowView.findViewById(R.id.userName);
                viewHolder.userStatusText = (TextView) rowView.findViewById(R.id.status);
                rowView.setTag(viewHolder);
            }

            //Getting references of pre inflated rows
            ViewHolder viewHolder = (ViewHolder) rowView.getTag();
            initializeListLayoutParameters(viewHolder,position);
            return rowView;
        }

        //update the list ui components
        public void initializeListLayoutParameters(ViewHolder holder,int position){
            holder.userNameText.setText(tweets.get(position).getUser().getScreenName());
            holder.userStatusText.setText(tweets.get(position).getText());
        }
    }
}
