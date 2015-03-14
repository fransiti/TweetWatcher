package com.example.morph.twipoto;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
        tweets = result.getTweets();
        displayMarkersOnMap();
    }

    //called to run every 10 seconds
    private class requestForTweets extends TimerTask {
        @Override
        public void run() {
            TwitterRequestController.getInstance().performQueryRequest();
        }
    }

}
