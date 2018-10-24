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

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private Integer tabrequest;
    public static List<Restaurant> restaurantListData;
    @SuppressWarnings("FieldCanBeLocal")
    private String url;

    @Override
    protected String doInBackground(Object... objects){
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        tabrequest = (Integer)objects[2];
        latitude = (double)objects[3];
        longitude = (double)objects[4];
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
        if(tabrequest == 3) {
            Set<Restaurant> hs = new HashSet<>(restaurantListData);
            restaurantListData.clear();
            restaurantListData.addAll(hs);

            Collections.sort(restaurantListData, (o1, o2) -> o1.getDistance().compareTo(o2.getDistance()));

            Intent intent = new Intent("com.action.test");
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(GetAppContext.getContext());
            manager.sendBroadcast(intent);
        }
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {
        if(restaurantListData == null) {
            restaurantListData = new ArrayList<>();
        }
        for (int i = 0; i < nearbyPlaceList.size(); i++) {
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);
            if(!googlePlace.isEmpty()){
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

                Marker marker = mMap.addMarker(markerOptions);

                if(!googlePlace.isEmpty()) {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setAddress(googlePlace.get("vicinity"));
                    restaurant.setId(googlePlace.get("id"));
                    restaurant.setName(googlePlace.get("place_name"));
                    restaurant.setOpen(Boolean.parseBoolean(googlePlace.get("opening")));
                    restaurant.setRating(Float.parseFloat(googlePlace.get("rating")));
                    restaurant.setMarker(marker);

                    Location locationA = new Location("point A");

                    locationA.setLatitude(latitude);
                    locationA.setLongitude(longitude);

                    Location locationB = new Location("point B");

                    locationB.setLatitude(lat);
                    locationB.setLongitude(lng);

                    restaurant.setDistance(locationA.distanceTo(locationB));

                    MapFragment.setMarkerIcon(restaurant);

                    restaurantListData.add(restaurant);

                }

            }
        }
    }
}

