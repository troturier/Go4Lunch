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
import com.openclassrooms.go4lunch.api.UserHelper;
import com.openclassrooms.go4lunch.controllers.activities.DetailActivity;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.utils.ItemClickSupport;
import com.openclassrooms.go4lunch.adapters.WorkmatesAdapter;

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


public class WorkmatesFragment extends Fragment {

    private WorkmatesAdapter mWorkmatesAdapter;

    @BindView(R.id.workmates_list_recycler_view)
    RecyclerView recyclerView;

    public WorkmatesFragment() {
        // Required empty public constructor
    }


// --Commented out by Inspection START (06/09/18 10:59):
//    public static WorkmatesFragment newInstance() {
//        WorkmatesFragment fragment = new WorkmatesFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }
// --Commented out by Inspection STOP (06/09/18 10:59)

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final List<User> mUsers = new ArrayList<>();

        View mPlacesView = inflater.inflate(R.layout.fragment_workmates, container, false);

        ButterKnife.bind(this, mPlacesView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        CollectionReference path = UserHelper.getUsersCollection();

        Task<QuerySnapshot> doc = path.get();
        doc.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    String newUid = user.getUid();
                    if(!newUid.equals(currentUid)) {
                        mUsers.add(user);
                    }
                }

                List<User> usersWithRestaurant = new ArrayList<>();
                List<User> usersWithoutRestaurant = new ArrayList<>();

                for (User user : mUsers) {
                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String mDate = format.format(date);
                    Task<DocumentSnapshot> doc2 = UserHelper.getUsersCollection().document(Objects.requireNonNull(user.getUid())).collection("dates").document(mDate).get();
                    final Boolean[] bool = new Boolean[1];
                    doc2.addOnCompleteListener(task2 -> {
                        bool[0] = doc2.getResult().exists();
                        if (bool[0]) {
                            usersWithRestaurant.add(0, user);
                        } else {
                            usersWithoutRestaurant.add(user);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            usersWithRestaurant.sort((user1, t1) -> user1.getUsername().compareToIgnoreCase(t1.getUsername()));
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            usersWithoutRestaurant.sort((user12, t1) -> user12.getUsername().compareToIgnoreCase(t1.getUsername()));
                        }

                        mUsers.clear();
                        mUsers.addAll(usersWithRestaurant);
                        mUsers.addAll(usersWithoutRestaurant);

                        mWorkmatesAdapter = new WorkmatesAdapter(mUsers, Glide.with(this));
                        recyclerView.setAdapter(mWorkmatesAdapter);
                        this.configureOnClickRecyclerView();
                    });
                }
            }
        });

        // Inflate the layout for this fragment
        return mPlacesView;
    }

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_workmates_recycler_view_item)
                .setOnItemClickListener((recyclerView, position, v) -> {
                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String mDate = format.format(date);
                    Task<DocumentSnapshot> doc = UserHelper.getUsersCollection().document(Objects.requireNonNull(mWorkmatesAdapter.getUser(position).getUid())).collection("dates").document(mDate).get();
                    final Boolean[] bool = new Boolean[1];
                    doc.addOnCompleteListener(task -> {
                        bool[0] = doc.getResult().exists();
                        if (bool[0]) {
                            DocumentSnapshot document = task.getResult();
                            String resId = document.getString("uid");
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient, resId)
                                    .setResultCallback(places -> {
                                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                            Place selected_place = places.get(0);
                                            Intent intent = new Intent(getActivity(), DetailActivity.class);
                                            Bundle bundle = new Bundle();
                                            if (selected_place.getId() != null)
                                                bundle.putString("place_id", selected_place.getId());
                                            if (selected_place.getWebsiteUri() != null)
                                                bundle.putString("place_website", selected_place.getWebsiteUri().toString());
                                            if (selected_place.getName() != null)
                                                bundle.putString("place_name", selected_place.getName().toString());
                                            if (selected_place.getPhoneNumber() != null)
                                                bundle.putString("place_phone", selected_place.getPhoneNumber().toString());
                                            if (selected_place.getAddress() != null)
                                                bundle.putString("place_address", selected_place.getAddress().toString());
                                            if (selected_place.getPlaceTypes() != null)
                                                bundle.putString("place_type", selected_place.getPlaceTypes().toString());
                                            intent.putExtras(bundle);
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
