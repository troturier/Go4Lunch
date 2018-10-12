package com.openclassrooms.go4lunch.controllers.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.adapters.WorkmatesAdapter;
import com.openclassrooms.go4lunch.controllers.activities.DetailActivity;
import com.openclassrooms.go4lunch.helpers.UserHelper;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.utils.ItemClickSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.openclassrooms.go4lunch.controllers.activities.MainActivity.mGoogleApiClient;
import static java.lang.String.format;

/**
 * Class used for the Workmates tab fragment of the app
 */
public class WorkmatesFragment extends Fragment {

    private WorkmatesAdapter mWorkmatesAdapter;

    @BindView(R.id.workmates_list_recycler_view)
    RecyclerView recyclerView;

    public WorkmatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Preparing a list that will contain all users
        final List<User> mUsers = new ArrayList<>();

        // Configuration of the RecyclerView and binding with the request manager
        View mPlacesView = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this, mPlacesView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Path that will be used for requests to FireStore
        CollectionReference path = UserHelper.getUsersCollection();

        // First, we retrieve the list of all the users present on FIreStore
        Task<QuerySnapshot> doc = path.get();
        doc.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    User user = document.toObject(User.class);
                    String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    String newUid = user.getUid();
                    if(!newUid.equals(currentUid)) {
                        mUsers.add(user);
                    }
                }

                // Then we create two lists for both groups of users
                List<User> usersWithRestaurant = new ArrayList<>();
                List<User> usersWithoutRestaurant = new ArrayList<>();

                // We then classify the retrieved users in the two previously created lists
                for (User user : mUsers) {
                    // Retrieving the current date
                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String mDate = format.format(date);

                    Task<DocumentSnapshot> doc2 = UserHelper.getUsersCollection().document(Objects.requireNonNull(user.getUid())).collection("dates").document(mDate).get();
                    final Boolean[] bool = new Boolean[1];
                    doc2.addOnCompleteListener(task2 -> {
                        bool[0] = Objects.requireNonNull(doc2.getResult()).exists();
                        if (bool[0]) {
                            usersWithRestaurant.add(0, user);
                        } else {
                            usersWithoutRestaurant.add(user);
                        }

                        // Once the lists are complete we sort them alphabetically
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            usersWithRestaurant.sort((user1, t1) -> user1.getUsername().compareToIgnoreCase(t1.getUsername()));
                            usersWithoutRestaurant.sort((user12, t1) -> user12.getUsername().compareToIgnoreCase(t1.getUsername()));
                        }

                        // We recreate a single list containing all classified users
                        mUsers.clear();
                        mUsers.addAll(usersWithRestaurant);
                        mUsers.addAll(usersWithoutRestaurant);

                        // We then pass the list to the adapter to update the RecyclerView
                        mWorkmatesAdapter = new WorkmatesAdapter(mUsers, Glide.with(this));
                        recyclerView.setAdapter(mWorkmatesAdapter);
                        this.configureOnClickRecyclerView();
                    });
                }
            }
        });

        return mPlacesView;
    }

    /**
     * Configuring actions for selecting an item in the RecyclerView
     * The actions are:
     * either the opening of the detail page of a place (DetailActivity),
     * or the displaying of an information message (Toast message)
     */
    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_workmates_recycler_view_item)
                .setOnItemClickListener((recyclerView, position, v) -> {
                    // Retrieving the current date
                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String mDate = format.format(date);

                    Task<DocumentSnapshot> doc = UserHelper.getUsersCollection().document(Objects.requireNonNull(mWorkmatesAdapter.getUser(position).getUid())).collection("dates").document(mDate).get();
                    final Boolean[] bool = new Boolean[1];
                    doc.addOnCompleteListener(task -> {
                        bool[0] = Objects.requireNonNull(doc.getResult()).exists();
                        if (bool[0]) {
                            DocumentSnapshot document = task.getResult();
                            String resId = Objects.requireNonNull(document).getString("uid");
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, resId)
                                    .setResultCallback(places -> {
                                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                            Place selected_place = places.get(0);
                                            Intent intent = new Intent(getActivity(), DetailActivity.class);
                                            Bundle bundle = new Bundle();
                                            if (selected_place.getId() != null) bundle.putString("place_id", selected_place.getId());
                                            if (selected_place.getWebsiteUri() != null) bundle.putString("place_website", selected_place.getWebsiteUri().toString());
                                            if (selected_place.getName() != null) bundle.putString("place_name", selected_place.getName().toString());
                                            if (selected_place.getPhoneNumber() != null) bundle.putString("place_phone", selected_place.getPhoneNumber().toString());
                                            if (selected_place.getAddress() != null) bundle.putString("place_address", selected_place.getAddress().toString());
                                            if (selected_place.getPlaceTypes() != null) bundle.putString("place_type", selected_place.getPlaceTypes().toString());
                                            intent.putExtras(bundle);
                                            // Opening place details activity
                                            startActivity(intent);
                                        }
                                    });
                        }
                        else{
                            String[] username = mWorkmatesAdapter.getUser(position).getUsername().split(" ");
                            Toast.makeText(getApplicationContext(), format("No restaurant has been chosen by %s yet", username[0]), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }

}
