package com.example.morph.twipoto;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Morph on 3/12/2015.
 * Singleton Location controller with helper method to provide the current location of the user
 * and return the location parameters such as longitude and latitude anywhere in the application
 */

public class LocationController extends Application implements LocationListener{

    public static LocationController locationController;
    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10 metters

    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private double curLatitude,curLongitude,radiusArea;
    public Context mContext;

    //flag for GPS Status
    private boolean isGPSEnabled = false;

    //flag for network status
    private boolean isNetworkEnabled = false;

    private boolean canGetLocation = false;

    //Declaring a Location Manager
    protected LocationManager locationManager;

    Location location;

    // Avoids Instantiation
    private LocationController(){}

    //Location controller single instance getter
    public static LocationController getInstance(){
        if (locationController == null){
            locationController = new LocationController();
        }
        return locationController;
    }

    public Location getLocation()
    {
        try
        {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            //getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled)
            {
                // no network provider is enabled
            }
            else
            {
                this.canGetLocation = true;

                //First get location from Network Provider
                if (isNetworkEnabled)
                {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Network", "Network");

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        updateGPSCoordinates();
                    }
                }

                //if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");

                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            updateGPSCoordinates();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            Log.e("Error : Location", "Impossible to connect to LocationManager", e);
        }

        return location;
    }

    public void updateGPSCoordinates()
    {
        if (location != null)
        {
            curLatitude = location.getLatitude();
            curLongitude = location.getLongitude();
        }
    }


    //current latitude getter
    public Double getCurrentLatitude(){
        if (location != null)
        {
            curLatitude = location.getLatitude();
        }

        return curLatitude;
    }


    //current longitude getter
    public Double getCurrentLongitude(){
        if (location != null)
        {
            curLongitude = location.getLongitude();
        }

        return curLongitude;
    }

    //radius area setter
    public void setRadius(Double radius){
        radiusArea = radius;
    }

    //radius area getter
    public Double getRadius(){
        return radiusArea;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
