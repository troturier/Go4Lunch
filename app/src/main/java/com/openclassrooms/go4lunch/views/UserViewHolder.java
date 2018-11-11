package com.openclassrooms.go4lunch.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.helpers.UserHelper;
import com.openclassrooms.go4lunch.models.User;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Class used to create a new RecyclerView item of DetailActivity
 */
public class UserViewHolder extends RecyclerView.ViewHolder{

    // FOR DESIGN
    @BindView(R.id.detail_list_name)
    TextView name;
    @BindView(R.id.detail_list_imageview_profile)
    ImageView detailIv;

    public UserViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithResult(User user, RequestManager glide) {

        DocumentReference path = UserHelper.getUsersCollection().document(user.getUid());

        Task<DocumentSnapshot> doc = path.get();
        doc.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user2 = Objects.requireNonNull(task.getResult()).toObject(User.class);

                        String[] username = Objects.requireNonNull(user2).getUsername().split(" ");

                        if(user2.getUrlPicture() != null){
                            glide.load(user2.getUrlPicture())
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(detailIv);
                        }

                        name.setText(String.format("%s is joining!", username[0]));
                    }
                });
    }
}
