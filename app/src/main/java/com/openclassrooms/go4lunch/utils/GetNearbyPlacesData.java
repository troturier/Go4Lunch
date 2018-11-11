package com.openclassrooms.go4lunch.utils;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controllers.fragments.MapFragment;
import com.openclassrooms.go4lunch.models.Restaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class used to handle responses from requests to Google Places API
 **/
public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private double latitude;
    private double longitude;
    private Integer tabrequest;
    public static List<Restaurant> restaurantListData;
    @SuppressWarnings("FieldCanBeLocal")
    private String url;

    @Override
    protected String doInBackground(Object... objects){
        url = (String)objects[0];
        tabrequest = (Integer)objects[1];
        latitude = (double)objects[2];
        longitude = (double)objects[3];
        DownloadURL downloadURL = new DownloadURL();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s){

        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        Log.d("nearbyplacesdata","called parse method");
        showNearbyPlaces(nearbyPlaceList);
        // If tabrequest value equals 3, it means that the last request is complete and the restaurants list can be used by the fragments
        if(tabrequest == 3) {
            Set<Restaurant> hs = new HashSet<>(restaurantListData);
            restaurantListData.clear();
            restaurantListData.addAll(hs);

            Collections.sort(restaurantListData, (o1, o2) -> o1.getDistance().compareTo(o2.getDistance()));

            // Broadcast sent to the RestaurantListFragment to inform it of the restaurants list update
            Intent intent = new Intent("com.action.test");
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(GetAppContext.getContext());
            manager.sendBroadcast(intent);
        }
    }

    /**
     * Invoked on the UI thread after the background computation finishes
     * Will parse the result of a request into Restaurant objects and set their corresponding marker on the MapFragment's map
     * @param nearbyPlaceList The result of the background computation
     */
    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {
        if(restaurantListData == null) {
            restaurantListData = new ArrayList<>();
        }
        for (int i = 0; i < nearbyPlaceList.size(); i++) {
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);
            if(!googlePlace.isEmpty()){

                // ------- MARKER -----------
                MarkerOptions markerOptions = new MarkerOptions();
                String placeName = googlePlace.get("place_name");
                String vicinity = googlePlace.get("vicinity");
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                String id = googlePlace.get("id");
                LatLng latLng = new LatLng(lat, lng);

                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vicinity);
                markerOptions.snippet(id);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant_orange));

                // ------- RESTAURANT OBJECT CREATION -----------
                Restaurant restaurant = new Restaurant();
                restaurant.setAddress(googlePlace.get("vicinity"));
                restaurant.setId(googlePlace.get("id"));
                restaurant.setName(googlePlace.get("place_name"));
                restaurant.setOpen(Boolean.parseBoolean(googlePlace.get("opening")));
                if(!googlePlace.get("rating").isEmpty() && googlePlace.get("rating") != null) restaurant.setRating(Float.parseFloat(googlePlace.get("rating")));
                restaurant.setLatLng(latLng);

                // Calculating the distance between the user and the restaurant
                Location locationA = new Location("point A");

                locationA.setLatitude(latitude);
                locationA.setLongitude(longitude);

                Location locationB = new Location("point B");

                locationB.setLatitude(lat);
                locationB.setLongitude(lng);

                restaurant.setDistance(locationA.distanceTo(locationB));

                // Call the method to update the marker color based on the FireStore database
                MapFragment.setMarkerIcon(restaurant);

                restaurantListData.add(restaurant);
            }
        }
    }
}

