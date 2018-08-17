package com.openclassrooms.go4lunch.views;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.RestaurantHelper;
import com.openclassrooms.go4lunch.data.Place;

import java.lang.ref.WeakReference;
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

    static private RequestManager glide;

    RestaurantViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithResult(Place place, String id, Boolean openNow, RequestManager glide, PlacesAdapter.Listener callback){

        this.glide = glide;

        checkNumberOfWorkmates(id);

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, id)
                .setResultCallback(places -> {
                    if (places.getStatus().isSuccess() && places.getCount() > 0) {
                        getPhotos(places.get(0).getId(), resIv);

                        name.setText(place.getName());
                        String addressString = place.getAddress();
                        String[] splitStringArray = addressString.split(",");
                        address.setText(place.getType() + " - " + splitStringArray[0]);
                        distance.setText(place.getDistance() + "m");

                        if(openNow){
                            opening.setText("Currently open");
                        }
                        else {
                            opening.setText("Currently closed");
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



        WeakReference<PlacesAdapter.Listener> callbackWeakRef = new WeakReference<>(callback);
    }

    private static void getPhotos(String id, ImageView iv) {
        new AsyncTask<Void,Void,Void>(){
            final ImageView pIv = iv;
            Bitmap image;
            Boolean exist = false;
            @Override
            protected Void doInBackground(Void... params) {

                PlacePhotoMetadataResult result = Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, id).await();
                if (result.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                    if (photoMetadataBuffer.getCount() > 0) {
                        PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                        CharSequence attribution = photo.getAttributions();

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
                    glide.load(image).into(pIv);
                    //pIv.setImageBitmap(image);
                    super.onPostExecute(aVoid);
                }
                else{
                    pIv.setImageResource(R.drawable.background_image_r);
                }
            }
        }.execute();
    }

    public void checkNumberOfWorkmates(String id){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        Task<QuerySnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().size() > 0){
                    workmatesTv.setText("(" + task.getResult().size() + ")");
                    workmatesTv.setVisibility(View.VISIBLE);
                    workmatesIc.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
