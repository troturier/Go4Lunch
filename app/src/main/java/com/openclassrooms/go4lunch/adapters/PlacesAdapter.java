package com.openclassrooms.go4lunch.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.location.places.Place;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.views.RestaurantViewHolder;

import java.util.Collections;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<Restaurant> mPlaces;
    private List<String> listId = Collections.emptyList();
    private List<Boolean> listOpenNow = Collections.emptyList();
    private final RequestManager glide;


    public interface Listener {

    }

    // FOR COMMUNICATION
    private final Listener callback;

    public PlacesAdapter(final List<Restaurant> places, RequestManager glide, Listener callback){
        this.mPlaces = places;
        this.glide = glide;
        this.callback = callback;
    }

    public final void setPlaces(final List<Restaurant> places){
        mPlaces = places;
    }

    @Override public final RestaurantViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View itemView = inflater.inflate(R.layout.fragment_restaurants_list_item, parent, false);
        return new RestaurantViewHolder(itemView);
    }


    @Override public final void onBindViewHolder(final RestaurantViewHolder holder, final int position) {
        final Restaurant place = mPlaces.get(position);
        holder.updateWithResult(place, this.glide);
    }

    public String getPlaceId(int position){
        return mPlaces.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override public final int getItemCount() {
        return mPlaces.size();
    }

    public Restaurant getPlace(int position){
        return this.mPlaces.get(position);
    }

}
