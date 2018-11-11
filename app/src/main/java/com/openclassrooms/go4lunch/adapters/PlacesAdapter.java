package com.openclassrooms.go4lunch.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.views.RestaurantViewHolder;

import java.util.List;

/**
 * Adapter used to manage Place objects for the RestaurantListFragment's RecyclerView
 */
public class PlacesAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<Restaurant> mPlaces;
    private final RequestManager glide;

    public interface Listener { }

    public PlacesAdapter(final List<Restaurant> places, RequestManager glide){
        this.mPlaces = places;
        this.glide = glide;
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
}
