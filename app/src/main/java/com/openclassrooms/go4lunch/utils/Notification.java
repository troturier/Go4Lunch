package com.openclassrooms.go4lunch.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controllers.activities.DetailActivity;
import com.openclassrooms.go4lunch.helpers.RestaurantHelper;
import com.openclassrooms.go4lunch.helpers.UserHelper;
import com.openclassrooms.go4lunch.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.openclassrooms.go4lunch.controllers.activities.MainActivity.mGoogleApiClient;
import static com.openclassrooms.go4lunch.helpers.UserHelper.getCurrentUser;

public class Notification extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            // Show notification after received message
            createNotification();
        }
    }

    public static void createNotification (){

        createNotificationChannel();

        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient
                    .Builder(GetAppContext.getContext())
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .build();
        }

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

                                //noinspection MismatchedQueryAndUpdateOfCollection
                                List<User> mUsers = new ArrayList<>();
                                final CharSequence[] usersJoining = {GetAppContext.getContext().getString(R.string.people_joining_you)};
                                final Boolean[] firstPerson = {true};

                                CollectionReference path = RestaurantHelper.getRestaurantsCollection().document(places.get(0).getId()).collection("dates").document(mDate).collection("users");

                                Task<QuerySnapshot> docUsers = path.get();
                                docUsers.addOnCompleteListener(taskUsers -> {
                                    if (taskUsers.isSuccessful()) {
                                        for (QueryDocumentSnapshot documentUsers : Objects.requireNonNull(taskUsers.getResult())) {
                                            User user = documentUsers.toObject(User.class);
                                            String currentUid = Objects.requireNonNull(getCurrentUser()).getUid();
                                            String newUid = user.getUid();
                                            // Filtering the list to not include the current user
                                            if(!newUid.equals(currentUid)) {
                                                mUsers.add(user);
                                                String[] username = Objects.requireNonNull(user).getUsername().split(" ");
                                                if (firstPerson[0]){
                                                    usersJoining[0] = usersJoining[0] + " " + username[0];
                                                    firstPerson[0] = false;
                                                }
                                                else usersJoining[0] = usersJoining[0] + ", " + username[0];
                                            }
                                        }

                                        if(firstPerson[0]){
                                            usersJoining[0] = GetAppContext.getContext().getString(R.string.nobody_joined);
                                        }

                                        Intent intent = new Intent(GetAppContext.getContext(), DetailActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        Bundle bundle = new Bundle();
                                        if (places.get(0).getId() != null ) bundle.putString("place_id", places.get(0).getId());
                                        if (places.get(0).getWebsiteUri() != null )bundle.putString("place_website", Objects.requireNonNull(places.get(0).getWebsiteUri()).toString());
                                        if (places.get(0).getName() != null )bundle.putString("place_name", places.get(0).getName().toString());
                                        if (places.get(0).getPhoneNumber() != null )bundle.putString("place_phone", Objects.requireNonNull(places.get(0).getPhoneNumber()).toString());
                                        if (places.get(0).getAddress() != null )bundle.putString("place_address", Objects.requireNonNull(places.get(0).getAddress()).toString());
                                        if (places.get(0).getPlaceTypes() != null )bundle.putString("place_type", places.get(0).getPlaceTypes().toString());
                                        intent.putExtras(bundle);

                                        PendingIntent pendingIntent = PendingIntent.getActivity(GetAppContext.getContext(), 0, intent, 0);

                                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(GetAppContext.getContext(), "1")
                                                .setSmallIcon(R.drawable.ic_stat_name)
                                                .setContentTitle("Go4Lunch")
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setStyle(new NotificationCompat.InboxStyle()
                                                        .addLine(GetAppContext.getContext().getString(R.string.you_have_chosen) + places.get(0).getName())
                                                        .addLine(usersJoining[0]))
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true);

                                        NotificationManager notificationManager = (NotificationManager) GetAppContext.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

                                        // notificationId is a unique int for each notification that you must define
                                        Objects.requireNonNull(notificationManager).notify(1, mBuilder.build());

                                    }
                                });
                            }
                        });
            }
        });
    }

    private static void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Go4Lunch";
            String description = "Go4Lunch daily notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = GetAppContext.getContext().getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }
}
