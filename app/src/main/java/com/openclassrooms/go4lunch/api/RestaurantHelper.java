package com.openclassrooms.go4lunch.api;

import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RestaurantHelper {
    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static void createRestaurant(String id) {
        // 1 - Create Obj
        Restaurant restaurantToCreate = new Restaurant(id);

        RestaurantHelper.getRestaurantsCollection().document(id).set(restaurantToCreate);
    }

    // --- DELETE ---

    public static void deleteRestaurant(String id) {
        RestaurantHelper.getRestaurantsCollection().document(id).delete();
    }

    // --- LIKE FUNCTION ---

    public static void likeRestaurant(String uid, String id){
        User userToCreate = new User(uid);
        RestaurantHelper.getRestaurantsCollection().document(id).collection("likes").document(uid).set(userToCreate);
    }

    public static void dislikeRestaurant(String uid, String id){
        RestaurantHelper.getRestaurantsCollection().document(id).collection("likes").document(uid).delete();
    }

    // --- CHOOSE FUNCTION ---

    public static void chooseRestaurant(String uid, String id){
        User userToCreate = new User(uid, Objects.requireNonNull(getCurrentUser()).getDisplayName());
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").document(uid).set(userToCreate);
    }

    public static void unchooseRestaurant(String uid, String id){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").document(uid).delete();
    }

    @Nullable
    private static FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }
}
