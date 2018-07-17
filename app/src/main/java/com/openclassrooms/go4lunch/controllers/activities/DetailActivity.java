package com.openclassrooms.go4lunch.controllers.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.RestaurantHelper;

public class DetailActivity extends AppCompatActivity {

    private String url;
    private String phone_number;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        TextView place_name = findViewById(R.id.detail_name);
        place_name.setText(intent.getStringExtra("place_name"));

        TextView place_address = findViewById(R.id.detail_address);
        place_address.setText(intent.getStringExtra("place_address"));

        id = intent.getStringExtra("place_id");

        getPhotos(id);

        url = intent.getStringExtra("place_website");
        phone_number = intent.getStringExtra("place_phone");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        checkIfDocumentExists(getCurrentUser().getUid(), id);
    }

    public void openWebsite(@SuppressWarnings("unused") View view){
        if(!url.isEmpty()) {
            Intent intent = new Intent(this, WebViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else{
            Snackbar.make(view, "No website found for this restaurant", Snackbar.LENGTH_LONG).show();
        }
    }

    public void openCallingApp(@SuppressWarnings("unused") View view){
        Intent callIntent  = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:"+phone_number));
        startActivity(callIntent);
    }

    public void likeRestaurant(View view){
        CheckBox cb_like = findViewById(R.id.detail_cb_like);

        if(cb_like.isChecked()){
            deleteRestaurantInFireStore();
            cb_like.setChecked(false);
        }
        else{
            createRestaurantInFireStore();
            cb_like.setChecked(true);
        }
    }

    private void getPhotos(String id) {
        GeoDataClient mGeoDataClient = Places.getGeoDataClient(this);

        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(id);
        photoMetadataResponse.addOnCompleteListener(task -> {
            // Get the list of photos.
            PlacePhotoMetadataResponse photos = task.getResult();
            // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
            // Get the first photo in the list.
            PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
            // Get the attribution text.
            CharSequence attribution = photoMetadata.getAttributions();
            // Get a full-size bitmap for the photo.
            Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
            photoResponse.addOnCompleteListener(task1 -> {
                PlacePhotoResponse photo = task1.getResult();
                ImageView imageView = findViewById(R.id.detail_photo);
                Bitmap bitmap = photo.getBitmap();
                imageView.setImageBitmap(bitmap);
            });
        });
    }

    // --------------------------------
    // REST
    // --------------------------------

    @Nullable
    private FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    private void createRestaurantInFireStore(){
        if (this.getCurrentUser() != null){
            RestaurantHelper.createRestaurant(getCurrentUser().getUid(), id);
        }
    }

    private void deleteRestaurantInFireStore(){
        if (this.getCurrentUser() != null){
            RestaurantHelper.deleteRestaurant(getCurrentUser().getUid(), id);
        }
    }


    public Boolean checkIfDocumentExists(String uid, String id){
        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection(uid).document(id).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                bool[0] = doc.getResult().exists();
                if(bool[0]){
                    CheckBox cb_like = findViewById(R.id.detail_cb_like);
                    cb_like.setChecked(true);
                }
            }
        });
        return bool[0];
    }

}
