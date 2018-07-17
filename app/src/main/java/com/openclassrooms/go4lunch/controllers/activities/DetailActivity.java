package com.openclassrooms.go4lunch.controllers.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.openclassrooms.go4lunch.R;

public class DetailActivity extends AppCompatActivity {

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

        getPhotos(intent.getStringExtra("place_id"));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
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
}
