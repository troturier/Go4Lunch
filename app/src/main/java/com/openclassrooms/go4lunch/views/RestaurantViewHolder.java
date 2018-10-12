package com.openclassrooms.go4lunch.views;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.helpers.RestaurantHelper;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.utils.GetPlacesData;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.Color.RED;
import static com.openclassrooms.go4lunch.controllers.fragments.RestaurantsListFragment.mGoogleApiClient;

public class RestaurantViewHolder extends RecyclerView.ViewHolder{

    // FOR DESIGN
    @BindView(R.id.res_list_address)
    TextView address;
    @BindView(R.id.res_list_distance)
    TextView distance;
    @BindView(R.id.res_list_name)
    TextView name;
    @BindView(R.id.res_list_opening)
    TextView opening;
    @BindView(R.id.res_list_workmates_tv)
    TextView workmatesTv;
    @BindView(R.id.res_list_workmates_ic)
    ImageView workmatesIc;
    @BindView(R.id.res_list_iv)
    ImageView resIv;
    @BindView(R.id.res_list_star_1)
    ImageView star1;
    @BindView(R.id.res_list_star_2)
    ImageView star2;
    @BindView(R.id.res_list_star_3)
    ImageView star3;

    public RestaurantViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @SuppressLint("DefaultLocale")
    public void updateWithResult(Restaurant place, RequestManager glideP){

        RestaurantHelper.checkNumberOfWorkmates(place.getId(), workmatesTv, workmatesIc);

        GetPlacesData.getPhotos(place.getId(), resIv, glideP);

        name.setText(place.getName());
        CharSequence addressString = place.getAddress();
        assert addressString != null;
        String[] splitStringArray = addressString.toString().split(",");
        address.setText(splitStringArray[0]);

        // distance.setText(String.format("%dm", place.getDistance()));

        if(place.getOpen()){
        opening.setText(R.string.open);
        }
        else {
            opening.setText(R.string.closed);
            opening.setTextColor(RED);
        }

        float rating = place.getRating();
        if (rating > 1) {
            star1.setVisibility(View.VISIBLE);
            if (rating > 2.5) {
                star2.setVisibility(View.VISIBLE);
                if (rating > 4) {
                    star3.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
