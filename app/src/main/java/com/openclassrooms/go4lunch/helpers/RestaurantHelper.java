package com.openclassrooms.go4lunch.helpers;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Helper used to communicate with the Restaurant part of the Firestore database
 */
public class RestaurantHelper {
    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---

    /**
     * Retrieve the Restaurant CollectionReference
     * @return CollectionReference
     */
    public static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    /**
     * Create a restaurant in FireStore
     * @param id Restaurant id
     */
    private static void createRestaurant(String id) {
        Restaurant restaurantToCreate = new Restaurant(id);
        RestaurantHelper.getRestaurantsCollection().document(id).set(restaurantToCreate);
    }

    /**
     * Create a restaurant in FireStore for the "Choose" functionality
     * @param id Restaurant id
     * @return Boolean
     */
    @SuppressWarnings("SameReturnValue")
    public static boolean createRestaurantInFireStoreForChoose(String id){
        if (!checkIfRestaurantExists(id)){
            RestaurantHelper.createRestaurant(id);
            if(UserHelper.getCurrentUser() != null) {
                checkIfUserDocumentExistsForChosen(UserHelper.getCurrentUser().getUid(), id);
            }
        }
        else {
            if(UserHelper.getCurrentUser() != null) {
                checkIfUserDocumentExistsForChosen(UserHelper.getCurrentUser().getUid(), id);
            }
        }
        return true;
    }

    /**
     * Create a restaurant in FireStore for the "Like" functionality
     * @param id Restaurant id
     */
    public static void createRestaurantInFireStoreForLike(String id){
        if (!checkIfRestaurantExists(id)){
            RestaurantHelper.createRestaurant(id);
            if(UserHelper.getCurrentUser() != null)
                RestaurantHelper.likeRestaurant(UserHelper.getCurrentUser().getUid(), id);
        }
        else {
            if(UserHelper.getCurrentUser() != null)
                RestaurantHelper.likeRestaurant(UserHelper.getCurrentUser().getUid(), id);
        }
    }

    // --- DELETE ---

    /**
     * Delete a restaurant in FireStore
     * @param id Restaurant id
     */
    private static void deleteRestaurant(String id) {
        RestaurantHelper.getRestaurantsCollection().document(id).delete();
    }

    /**
     * Delete a restaurant in FireStore for the "Choose" functionality
     * @param id Restaurant id
     * @return Boolean
     */
    @SuppressWarnings("SameReturnValue")
    public static boolean deleteRestaurantInFireStoreForChoose(String id){
        if (checkIfRestaurantExists(id)){
            RestaurantHelper.deleteRestaurant(id);
        }
        if(UserHelper.getCurrentUser() != null) {
            RestaurantHelper.unchooseRestaurant(UserHelper.getCurrentUser().getUid(), id);
            UserHelper.unchooseRestaurant(UserHelper.getCurrentUser().getUid());
        }
        return false;
    }

    /**
     * Delete a restaurant in FireStore for the "Like" functionality
     * @param id Restaurant id
     */
    public static void deleteRestaurantInFireStoreForLike(String id){
        if (checkIfRestaurantExists(id)){
            RestaurantHelper.deleteRestaurant(id);
        }
        if(UserHelper.getCurrentUser() != null)
            RestaurantHelper.dislikeRestaurant(UserHelper.getCurrentUser().getUid(), id);
    }

    // --- LIKE FUNCTION ---

    /**
     * Add a user to a restaurant "Like" list in FireStore
     * @param uid User id
     * @param id Restaurant id
     */
    private static void likeRestaurant(String uid, String id){
        User userToCreate = new User(uid);
        RestaurantHelper.getRestaurantsCollection().document(id).collection("likes").document(uid).set(userToCreate);
    }

    /**
     * Delete a user to a restaurant "Like" list in FireStore
     * @param uid User id
     * @param id Restaurant id
     */
    private static void dislikeRestaurant(String uid, String id){
        RestaurantHelper.getRestaurantsCollection().document(id).collection("likes").document(uid).delete();
    }

    // --- CHOOSE FUNCTION ---

    /**
     * Add a user to a restaurant "Choose" list in FireStore
     * @param uid User id
     * @param id Restaurant id
     */
    private static void chooseRestaurant(String uid, String id){
        User userToCreate = new User(uid, Objects.requireNonNull(UserHelper.getCurrentUser()).getDisplayName());
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").document(uid).set(userToCreate);
    }

    /**
     * Delete a user to a restaurant "Choose" list in FireStore
     * @param uid User id
     * @param id Restaurant id
     */
    private static void unchooseRestaurant(String uid, String id){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").document(uid).delete();
    }

    // --- CHECK FUNCTION ---


    /**
     * Check if a user has already chosen the given restaurant and call the appropriate functions in consequence
     * @param uid User id
     * @param id Restaurant id
     */
    private static void checkIfUserDocumentExistsForChosen(String uid, String id){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        Task<DocumentSnapshot> doc = UserHelper.getUsersCollection().document(uid).collection("dates").document(mDate).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(task -> {
            bool[0] = Objects.requireNonNull(doc.getResult()).exists();
            if(bool[0]){
                DocumentSnapshot document = task.getResult();
                Restaurant restaurant = Objects.requireNonNull(document).toObject(Restaurant.class);
                RestaurantHelper.unchooseRestaurant(uid, Objects.requireNonNull(restaurant).getId());
                RestaurantHelper.chooseRestaurant(uid, id);
                UserHelper.chooseRestaurant(uid, id);
            }
            else{
                RestaurantHelper.chooseRestaurant(uid, id);
                UserHelper.chooseRestaurant(uid, id);
            }
        });
    }

    /**
     * Check if a given restaurant exists in the FireStore database
     * @param id Restaurant id
     */
    private static Boolean checkIfRestaurantExists(String id){
        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).get();
        final Boolean[] bool2 = new Boolean[1];
        bool2[0] = false;
        doc.addOnCompleteListener(task -> bool2[0] = Objects.requireNonNull(doc.getResult()).exists());
        return bool2[0];
    }

    /**
     * Check if the given has already liked a restaurant and update the Checkbox of its corresponding DetailActivity
     * @param uid User id
     * @param id Restaurant id
     * @param cb_like Checkbox from DetailActivity
     */
    public static void checkIfDocumentExistsForLike(String uid, String id, CheckBox cb_like){
        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("likes").document(uid).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(task -> {
            bool[0] = Objects.requireNonNull(doc.getResult()).exists();
            if(bool[0]){
                cb_like.setChecked(true);
            }
        });
    }

    /**
     * Update workmates UI elements of a RestaurantList Fragment item according to the FireStore database
     * @param id Restaurant id
     * @param workmatesTv Workmates TextView
     * @param workmatesIc Workmates Icon
     */
    @SuppressLint("DefaultLocale")
    public static void checkNumberOfWorkmates(String id, TextView workmatesTv, ImageView workmatesIc){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        Task<QuerySnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").get();
        doc.addOnCompleteListener(task -> {
            if (Objects.requireNonNull(task.getResult()).size() > 0){
                workmatesTv.setText(String.format("(%d)", task.getResult().size()));
                workmatesTv.setVisibility(View.VISIBLE);
                workmatesIc.setVisibility(View.VISIBLE);
            }
            else {
                workmatesTv.setVisibility(View.INVISIBLE);
                workmatesIc.setVisibility(View.INVISIBLE);
            }
        });
    }

}
