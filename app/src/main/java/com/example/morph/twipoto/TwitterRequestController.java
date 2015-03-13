package com.example.morph.twipoto;

import android.app.Application;

import android.os.AsyncTask;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Morph on 3/12/2015.
 * Provides functionality for making requests for tweets based on geolocation
 * Used External Twitter Api support library twitter4j for making authenticated requests
 * to access public information from Twitter
 * Having a singleton Request Controller helps to make request anywhere in the application
 */


public class TwitterRequestController extends Application {

    private static final String TWITTER_KEY = "Ln7Z0iX3gipB35L8UtuuNlUSw";
    private static final String TWITTER_SECRET = "JO6WSmjXbvGRVgSCmrjMSMsSeSOvyqBtTmDx9cEuP4Zf5FBFtp";

    private Twitter twitter;
    private QueryResult result;
    private Query query;
    private GeoLocation location;
    private Query.Unit unit;
    private double latitude,longitude;
    private double radius;


    //single RequestController instance having global context
    private static TwitterRequestController requestController;

    //callback interface variable on tweet response
    public GetResultForPublicTweets resultForPublicTweets;

    //Declaring a private constructor to avoid multiple instance creation
    private TwitterRequestController(){}

    //returns requestController instance
    public static TwitterRequestController getInstance(){
        if(requestController == null){
            requestController = new TwitterRequestController();
        }
        return requestController;
    }

    //configure the twitter factory instance with keys and tokens provided by twitter
    public void configureTwitterFactoryInstance(){
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(TWITTER_KEY)
                .setOAuthConsumerSecret(TWITTER_SECRET)
                .setOAuthAccessToken("3071895709-9wjAMW9tiCCdhfAT22BuPyHK8vPJWq2MHE81m2I")
                .setOAuthAccessTokenSecret("J1Ho9zznzAbPsvWg0KgV74nCNPE253lzSdf12QzVrLsOU");
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        initializeGeolocationInstance();
    }

    //Instantiates a new query
    public void buildQueryForPublicTweets(){
        query = new Query();
        unit = Query.KILOMETERS; // or Query.MILES;
        query.setGeoCode(location, radius, unit);
    }

    public void initializeGeolocationInstance(){
        latitude = LocationController.getInstance().getCurrentLatitude();
        longitude = LocationController.getInstance().getCurrentLongitude();
        radius = LocationController.getInstance().getRadius();
        location = new GeoLocation(latitude, longitude);
    }


    public void performQueryRequest(){
        configureTwitterFactoryInstance();
        buildQueryForPublicTweets();
        new getTweets().execute();
    }

    private class getTweets extends AsyncTask<Void, Void, QueryResult> {

        @Override
        protected QueryResult doInBackground(Void... params) {
            try {

                result = twitter.search(query);
            }catch (twitter4j.TwitterException e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(QueryResult queryresult) {
            super.onPostExecute(queryresult);
            resultForPublicTweets.onTweetResponse(queryresult);
        }
    }

}
