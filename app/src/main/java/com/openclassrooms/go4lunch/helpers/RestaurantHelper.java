package com.openclassrooms.go4lunch.helpers;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.utils.GetAppContext;

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
        Restaurant restaurantToCreate = new Restaurant(id);
        RestaurantHelper.getRestaurantsCollection().document(id).set(restaurantToCreate);
    }

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

    public static void deleteRestaurant(String id) {
        RestaurantHelper.getRestaurantsCollection().document(id).delete();
    }

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

    public static void deleteRestaurantInFireStoreForLike(String id){
        if (checkIfRestaurantExists(id)){
            RestaurantHelper.deleteRestaurant(id);
        }
        if(UserHelper.getCurrentUser() != null)
            RestaurantHelper.dislikeRestaurant(UserHelper.getCurrentUser().getUid(), id);
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

    // --- CHECK FUNCTION ---



    public static void checkIfUserDocumentExistsForChosen(String uid, String id){

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
                RestaurantHelper.unchooseRestaurant(uid, restaurant.getId());
                RestaurantHelper.chooseRestaurant(uid, id);
                UserHelper.chooseRestaurant(uid, id);
            }
            else{
                RestaurantHelper.chooseRestaurant(uid, id);
                UserHelper.chooseRestaurant(uid, id);
            }
        });
    }

    public static Boolean checkIfRestaurantExists(String id){
        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).get();
        final Boolean[] bool2 = new Boolean[1];
        bool2[0] = false;
        doc.addOnCompleteListener(task -> bool2[0] = doc.getResult().exists());
        return bool2[0];
    }

    public static void checkIfDocumentExistsForLike(String uid, String id, CheckBox cb_like){
        Task<DocumentSnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("likes").document(uid).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(task -> {
            bool[0] = doc.getResult().exists();
            if(bool[0]){
                cb_like.setChecked(true);
            }
        });
    }


    @SuppressLint("DefaultLocale")
    public static void checkNumberOfWorkmates(String id, TextView workmatesTv, ImageView workmatesIc){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);

        Task<QuerySnapshot> doc = RestaurantHelper.getRestaurantsCollection().document(id).collection("dates").document(mDate).collection("users").get();
        doc.addOnCompleteListener(task -> {
            if (task.getResult().size() > 0){
                workmatesTv.setText(String.format("(%d)", task.getResult().size()));
                workmatesTv.setVisibility(View.VISIBLE);
                workmatesIc.setVisibility(View.VISIBLE);
            }
        });
    }

    @Nullable
    private static FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }
}
