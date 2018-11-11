package com.openclassrooms.go4lunch.helpers;

import android.support.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserHelper {

    private static final String COLLECTION_NAME = "users";

    // --- COLLECTION REFERENCE ---

    /**
     * Retrieve the User CollectionReference
     * @return CollectionReference
     */
    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    /**
     * Create a user in FireStore
     * @param uid User id
     * @param username Username
     * @param urlPicture User's picture URL
     * @return Task
     */
    public static Task<Void> createUser(String uid, String username, String urlPicture) {
        User userToCreate = new User(uid, username, urlPicture);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---

    /**
     * Return data associated with a given user
     * @param uid User id
     * @return User data
     */
    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    /**
     * Return the current user connected in the application
     * @return User
     */
    @Nullable
    public static FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    // --- CHOOSE FUNCTION ---

    /**
     * Add a restaurant to a user "Choose" list in FireStore for the current date
     * @param uid User id
     * @param id Restaurant id
     */
    static void chooseRestaurant(String uid, String id){
        Restaurant restaurantToCreate = new Restaurant(id);
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);
        UserHelper.getUsersCollection().document(uid).collection("dates").document(mDate).set(restaurantToCreate);
    }

    /**
     * Delete the chosen restaurant from a user "Choose" list in FireStore for the current date
     * @param uid User id
     */
    static void unchooseRestaurant(String uid){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        UserHelper.getUsersCollection().document(uid).collection("dates").document(mDate).delete();
    }
}
