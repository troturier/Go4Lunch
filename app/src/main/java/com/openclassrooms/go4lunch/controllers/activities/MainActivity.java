package com.openclassrooms.go4lunch.controllers.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.maps.android.SphericalUtil;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.adapters.ViewPagerAdapter;
import com.openclassrooms.go4lunch.controllers.fragments.MapFragment;
import com.openclassrooms.go4lunch.controllers.fragments.RestaurantsListFragment;
import com.openclassrooms.go4lunch.helpers.UserHelper;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.utils.GetAppContext;
import com.openclassrooms.go4lunch.utils.GetNearbyPlacesData;
import com.openclassrooms.go4lunch.utils.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.Color.WHITE;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, RestaurantsListFragment.FragmentListener {

    //FOR DESIGN
    @BindView(R.id.main_activity_linear_layout)
    LinearLayout linearLayout;

    private TextView drawerUsername;

    public static GoogleApiClient mGoogleApiClient;

    //FOR DATA
    private static final int RC_SIGN_IN = 123;
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private User currentUser;

    private DrawerLayout mDrawerLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private FusedLocationProviderClient mFusedLocationClient;

    private static final CharacterStyle STYLE_NORMAL = new StyleSpan(Typeface.NORMAL);

    private Place selected_place;

    // --Commented out by Inspection (10/07/2018 13:42):private Menu menu;

    // --------------------
    // LIFE CYCLE
    // --------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        ButterKnife.bind(this); //Configure Butterknife

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();

        //noinspection unused
        GeoDataClient mGeoDataClient = Places.getGeoDataClient(this);

        AuthUI.getInstance();

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {

                    int id = menuItem.getItemId();

                    switch (id) {
                        case R.id.nav_logout:
                            signOutUserFromFirebase();
                            break;
                        case R.id.nav_lunch:
                            Date date = Calendar.getInstance().getTime();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String mDate = format.format(date);
                            Task<DocumentSnapshot> doc = UserHelper.getUsersCollection().document(Objects.requireNonNull(getCurrentUser()).getUid()).collection("dates").document(mDate).get();
                            final Boolean[] bool = new Boolean[1];
                            doc.addOnCompleteListener(task -> {
                                bool[0] = Objects.requireNonNull(doc.getResult()).exists();
                                if(bool[0]){
                                    DocumentSnapshot document = task.getResult();
                                    String resId = Objects.requireNonNull(document).getString("id");
                                    Places.GeoDataApi.getPlaceById(mGoogleApiClient, resId)
                                            .setResultCallback(places -> {
                                                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                                    selected_place = places.get(0);
                                                    Intent intent = new Intent(this, DetailActivity.class);
                                                    Bundle bundle = new Bundle();
                                                    if (selected_place.getId() != null ) bundle.putString("place_id", selected_place.getId());
                                                    if (selected_place.getWebsiteUri() != null )bundle.putString("place_website", selected_place.getWebsiteUri().toString());
                                                    if (selected_place.getName() != null )bundle.putString("place_name", selected_place.getName().toString());
                                                    if (selected_place.getPhoneNumber() != null )bundle.putString("place_phone", selected_place.getPhoneNumber().toString());
                                                    if (selected_place.getAddress() != null )bundle.putString("place_address", selected_place.getAddress().toString());
                                                    if (selected_place.getPlaceTypes() != null )bundle.putString("place_type", selected_place.getPlaceTypes().toString());
                                                    intent.putExtras(bundle);
                                                    startActivity(intent);
                                                }
                                            });
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), R.string.no_rest_chosen, Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case R.id.nav_settings:
                            break;
                        default:
                            break;
                    }

                    mDrawerLayout.closeDrawers();
                    return true;
                });

        if (!isCurrentUserLogged()) {
            startSignInActivity();
        } else {
            UserHelper.getUser(Objects.requireNonNull(this.getCurrentUser()).getUid()).addOnSuccessListener(documentSnapshot -> currentUser = documentSnapshot.toObject(User.class));
            this.configureToolbar();
            this.updateUIWhenCreating();

            viewPager = findViewById(R.id.viewpager);
            setupViewPager(viewPager);

            tabLayout = findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
            setupTabsStyle(tabLayout, viewPager);
        }

        SearchView searchView = findViewById(R.id.searchView);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("FR")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();

        searchView.setOnCloseListener(() -> {
            RestaurantsListFragment.mPlaceAdapter.setPlaces(GetNearbyPlacesData.restaurantListData);
            RestaurantsListFragment.mPlaceAdapter.notifyDataSetChanged();
            MapFragment.setMarkersIcon(GetNearbyPlacesData.restaurantListData);
            return false;
        });


        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @SuppressLint("MissingPermission")
            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.isEmpty()){

                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(GetAppContext.getContext()));

                    mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                        LatLng latLng = new LatLng(Objects.requireNonNull(task.getResult()).getLatitude(), task.getResult().getLongitude());

                        mGeoDataClient.getAutocompletePredictions(newText, toBounds(latLng), typeFilter).addOnCompleteListener(task1 -> {
                            ArrayList<Restaurant> restaurantList = new ArrayList<>();
                            ArrayList<Restaurant> combinedList = new ArrayList<>();
                            for (int i = 0; i < Objects.requireNonNull(task1.getResult()).getCount(); i++) {
                                Restaurant restaurant = new Restaurant();
                                restaurant.setName(task1.getResult().get(i).getPrimaryText(STYLE_NORMAL).toString());
                                restaurant.setAddress(task1.getResult().get(i).getSecondaryText(STYLE_NORMAL).toString());
                                restaurant.setId(task1.getResult().get(i).getPlaceId());
                                restaurantList.add(restaurant);
                            }
                            for (int i = 0; i < GetNearbyPlacesData.restaurantListData.size(); i++) {
                                if(GetNearbyPlacesData.restaurantListData.get(i).getName().toLowerCase().contains(newText.toLowerCase())){
                                    restaurantList.add(GetNearbyPlacesData.restaurantListData.get(i));
                                }
                            }
                            combinedList.addAll(restaurantList);
                            combinedList.addAll(GetNearbyPlacesData.restaurantListData);
                            RestaurantsListFragment.mPlaceAdapter.setPlaces(findDuplicates(combinedList));
                            RestaurantsListFragment.mPlaceAdapter.notifyDataSetChanged();
                            MapFragment.setMarkersIcon(findDuplicates(combinedList));
                            Log.d("AutoComp", restaurantList.toString());
                        });
                    });

                    }
                else {
                    RestaurantsListFragment.mPlaceAdapter.setPlaces(GetNearbyPlacesData.restaurantListData);
                    RestaurantsListFragment.mPlaceAdapter.notifyDataSetChanged();
                    MapFragment.setMarkersIcon(GetNearbyPlacesData.restaurantListData);
                }
                return false;
            }
        });

        Notification.createNotification();

    }

    private LatLngBounds toBounds(LatLng center) {
        double distanceFromCenterToCorner = (double) 10000 * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    private static List<Restaurant> findDuplicates(List<Restaurant> listContainingDuplicates) {

        final Set<Restaurant> setToReturn = new HashSet<>();
        final Set<Restaurant> set1 = new HashSet<>();

        for (Restaurant restaurant : listContainingDuplicates) {
            if (!set1.add(restaurant)) {
                setToReturn.add(restaurant);
            }
        }
        List<Restaurant> restaurantList = new ArrayList<>(setToReturn);
        Collections.sort(restaurantList, (o1, o2) -> o1.getDistance().compareTo(o2.getDistance()));
        return restaurantList;
    }

    // --------------------
    // ACTIONS
    // --------------------

    /* @OnClick(R.id.profile_activity_button_delete)
    public void onClickDeleteButton() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) -> deleteUserFromFirebase())
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();
    } */

    @SuppressWarnings("SameReturnValue")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    @SuppressWarnings("unused")
    private void deleteUserFromFirebase(){
        if (this.getCurrentUser() != null) {

            //4 - We also delete user from firestore storage
            UserHelper.deleteUser(this.currentUser.getUid()).addOnFailureListener(this.onFailureListener());

            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }

    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());
        }
    }

    // --------------------
    // UI
    // --------------------

    private void showSnackBar(LinearLayout linearLayout, String message){
        Snackbar snackbar = Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void updateUIWhenCreating(){
        if (this.getCurrentUser() != null){

            NavigationView navigationView = findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);

            ImageView drawerImageviewProfile = headerView.findViewById(R.id.drawer_imageview_profile);
            TextView drawerEmail = headerView.findViewById(R.id.drawer_email);
            drawerUsername = headerView.findViewById(R.id.drawer_username);

            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(drawerImageviewProfile);
            }

            //Get email & username from Firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();

            //Update views with data
            drawerEmail.setText(email);

            // 5 - Get additional data from Firestore
            UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
                currentUser = documentSnapshot.toObject(User.class);
                String username = TextUtils.isEmpty(Objects.requireNonNull(currentUser).getUsername()) ? getString(R.string.info_no_username_found) : currentUser.getUsername();
                drawerUsername.setText(username);
            });
        }
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return aVoid -> {
            switch (origin){
                case RC_SIGN_IN:
                    UserHelper.getUser(Objects.requireNonNull(this.getCurrentUser()).getUid()).addOnSuccessListener(documentSnapshot -> currentUser = documentSnapshot.toObject(User.class));
                    this.configureToolbar();
                    this.updateUIWhenCreating();

                    viewPager = findViewById(R.id.viewpager);
                    setupViewPager(viewPager);

                    tabLayout = findViewById(R.id.tabs);
                    tabLayout.setupWithViewPager(viewPager);
                    setupTabsStyle(tabLayout, viewPager);
                    break;
                case SIGN_OUT_TASK:
                    startSignInActivity();
                    break;
                case DELETE_USER_TASK:
                    startSignInActivity();
                    break;
                default:
                    break;
            }
        };
    }

    private void configureToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(WHITE);

        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle(R.string.im_hungry);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    private OnFailureListener onFailureListener(){
        // return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
        return e -> Log.d("Failure", "Error", e);
    }

    // --------------------
    // ON RESULT HANDLER
    // --------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_CANCELED :
                startSignInActivity();
                Toast.makeText(getApplicationContext(), R.string.connect_first, Toast.LENGTH_LONG).show();
                break;
            case RESULT_OK:
                this.handleResponseAfterSignIn(requestCode, resultCode, data);
                UserHelper.getUser(Objects.requireNonNull(this.getCurrentUser()).getUid()).addOnSuccessListener(documentSnapshot -> currentUser = documentSnapshot.toObject(User.class));
                this.configureToolbar();
                this.updateUIWhenCreating();

                viewPager = findViewById(R.id.viewpager);
                setupViewPager(viewPager);
                tabLayout = findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);
                setupTabsStyle(tabLayout, viewPager);
                break;
            default:
                startSignInActivity();
                break;
        }
    }

    // --------------------
    // SIGN-IN ACTIVITY
    // --------------------

    private void startSignInActivity(){
        startActivityForResult(AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setTheme(R.style.LoginTheme)
                                .setAvailableProviders(
                                        Arrays.asList(
                                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                                new AuthUI.IdpConfig.FacebookBuilder().build(),
                                                new AuthUI.IdpConfig.EmailBuilder().build()))
                                .setIsSmartLockEnabled(false, true)
                                .setLogo(R.drawable.ic_logo)
                                .build(),
                        RC_SIGN_IN);
    }

    // --------------------
    // UTILS
    // --------------------

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore();
                showSnackBar(this.linearLayout, getString(R.string.connection_succeed));
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.linearLayout, getString(R.string.error_authentication_canceled));
                } else if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.linearLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.linearLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    @Nullable
    private FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    private Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }

    // --------------------
    // TABS
    // --------------------

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new com.openclassrooms.go4lunch.controllers.fragments.MapFragment(), "Map View");
        adapter.addFragment(new com.openclassrooms.go4lunch.controllers.fragments.RestaurantsListFragment(), "List View");
        adapter.addFragment(new com.openclassrooms.go4lunch.controllers.fragments.WorkmatesFragment(), "Workmates");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    private void setupTabsStyle(TabLayout tabLayout, ViewPager viewPager) {
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.ic_map);
        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(R.string.map_view_title);

        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.ic_view_list);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText(R.string.list_view_title);

        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.ic_workmates);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setText(R.string.workmates_title);

        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(tabLayout.getSelectedTabPosition())).getIcon()).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);


        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                }
                if (tab.getPosition() == 2){
                    ActionBar actionbar = getSupportActionBar();
                    Objects.requireNonNull(actionbar).setTitle(R.string.available_workmates);
                }
                else {
                    ActionBar actionbar = getSupportActionBar();
                    Objects.requireNonNull(actionbar).setTitle(R.string.im_hungry);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Connection", "Connection failed");
    }

    @Override
    public void onCreationComplete() {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.d("PointerCapture", "Pointer capture changed");
    }
}
