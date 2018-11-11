package com.openclassrooms.go4lunch.controllers.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.adapters.PlacesAdapter;
import com.openclassrooms.go4lunch.controllers.activities.DetailActivity;
import com.openclassrooms.go4lunch.controllers.activities.MainActivity;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.utils.GetNearbyPlacesData;
import com.openclassrooms.go4lunch.utils.ItemClickSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment containing the list view
 */
public class RestaurantsListFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, PlacesAdapter.Listener{

    // FOR DESIGN
    @BindView(R.id.fragment_restaurant_list_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_restaurant_list_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    private FragmentListener mCallback;

    public static GoogleApiClient mGoogleApiClient = MainActivity.mGoogleApiClient;

    public static PlacesAdapter mPlaceAdapter;

    private List<Restaurant> restaurantList = new ArrayList<>();

    private Boolean listLoaded;

    public List<Restaurant> placeList;

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
        mPlaceAdapter = new PlacesAdapter(placeList, Glide.with(this));
        mCallback.onCreationComplete();
    }

    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_restaurants_list_item)
                .setOnItemClickListener((recyclerView, position, v) -> Places.GeoDataApi.getPlaceById(mGoogleApiClient, mPlaceAdapter.getPlaceId(position))
                        .setResultCallback(places -> {
                            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                Intent intent = new Intent(getActivity(), DetailActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("place_id", places.get(0).getId());
                                if(places.get(0).getWebsiteUri() != null) bundle.putString("place_website", Objects.requireNonNull(Objects.requireNonNull(places.get(0).getWebsiteUri()).toString()));
                                bundle.putString("place_name", places.get(0).getName().toString());
                                bundle.putString("place_phone", Objects.requireNonNull(Objects.requireNonNull(places.get(0).getPhoneNumber()).toString()));
                                bundle.putString("place_address", Objects.requireNonNull(Objects.requireNonNull(places.get(0).getAddress()).toString()));

                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                            places.release();
                        }));
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

        LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity())).registerReceiver(MyBroadCastReceiver,
                new IntentFilter("com.action.test"));

        return mPlacesView;
    }

    /**
     * Show a progress indicator in the form of a Snackbar
     * @param message Message content
     */
    public void showProgressIndicator(final String message) {
        SwipeRefreshLayout sRefreshLayout = Objects.requireNonNull(getView()).findViewById(R.id.fragment_restaurant_list_swipe_container);
        Snackbar mSnackbar = Snackbar.make(sRefreshLayout, message, Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) mSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        ProgressBar item = new ProgressBar(getContext());
        contentLay.addView(item,0);
        mSnackbar.show();
    }

    /**
     * Display a Toast message
     * @param message Message content
     */
    public void showMessage(final String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("fragment", "Google Places API connection failed with error code: " + connectionResult.getErrorCode());
        Toast.makeText(getActivity(), "Google Places API connection failed with error code:" + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
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
    public void onResume() {
        super.onResume();
        if(GetNearbyPlacesData.restaurantListData != null && restaurantList != null) {
            restaurantList.clear();
            mPlaceAdapter.notifyDataSetChanged();
            restaurantList.addAll(GetNearbyPlacesData.restaurantListData);
            mPlaceAdapter.setPlaces(restaurantList);
            mPlaceAdapter.notifyDataSetChanged();
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

    /**
     * Used to update the list of restaurant in the RecyclerView accordingly to the result of the NearbySearch request
     */
    private final BroadcastReceiver MyBroadCastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            restaurantList.addAll(GetNearbyPlacesData.restaurantListData);
            mPlaceAdapter.setPlaces(restaurantList);
            mPlaceAdapter.notifyDataSetChanged();
        }
    };
}
