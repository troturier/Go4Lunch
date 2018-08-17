package com.openclassrooms.go4lunch.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserViewHolder extends RecyclerView.ViewHolder{

    // FOR DESIGN
    @BindView(R.id.detail_list_name)
    TextView name;
    @BindView(R.id.detail_list_imageview_profile)
    ImageView detailIv;

    static private RequestManager glide;

    UserViewHolder(View itemView){
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithResult(User user, RequestManager glide){

        this.glide = glide;

        String[] username = user.getUsername().split(" ");

        name.setText(username[0] + " is joining!");
    }
}
