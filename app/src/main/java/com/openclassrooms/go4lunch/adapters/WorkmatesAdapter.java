package com.openclassrooms.go4lunch.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.views.WorkmatesViewHolder;

import java.util.List;

public class WorkmatesAdapter extends RecyclerView.Adapter<WorkmatesViewHolder> {

    private final List<User> mUsers;
    private final RequestManager glide;

    public WorkmatesAdapter(final List<User> users, RequestManager glide){
        mUsers = users;
        this.glide = glide;
    }

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
