package com.openclassrooms.go4lunch.controllers.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controllers.activities.DetailActivity;
import com.openclassrooms.go4lunch.controllers.activities.MainActivity;
import com.openclassrooms.go4lunch.helpers.RestaurantHelper;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.utils.GetNearbyPlacesData;
import com.openclassrooms.go4lunch.utils.Toolbox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Fragment containing the map view
 */
@SuppressWarnings("FieldCanBeLocal")
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, EasyPermissions.PermissionCallbacks {

    private static GoogleMap mGoogleMap;
    private MapView mapView;
    private View mView;
    private FusedLocationProviderClient mFusedLocationClient;

    private Place selected_place;

    private final int RC_LOCATION = 1234;

    private GoogleApiClient mGoogleApiClient;

    private final int PROXIMITY_RADIUS = 10000;
    private double latitude;
    private double longitude;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        mGoogleApiClient = MainActivity.mGoogleApiClient;
    }

    //@AfterPermissionGranted(RC_LOCATION)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
        if (EasyPermissions.hasPermissions(Objects.requireNonNull(getActivity()), perms)) {
            mapView = mView.findViewById(R.id.mapView);
            if (mapView != null) {
                mapView.onCreate(null);
                mapView.onResume();
                mapView.getMapAsync(this);
            }
        }
        else {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_string), RC_LOCATION, perms);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(Objects.requireNonNull(getContext()));

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Objects.requireNonNull(getActivity()), R.raw.map_style));

        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        mGoogleMap.setOnMarkerClickListener(marker -> {

            String id = marker.getSnippet();

            Places.GeoDataApi.getPlaceById(mGoogleApiClient, id)
                    .setResultCallback(places -> {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            selected_place = places.get(0);
                            Log.i("PlacesTest", "Place found: " + selected_place.getName());
                            Intent intent = new Intent(getActivity(), DetailActivity.class);
                            Bundle bundle = new Bundle();
                            if (selected_place.getId() != null ) bundle.putString("place_id", selected_place.getId());
                            if (selected_place.getWebsiteUri() != null )bundle.putString("place_website", selected_place.getWebsiteUri().toString());
                            if (selected_place.getName() != null )bundle.putString("place_name", selected_place.getName().toString());
                            if (selected_place.getPhoneNumber() != null )bundle.putString("place_phone", selected_place.getPhoneNumber().toString());
                            if (selected_place.getAddress() != null )bundle.putString("place_address", selected_place.getAddress().toString());
                            if (selected_place.getPlaceTypes() != null )bundle.putString("place_type", selected_place.getPlaceTypes().toString());
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else {
                            Log.e("PlacesTest", "Place not found");
                        }
                        places.release();
                    });
            return true;
        });
        locateUser();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @SuppressLint("MissingPermission")
    private void locateUser() {
        if (EasyPermissions.hasPermissions(Objects.requireNonNull(getContext()), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET)) {

            MapsInitializer.initialize(Objects.requireNonNull(getContext()));

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(Objects.requireNonNull(getActivity()), location -> {
                        // Get last known location. In some rare situations this can be null.
                        if (location != null) {
                            mFusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(getActivity(), location2 -> {
                                        // Get last known location. In some rare situations this can be null.
                                        if (location2 != null) {

                                            CameraPosition user = CameraPosition.builder().target(new LatLng(location2.getLatitude(), location2.getLongitude())).zoom(16).bearing(0).build();
                                            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(user));

                                            latitude = location2.getLatitude();
                                            longitude = location2.getLongitude();

                                            Integer tabRequest = 1;

                                            // Creating and executing NearbySearch request

                                            // -------------- RESTAURANT ------------------- //

                                            String restaurant = "restaurant";

                                            String url = getUrl(latitude, longitude, restaurant);

                                            Object dataTransfer[] = new Object[4];

                                            dataTransfer[0] = url;
                                            dataTransfer[1] = tabRequest;
                                            dataTransfer[2] = latitude;
                                            dataTransfer[3] = longitude;

                                            GetNearbyPlacesData getNearbyPlacesData1 = new GetNearbyPlacesData();

                                            getNearbyPlacesData1.execute(dataTransfer);

                                            // -------------- MEAL TAKEAWAY ------------------- //

                                            String meal_takeaway = "meal_takeaway";

                                            url = getUrl(latitude, longitude, meal_takeaway);

                                            Object dataTransfer2[] = new Object[4];

                                            dataTransfer2[0] = url;
                                            dataTransfer2[1] = tabRequest;
                                            dataTransfer2[2] = latitude;
                                            dataTransfer2[3] = longitude;

                                            GetNearbyPlacesData getNearbyPlacesData2 = new GetNearbyPlacesData();

                                            getNearbyPlacesData2.execute(dataTransfer2);

                                            // -------------- MEAL DELIVERY ------------------- //

                                            String meal_delivery = "meal_delivery";

                                            url = getUrl(latitude, longitude, meal_delivery);

                                            Object dataTransfer3[] = new Object[4];

                                            tabRequest = 3;

                                            dataTransfer3[0] = url;
                                            dataTransfer3[1] = tabRequest;
                                            dataTransfer3[2] = latitude;
                                            dataTransfer3[3] = longitude;

                                            GetNearbyPlacesData getNearbyPlacesData3 = new GetNearbyPlacesData();

                                            getNearbyPlacesData3.execute(dataTransfer3);
                                        }
                                    });
                        }
                    });
        } else {
            // Request permissions
            EasyPermissions.requestPermissions(this, getString(R.string.rational_string),
                    RC_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET);
        }
    }

    /**
     * Will update all the markers of the GoogleMap according to a list of restaurants
     * @param restaurantList A list of restaurants
     */
    public  static void setMarkersIcon(List<Restaurant> restaurantList){
        MapFragment.mGoogleMap.clear();
        for (int i=0; i<restaurantList.size(); i++){
            MapFragment.setMarkerIcon(restaurantList.get(i));
        }
    }

    /**
     * Set the icon of a marker accordingly to FireStore database
     * @param restaurant A restaurant object
     */
    public static void setMarkerIcon(Restaurant restaurant){
        String mDate = Toolbox.getCurrentDate();

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(restaurant.getLatLng());
        markerOptions.title(restaurant.getName() + " : " + restaurant.getAddress());
        markerOptions.snippet(restaurant.getId());
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant_orange));

       mGoogleMap.addMarker(markerOptions);

        Task<QuerySnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(restaurant.getId()).collection("dates").document(mDate).collection("users").get();
        doc.addOnCompleteListener(task -> {
            if (Objects.requireNonNull(task.getResult()).size() > 0) {
                // Changes the color of a marker if a user has chosen this restaurant
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant_green));
                mGoogleMap.addMarker(markerOptions);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(GetNearbyPlacesData.restaurantListData != null) {
            List<Restaurant> restaurantList = GetNearbyPlacesData.restaurantListData;
            for (int i = 0; i < restaurantList.size(); i++) {
                setMarkerIcon(restaurantList.get(i));
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        mapView = mView.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms2) {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
        EasyPermissions.requestPermissions(this, getString(R.string.rational_string), 456, perms);
    }

    /**
     * Generate a NearbySearch request URL
     * @param latitude Latitude in a double object
     * @param longitude Longitude in a double object
     * @param nearbyPlace Type of place to search for
     * @return NearbySearch request URL
     */
    private String getUrl(double latitude, double longitude, String nearbyPlace)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=").append(nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=").append(getResources().getString(R.string.google_maps_key));

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
