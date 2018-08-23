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

public class WorkmatesAdapter extends RecyclerView.Adapter<WorkmatesViewHolder> {

    private List<User> mUsers = Collections.emptyList();
    private RequestManager glide;

    public WorkmatesAdapter(final Context context, final List<User> users, RequestManager glide){
        mUsers = users;
        this.glide = glide;
    }

    public final void setUsers(final List<User> users){ mUsers = users; }

    @Override public final WorkmatesViewHolder onCreateViewHolder(final ViewGroup parent,
                                                                                final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View itemView = inflater.inflate(R.layout.fragment_workmates_recycler_view_item, parent, false);
        return new WorkmatesViewHolder(itemView);
    }


    @Override public final void onBindViewHolder(final WorkmatesViewHolder holder, final int position) {

        final User user = mUsers.get(position);
        holder.updateWithResult(user, this.glide);

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
