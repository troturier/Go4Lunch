package com.openclassrooms.go4lunch.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.User;

import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private List<User> mUsers = Collections.emptyList();
    private static RequestManager glide;

    public UserAdapter(final Context context, final List<User> users, RequestManager glide){
        mUsers = users;
        UserAdapter.glide = glide;
    }

    public final void setUsers(final List<User> users){ mUsers = users; }

    @Override public final UserViewHolder onCreateViewHolder(final ViewGroup parent,
                                                                                final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View itemView = inflater.inflate(R.layout.activity_detail_recycler_view_item, parent, false);
        return new UserViewHolder(itemView);
    }


    @Override public final void onBindViewHolder(final UserViewHolder holder, final int position) {

        final User user = mUsers.get(position);
        holder.updateWithResult(user, glide);

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override public final int getItemCount() {
        return mUsers.size();
    }

    public User getUser(int position){
        return this.mUsers.get(position);
    }

}
