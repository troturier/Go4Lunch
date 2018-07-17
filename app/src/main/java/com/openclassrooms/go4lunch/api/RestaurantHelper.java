package com.openclassrooms.go4lunch.api;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.models.Restaurant;

public class RestaurantHelper {
    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getRestaurantsCollection(String uid){
        return FirebaseFirestore.getInstance().collection("users").document(uid).collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createRestaurant(String uid, String id) {
        // 1 - Create Obj
        Restaurant restaurantToCreate = new Restaurant(id);

        return RestaurantHelper.getRestaurantsCollection(uid).document(id).set(restaurantToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getRestaurant(String uid, String id){
        return RestaurantHelper.getRestaurantsCollection(uid).document(id).get();
    }

    // --- CHECK ---



    // --- DELETE ---

    public static Task<Void> deleteRestaurant(String uid, String id) {
        return RestaurantHelper.getRestaurantsCollection(uid).document(id).delete();
    }
}
