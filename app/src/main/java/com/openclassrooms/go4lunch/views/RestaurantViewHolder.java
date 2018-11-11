package com.openclassrooms.go4lunch.views;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.helpers.RestaurantHelper;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.utils.GetPlaceData;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.Color.RED;

/**
 * Class used to create a new RecyclerView item of RestaurantsListFragment
 */
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
        // Get the number of colleagues who have chosen this restaurant
        RestaurantHelper.checkNumberOfWorkmates(place.getId(), workmatesTv, workmatesIc);

        // Get the photo of the restaurant
        GetPlaceData.getPhotos(place.getId(), resIv, glideP);

        name.setText(place.getName());
        CharSequence addressString = place.getAddress();
        assert addressString != null;
        String[] splitStringArray = addressString.toString().split(",");
        address.setText(splitStringArray[0]);

        String dist;
        Float distFloat = place.getDistance();
        // Convert the distance to the restaurant in kilometers if it exceeds 1000m
        if(place.getDistance() > 1000){
            distFloat = distFloat/1000;
            dist = String.format("%.1f", distFloat);
            distance.setText(String.format("%skm", dist));
        }
        else {
            int distInt = Math.round(distFloat);
            dist = Integer.toString(distInt);
            distance.setText(String.format("%sm", dist));
        }

        // Displays if the restaurant is open at the moment
        if(place.getOpen()){
            opening.setText(R.string.open);
        }
        else {
            opening.setText(R.string.closed);
            opening.setTextColor(RED);
        }

        // Shows a number of stars based on the restaurant's rating
        if(place.getRating() != null){
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
}
