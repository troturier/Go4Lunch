package com.openclassrooms.go4lunch.controllers.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controllers.activities.DetailActivity;
import com.openclassrooms.go4lunch.data.LocationService;
import com.openclassrooms.go4lunch.data.Place;
import com.openclassrooms.go4lunch.places.PlacesContract;
import com.openclassrooms.go4lunch.utils.GetPlacesData;
import com.openclassrooms.go4lunch.utils.ItemClickSupport;
import com.openclassrooms.go4lunch.views.PlacesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantsListFragment extends Fragment implements PlacesContract.View, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, PlacesAdapter.Listener{

    // FOR DESIGN
    @BindView(R.id.fragment_restaurant_list_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_restaurant_list_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    private FragmentListener mCallback;

    public static GoogleApiClient mGoogleApiClient;

    private PlacesContract.Presenter mPresenter = null;

    private PlacesAdapter mPlaceAdapter;

    private ProgressDialog mProgressDialog;

    private Boolean listLoaded;

    private List<Place> placeList;

    public RestaurantsListFragment() {
        // Required empty public constructor
    }

    public static  RestaurantsListFragment newInstance(){
        return new RestaurantsListFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        listLoaded = false;

        placeList = new ArrayList<>();

        @SuppressWarnings("unused") GeoDataClient mGeoDataClient = Places.getGeoDataClient(Objects.requireNonNull(getContext()));
        mPlaceAdapter = new PlacesAdapter(getContext(), placeList, Glide.with(this), this);
        mCallback.onCreationComplete();
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_restaurants_list_item)
                .setOnItemClickListener((recyclerView, position, v) -> {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("place_id", mPlaceAdapter.getPlaceId(position));
                    bundle.putString("place_website", Objects.requireNonNull(mPlaceAdapter.getPlace(position).getURL()));
                    bundle.putString("place_name", mPlaceAdapter.getPlace(position).getName());
                    bundle.putString("place_phone", Objects.requireNonNull(mPlaceAdapter.getPlace(position).getPhone()));
                    bundle.putString("place_address", Objects.requireNonNull(mPlaceAdapter.getPlace(position).getAddress()));
                    bundle.putString("place_type", Objects.requireNonNull(mPlaceAdapter.getPlace(position).getType()));

                    intent.putExtras(bundle);
                    startActivity(intent);

                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mPlacesView = inflater.inflate(R.layout.fragment_restaurants_list, container, false);

        ButterKnife.bind(this, mPlacesView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(false);

        recyclerView.setAdapter(mPlaceAdapter);

        this.configureOnClickRecyclerView();

        return mPlacesView;
    }

    @Override public final void showNearbyPlaces(final List<Place> places) {
        if (places.isEmpty()) {
            showMessage("No places found");
        } else {
            List<Place> newPlacesList = new ArrayList<>();
            List<String> listId = new ArrayList<>();
            List<Boolean> listOpenNow = new ArrayList<>();

            for (int i = 0; i < places.size(); i++) {

                String url = getUrlId(places.get(i));
                final String[] id = {null};
                Object dataTransfer[] = new Object[2];

                dataTransfer[0] = id[0];
                dataTransfer[1] = url;

                int finalI = i;
                new GetPlacesData((output, openNow) -> {
                    Log.d("PLACE_ID", "ID = " + output);
                    if (output != null && !Objects.requireNonNull(places.get(finalI).getType()).contains("Bakery") && !Objects.requireNonNull(places.get(finalI).getType()).contains("Winery")) {
                        newPlacesList.add(places.get(finalI));
                        listId.add(output);
                        listOpenNow.add(openNow);
                    }
                    if (finalI == places.size() - 1) {
                        placeList = newPlacesList;
                        mPlaceAdapter.setPlaces(placeList, listId, listOpenNow);
                        mPlaceAdapter.notifyDataSetChanged();
                        mProgressDialog.dismiss();
                    }
                }).execute(dataTransfer);
            }
        }
    }


    @Override public void showProgressIndicator(final String message) {
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(getActivity());
        }
        mProgressDialog.dismiss();
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }


    @Override
    public void showMessage(final String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("fragment", "Google Places API connection failed with error code: " + connectionResult.getErrorCode());
        Toast.makeText(getActivity(), "Google Places API connection failed with error code:" + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    @Override public final void setPresenter(final PlacesContract.Presenter presenter) {
        mPresenter = presenter;
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(Objects.requireNonNull(getActivity()))
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }
    }

    @Override public final void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override public final void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!listLoaded) {
            @SuppressLint("MissingPermission") Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                Log.i("Places", "Latitude/longitude from Google Location Services " + mLastLocation.getLatitude() + "/" + mLastLocation.getLongitude());
                mPresenter.setLocation(mLastLocation);
                final LocationService locationService = LocationService.getInstance();
                locationService.setCurrentLocation(mLastLocation);
                mPresenter.start();
                listLoaded = true;
            }
        }
    }

    @Override
    public void onAttach(final Context activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (FragmentListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListener");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Places", "Location connection lost, trying to re-connect");
        mGoogleApiClient.connect();
    }

    /**
     * Signals to the activity that this fragment has
     * completed creation activities.
     */
    public interface FragmentListener{
        void onCreationComplete();
    }

    private String getUrlId(Place place)
    {

        String location = Objects.requireNonNull(place.getLocation()).toString();

        Log.d("ResFragment", place.getLocation().toString());

        String[] splitStringArray = location.split("-");
        String[] splitStringArray2 = splitStringArray[1].split(",");

        String lng;
        String lat;

        if(splitStringArray[1].contains(".")){
            lng = "-" + splitStringArray2[0];

            lat = splitStringArray2[1];
            lat = lat.trim();
        }
        else {
            lng = "-" + splitStringArray2[0] + "." + splitStringArray2[1];

            lat = splitStringArray2[2] + "." + splitStringArray2[3];
            lat = lat.trim();
        }

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=").append(lat).append(",").append(lng);
        googlePlaceUrl.append("&rankby=distance");
        String name = place.getName().replace(" ", "+");
        googlePlaceUrl.append("&name=").append(name);
        googlePlaceUrl.append("&key=").append("AIzaSyCzm8EvYTPjQukPostLLKxVzheqwDc1Q9o");

        Log.d("ResFragment", googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }
}
