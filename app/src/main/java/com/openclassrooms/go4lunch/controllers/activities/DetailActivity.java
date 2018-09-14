package com.openclassrooms.go4lunch.controllers.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.RestaurantHelper;
import com.openclassrooms.go4lunch.api.UserHelper;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.adapters.UserAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.openclassrooms.go4lunch.controllers.fragments.RestaurantsListFragment.mGoogleApiClient;

public class DetailActivity extends AppCompatActivity {

    private String url;
    private String phone_number;
    private String id;
    private FloatingActionButton fab;
    private Boolean restaurantSelected;
    private UserAdapter mUserAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        // -------- USER LIST ---------

        final List<User> mUsers = new ArrayList<>();

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        id = intent.getStringExtra("place_id");

        recyclerView = findViewById(R.id.detail_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CollectionReference path = RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users");

        Task<QuerySnapshot> doc = path.get();
        doc.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    String currentUid = Objects.requireNonNull(getCurrentUser()).getUid();
                    String newUid = user.getUid();
                    if(!newUid.equals(currentUid)) {
                        mUsers.add(user);
                    }
                }

                mUserAdapter = new UserAdapter(getApplicationContext(), mUsers, Glide.with(this));
                recyclerView.setAdapter(mUserAdapter);
            }
        });



        // ----------------------------

        TextView place_name = findViewById(R.id.detail_name);
        place_name.setText(intent.getStringExtra("place_name"));

        TextView place_address = findViewById(R.id.detail_address);

        String address = intent.getStringExtra("place_address");

        String type = intent.getStringExtra("place_type");

        String[] splitStringArray = address.split(",");

        if(type.contains("[")){
            place_address.setText(splitStringArray[0]);
        }
        else{
            place_address.setText(getString(R.string.place_address_type, type, splitStringArray[0]));
        }

        ImageView imageView = findViewById(R.id.detail_photo);

        getPhotos(id, imageView);

        url = intent.getStringExtra("place_website");
        phone_number = intent.getStringExtra("place_phone");

        restaurantSelected = false;

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(v -> {
            if(!restaurantSelected){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    createRestaurantInFireStoreForChoose();
                }
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    deleteRestaurantInFireStoreForChoose();
                }
            }
        });

        checkIfDocumentExistsForLike(Objects.requireNonNull(getCurrentUser()).getUid(), id);
        checkIfDocumentExistsForChosen(getCurrentUser().getUid(), id);
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
            Snackbar.make(view, R.string.no_website, Snackbar.LENGTH_LONG).show();
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
            deleteRestaurantInFireStoreForLike();
            cb_like.setChecked(false);
        }
        else{
            createRestaurantInFireStoreForLike();
            cb_like.setChecked(true);
        }
    }

    private static void getPhotos(String id, ImageView iv) {
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
                        image = photo.getPhoto(mGoogleApiClient).await()
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
                if (exist) {
                    iv.setImageBitmap(image);
                    super.onPostExecute(aVoid);
                }
            }
        }.execute();
    }

    // --------------------------------
    // REST
    // --------------------------------

    @Nullable
    private FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    // --- LIKE FUNCTION ---

    private void createRestaurantInFireStoreForLike(){
        if (!checkIfRestaurantExists(id)){
            RestaurantHelper.createRestaurant(id);
            if(this.getCurrentUser() != null)
                RestaurantHelper.likeRestaurant(this.getCurrentUser().getUid(), id);
        }
        else {
            if(this.getCurrentUser() != null)
                RestaurantHelper.likeRestaurant(this.getCurrentUser().getUid(), id);
        }
    }

    private void deleteRestaurantInFireStoreForLike(){
        if (checkIfRestaurantExists(id)){
            RestaurantHelper.deleteRestaurant(id);
        }
        if(this.getCurrentUser() != null)
            RestaurantHelper.dislikeRestaurant(this.getCurrentUser().getUid(), id);
    }

    private void checkIfDocumentExistsForLike(String uid, String id){
        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("likes").document(uid).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(task -> {
            bool[0] = doc.getResult().exists();
            if(bool[0]){
                CheckBox cb_like = findViewById(R.id.detail_cb_like);
                cb_like.setChecked(true);
            }
        });
    }

    // --- CHOOSE FUNCTION ---

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createRestaurantInFireStoreForChoose(){
        if (!checkIfRestaurantExists(id)){
            RestaurantHelper.createRestaurant(id);
            if(this.getCurrentUser() != null) {
                checkIfUserDocumentExistsForChosen(this.getCurrentUser().getUid(), id);
                fab.setImageDrawable(getDrawable(R.drawable.ic_restaurant_selected));
                restaurantSelected = true;
            }
        }
        else {
            if(this.getCurrentUser() != null) {
                checkIfUserDocumentExistsForChosen(this.getCurrentUser().getUid(), id);
                fab.setImageDrawable(getDrawable(R.drawable.ic_restaurant_selected));
                restaurantSelected = true;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void deleteRestaurantInFireStoreForChoose(){
        if (checkIfRestaurantExists(id)){
            RestaurantHelper.deleteRestaurant(id);
            fab.setImageDrawable(getDrawable(R.drawable.ic_restaurant_not_selected));
            restaurantSelected = false;
        }
        if(this.getCurrentUser() != null) {
            RestaurantHelper.unchooseRestaurant(this.getCurrentUser().getUid(), id);
            UserHelper.unchooseRestaurant(this.getCurrentUser().getUid());
            fab.setImageDrawable(getDrawable(R.drawable.ic_restaurant_not_selected));
            restaurantSelected = false;
        }
    }


    private void checkIfDocumentExistsForChosen(String uid, String id){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").document(uid).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(task -> {
            bool[0] = doc.getResult().exists();
            if(bool[0]){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(getDrawable(R.drawable.ic_restaurant_selected));
                    restaurantSelected = true;
                }
            }
        });
    }

    private void checkIfUserDocumentExistsForChosen(String uid, String id){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        Task<DocumentSnapshot> doc = UserHelper.getUsersCollection().document(uid).collection("dates").document(mDate).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(task -> {
            bool[0] = doc.getResult().exists();
            if(bool[0]){
                DocumentSnapshot document = task.getResult();
                Restaurant restaurant = document.toObject(Restaurant.class);
                RestaurantHelper.unchooseRestaurant(uid, Objects.requireNonNull(restaurant).getUid());
                RestaurantHelper.chooseRestaurant(uid, id);
                UserHelper.chooseRestaurant(uid, id);
            }
            else{
                RestaurantHelper.chooseRestaurant(uid, id);
                UserHelper.chooseRestaurant(uid, id);
            }
        });
    }

    private Boolean checkIfRestaurantExists(String id){
        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).get();
        final Boolean[] bool2 = new Boolean[1];
        bool2[0] = false;
        doc.addOnCompleteListener(task -> bool2[0] = doc.getResult().exists());
        return bool2[0];
    }

}
