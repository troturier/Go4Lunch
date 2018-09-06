package com.openclassrooms.go4lunch.views;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.RestaurantHelper;
import com.openclassrooms.go4lunch.data.Place;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    public void updateWithResult(Place place, String id, Boolean openNow, RequestManager glideP){

        checkNumberOfWorkmates(id);

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, id)
                .setResultCallback(places -> {
                    if (places.getStatus().isSuccess() && places.getCount() > 0) {
                        getPhotos(places.get(0).getId(), resIv, glideP);

                        name.setText(place.getName());
                        String addressString = place.getAddress();
                        assert addressString != null;
                        String[] splitStringArray = addressString.split(",");
                        address.setText(String.format("%s - %s", place.getType(), splitStringArray[0]));
                        distance.setText(String.format("%dm", place.getDistance()));

                        if(openNow){
                            opening.setText(R.string.open);
                        }
                        else {
                            opening.setText(R.string.closed);
                            opening.setTextColor(RED);
                        }

                        float rating = places.get(0).getRating();
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
                    places.release();
                });


    }

    private static void getPhotos(String id, ImageView iv, RequestManager glideP) {
        new AsyncTask<Void,Void,Void>(){
            Bitmap image;
            Boolean exist = false;
            @Override
            protected Void doInBackground(Void... params) {

                PlacePhotoMetadataResult result = Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, id).await();
                if (result.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                    if (photoMetadataBuffer.getCount() > 0) {
                        PlacePhotoMetadata photo = photoMetadataBuffer.get(0);

                        image = photo.getScaledPhoto(mGoogleApiClient,400,400).await()
                                .getBitmap();

                        Log.d("Bitmap", String.valueOf(image));
                        exist = true;
                    }
                    photoMetadataBuffer.release();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(exist) {
                    glideP.load(image).into(iv);
                    //pIv.setImageBitmap(image);
                    super.onPostExecute(aVoid);
                }
                else{
                    iv.setImageResource(R.drawable.background_image_r);
                }
            }
        }.execute();
    }

    @SuppressLint("DefaultLocale")
    private void checkNumberOfWorkmates(String id){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        Task<QuerySnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").get();
        doc.addOnCompleteListener(task -> {
            if (task.getResult().size() > 0){
                workmatesTv.setText(String.format("(%d)", task.getResult().size()));
                workmatesTv.setVisibility(View.VISIBLE);
                workmatesIc.setVisibility(View.VISIBLE);
            }
        });
    }
}
