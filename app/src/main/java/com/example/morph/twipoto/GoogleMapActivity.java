package com.example.morph.twipoto;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import twitter4j.QueryResult;
import twitter4j.Status;

/**
 * Created by Morph on 3/14/2015.
 */
public class GoogleMapActivity extends  Activity implements GetResultForPublicTweets{

    private GoogleMap map;
    private List<Status> tweets;
    private Timer timer;
    private double userCurLatitude,userCurLongitude;
    private int tweetCount = 100; //size of tweets to be shown
    private Date mostRecentTweetDate;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        tweets = TwitterRequestController.getInstance().getQuery().getTweets();
        initializeComponents();
        setCallbackContext();
        timer = new Timer();
        timer.schedule(new requestForTweets(),0,10000);
    }

    //setting context to controllers
    public void setCallbackContext(){
        TwitterRequestController.getInstance().resultForPublicTweets = this;
        LocationController.getInstance().mContext = this;
    }

    //initializing ui components
    public void initializeComponents(){
        userCurLatitude = LocationController.getInstance().getCurrentLatitude();
        userCurLongitude = LocationController.getInstance().getCurrentLongitude();
        LatLng latLng = new LatLng(userCurLatitude,userCurLongitude);
        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
        displayMarkersOnMap();
    }

    //displays markers on the map with info window having tweet status information
    public void displayMarkersOnMap(){
        for (Status tweet : tweets ){
            double latitude,longitude;
            latitude = tweet.getGeoLocation().getLatitude();
            longitude = tweet.getGeoLocation().getLongitude();
            MarkerOptions marker =  new MarkerOptions()
                    .position(new LatLng(latitude,longitude)).title(tweet.getUser().getScreenName()).snippet(tweet.getText());

            map.addMarker(marker);

        }
    }

    @Override
    public void onTweetResponse(QueryResult result) {
        if (tweets.size()<tweetCount) {
            firstUpdate(result.getTweets());    //called to update the list to the size mentioned
        }else {
            updateTweetList(result.getTweets());    //updates the list with the same buffer size
        }
        displayMarkersOnMap();
    }


    //called when the tweets list is less than tweet buffer size mentioned
    public void firstUpdate(List<Status> tweetList){
        mostRecentTweetDate = tweetList.get(0).getCreatedAt();  //assigning the most recent status
        int i = 0;

        while (tweets.size() < tweetCount && tweets.size() < tweetList.size() ) {
            if (!(tweets.contains(tweetList.get(i)))) {     //add to the list if the list size is less than size mentioned and
                tweets.add(tweetList.get(i));               //check if the responded tweet list has fewer tweets than the buffer size
            }                                               //also checks if the tweet list has no duplicate values
            i++;
        }

    }

    //updates the list to have same buffer size of tweets mentioned
    public void updateTweetList(List<Status> tweetList){
        for (int i=0;i<tweetList.size();i++){
            if (((mostRecentTweetDate.compareTo(tweetList.get(i).getCreatedAt()))<0) && !(tweets.contains(tweetList.get(i)))){    //checks and adds the tweet to the list if its a recent one
                tweets.remove(tweets.size()-1);     //remove the last item from the list
                tweets.add(0,tweetList.get(i));     //add a new item to the list at first position
                //also checks if the tweet list has no duplicate values
            }
        }
        mostRecentTweetDate = tweetList.get(0).getCreatedAt();   //update the most recent tweet Date
    }


    //called to run every 10 seconds
    private class requestForTweets extends TimerTask {
        @Override
        public void run() {
            TwitterRequestController.getInstance().performQueryRequest();
        }
    }

}
