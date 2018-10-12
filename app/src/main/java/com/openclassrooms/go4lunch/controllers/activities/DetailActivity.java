package com.openclassrooms.go4lunch.controllers.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.adapters.UserAdapter;
import com.openclassrooms.go4lunch.helpers.RestaurantHelper;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.utils.GetPlacesData;
import com.openclassrooms.go4lunch.utils.GetAppContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.openclassrooms.go4lunch.helpers.UserHelper.getCurrentUser;

/**
 * Class relating to the "Detail" activity of a place
 */
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

        // Creating a list which will be used to retrieve users who chose this location
        final List<User> mUsers = new ArrayList<>();

        // Retrieving the current date
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        // Retrieving the place id
        id = intent.getStringExtra("place_id");

        // Configuration of the RecyclerView that will be used for the user list
        recyclerView = findViewById(R.id.detail_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Path for the database to the chosen place
        CollectionReference path = RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users");

        // Retrieving the list of users who chose this restaurant and calling the corresponding adapter
        Task<QuerySnapshot> doc = path.get();
        doc.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    String currentUid = Objects.requireNonNull(getCurrentUser()).getUid();
                    String newUid = user.getUid();
                    // Filtering the list to not include the current user
                    if(!newUid.equals(currentUid)) {
                        mUsers.add(user);
                    }
                }

                mUserAdapter = new UserAdapter(getApplicationContext(), mUsers, Glide.with(this));
                recyclerView.setAdapter(mUserAdapter);
            }
        });

        // Set the place name in the corresponding TextView
        TextView place_name = findViewById(R.id.detail_name);
        place_name.setText(intent.getStringExtra("place_name"));

        // Retrieving place address TextView holder
        TextView place_address = findViewById(R.id.detail_address);

        // Retrieving place address from the intents
        String address = intent.getStringExtra("place_address");

        // Splitting the place address to only get the short version of it (Street and number)
        String[] splitStringArray = address.split(",");

        // Setting it in the corresponding TextView
        place_address.setText(splitStringArray[0]);

        // ImageView holder for the place picture
        ImageView imageView = findViewById(R.id.detail_photo);
        // Retrieving the place picture and loading it into the ImageView
        GetPlacesData.getPhotos(id, imageView, Glide.with(this));

        // Retrieving place website url from the intents
        url = intent.getStringExtra("place_website");

        // Retrieving place phone number from the intents
        phone_number = intent.getStringExtra("place_phone");

        // Setting a default value for the restaurantSelected boolean
        this.restaurantSelected = false;

        // Retrieving the floating action button
        fab = findViewById(R.id.fab);

        // Configuring the different actions of the button
        fab.setOnClickListener(v -> {
            if(!this.restaurantSelected){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.restaurantSelected = RestaurantHelper.createRestaurantInFireStoreForChoose(id);
                    fab.setImageDrawable(GetAppContext.getContext().getDrawable(R.drawable.ic_restaurant_selected));
                }
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.restaurantSelected = RestaurantHelper.deleteRestaurantInFireStoreForChoose(id);
                    fab.setImageDrawable(GetAppContext.getContext().getDrawable(R.drawable.ic_restaurant_not_selected));
                }
            }
        });

        // Updating the value of the Boolean variable restaurantSelected according to the database
        RestaurantHelper.checkIfDocumentExistsForLike(Objects.requireNonNull(getCurrentUser()).getUid(), id, findViewById(R.id.detail_cb_like));
        checkIfDocumentExistsForChosen(getCurrentUser().getUid(), id, fab);

    }

    /**
     * Open the place website in the WebViewActivity
     * @param view WebViewActivity
     */
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

    /**
     * Open the calling app of the device with the place phone number
     * @param view A calling app
     */
    public void openCallingApp(@SuppressWarnings("unused") View view){
        Intent callIntent  = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:"+phone_number));
        startActivity(callIntent);
    }

    /**
     * Function called when the "Like" button is pressed
     * Calls the corresponding helper functions to update place data in the database
     * @param view View
     */
    public void likeRestaurant(View view){
        CheckBox cb_like = findViewById(R.id.detail_cb_like);

        if(cb_like.isChecked()){
            RestaurantHelper.deleteRestaurantInFireStoreForLike(id);
            cb_like.setChecked(false);
        }
        else{
            RestaurantHelper.createRestaurantInFireStoreForLike(id);
            cb_like.setChecked(true);
        }
    }

    public void checkIfDocumentExistsForChosen(String uid, String id, FloatingActionButton fab){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);
        final Boolean[] resSelected = {false};

        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").document(uid).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(task -> {
            bool[0] = doc.getResult().exists();
            if(bool[0]){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fab.setImageDrawable(GetAppContext.getContext().getDrawable(R.drawable.ic_restaurant_selected));
                    restaurantSelected = resSelected[0] = true;
                }
            }
        });
    }
}
