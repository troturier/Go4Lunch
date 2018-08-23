package com.openclassrooms.go4lunch.views;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.UserHelper;
import com.openclassrooms.go4lunch.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.openclassrooms.go4lunch.controllers.activities.MainActivity.mGoogleApiClient;

public class WorkmatesViewHolder extends RecyclerView.ViewHolder{

    // FOR DESIGN
    @BindView(R.id.workmates_list_name)
    TextView name;
    @BindView(R.id.workmates_list_imageview_profile)
    ImageView workmateIv;

    private RequestManager glide;

    WorkmatesViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithResult(User user, RequestManager glide) {

        this.glide = glide;

        String[] username = user.getUsername().split(" ");

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String mDate = format.format(date);
        Task<DocumentSnapshot> doc = UserHelper.getUsersCollection().document(Objects.requireNonNull(user.getUid())).collection("dates").document(mDate).get();
        final Boolean[] bool = new Boolean[1];
        doc.addOnCompleteListener(task -> {
            bool[0] = doc.getResult().exists();
            if (bool[0]) {
                DocumentSnapshot document = task.getResult();
                String resId = document.getString("uid");
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, resId)
                        .setResultCallback(places -> {
                            if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                name.setText(String.format("%s is eating at %2s", username[0], places.get(0).getName()));
                                name.setTextColor(Color.BLACK);
                                name.setTypeface(null, Typeface.NORMAL);
                            }
                        });
            }
            else {
                name.setText(String.format("%s hasn't decided yet", username[0]));
            }
        });
    }
}
