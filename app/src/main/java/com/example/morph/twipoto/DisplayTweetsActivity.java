package com.example.morph.twipoto;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.twitter.sdk.android.core.TwitterException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;


public class DisplayTweetsActivity extends ListActivity implements GetResultForPublicTweets{
    private Query query;
    private double radius = 500; //query for tweets within 1 km radius
    //layout objects
    private ListView tweetListView;
    private List<Status> tweets;
    private Timer timer;
    private LinearLayout mapBarLayout;
    private int tweetCount = 100; //size of tweets to be shown
    private Date mostRecentTweetDate;
    TweetListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweets_display_activity);
        initializeLayoutObject();
        setClickListeners();
        setCallbackContext();
        LocationController.getInstance().getLocation();
        LocationController.getInstance().setRadius(radius);
        tweets = new ArrayList<>();
        timer = new Timer();
        timer.schedule(new requestForTweets(),0,10000);   // request for tweets after each 10 seconds
    }

    //initializes layout objects
    public void initializeLayoutObject(){
        tweetListView = (ListView) findViewById(android.R.id.list);
        mapBarLayout = (LinearLayout) findViewById(R.id.toMaps);
    }

    public void setClickListeners(){
        mapBarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayTweetsActivity.this,GoogleMapActivity.class);
                startActivity(intent);
            }
        });
    }

    public void setCallbackContext(){
        TwitterRequestController.getInstance().resultForPublicTweets = this;
        LocationController.getInstance().mContext = this;
    }

    @Override
    public void onTweetResponse(QueryResult result) {

        try{
            if (tweets.size()<tweetCount) {
                firstUpdate(result.getTweets());    //called to update the list to the size mentioned
            }else {
                updateTweetList(result.getTweets());    //updates the list with the same buffer size
            }
           
            adapter = new TweetListAdapter(DisplayTweetsActivity.this);
            tweetListView.setAdapter(adapter);

        } catch (TwitterException te) {
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }
    }

    //called when the tweets list is less than tweet buffer size mentioned
    public void firstUpdate(List<Status> tweetList){
        mostRecentTweetDate = tweetList.get(0).getCreatedAt();  //assigning the most recent status
        int i = 0;

        while (tweets.size() < tweetCount && tweets.size() < tweetList.size() ) {
            tweets.add(tweetList.get(i));   //add to the list if the list size is less than size mentioned and
            i++;                            //check if the responded tweet list has fewer tweets than the buffer size
        }

    }

    //updates the list to have same buffer size of tweets mentioned
    public void updateTweetList(List<Status> tweetList){
        for (int i=0;i<tweetList.size();i++){
            if ((mostRecentTweetDate.compareTo(tweetList.get(i).getCreatedAt()))<0){    //checks and adds the tweet to the list if its a recent one
                tweets.remove(tweets.size()-1);    //remove the last item from the list
                tweets.add(0,tweetList.get(i));     //add a new item to the list at first position
            }
        }
        mostRecentTweetDate = tweetList.get(0).getCreatedAt();   //update the most recent tweet Date
    }

    //declaring an array adapter to manage tweet lists
    private class TweetListAdapter extends ArrayAdapter{
        private Context mContext;

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
            notifyDataSetChanged();
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

    private class requestForTweets extends TimerTask{
        @Override
        public void run() {
            TwitterRequestController.getInstance().performQueryRequest();
        }
    }
}