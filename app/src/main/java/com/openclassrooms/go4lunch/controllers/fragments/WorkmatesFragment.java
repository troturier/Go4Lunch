package com.openclassrooms.go4lunch.controllers.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.UserHelper;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.views.WorkmatesAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WorkmatesFragment extends Fragment {

    private WorkmatesAdapter mWorkmatesAdapter;

    @BindView(R.id.workmates_list_recycler_view)
    RecyclerView recyclerView;

    public WorkmatesFragment() {
        // Required empty public constructor
    }


    public static WorkmatesFragment newInstance() {
        WorkmatesFragment fragment = new WorkmatesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final List<User> mUsers = new ArrayList<>();

        View mPlacesView = inflater.inflate(R.layout.fragment_workmates, container, false);

        ButterKnife.bind(this, mPlacesView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        CollectionReference path = UserHelper.getUsersCollection();

        Task<QuerySnapshot> doc = path.get();
        doc.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    //String currentUid = Objects.requireNonNull(getCurrentUser()).getUid();
                    String newUid = user.getUid();
                    //if(!newUid.equals(currentUid)) {
                    mUsers.add(user);
                    //}
                }

                mWorkmatesAdapter = new WorkmatesAdapter(getContext(), mUsers);
                recyclerView.setAdapter(mWorkmatesAdapter);
            }
        });

        // Inflate the layout for this fragment
        return mPlacesView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
