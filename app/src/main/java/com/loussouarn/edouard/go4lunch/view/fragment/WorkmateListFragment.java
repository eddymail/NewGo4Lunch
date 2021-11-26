package com.loussouarn.edouard.go4lunch.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.api.UserFirebase;
import com.loussouarn.edouard.go4lunch.model.User;
import com.loussouarn.edouard.go4lunch.view.activities.RestaurantDetailsActivity;
import com.loussouarn.edouard.go4lunch.view.adapter.WorkmateListAdapter;

public class WorkmateListFragment extends Fragment {

    private final static String PLACE_ID = "restaurant_place_id";
    private RecyclerView recyclerView;
    private WorkmateListAdapter adapter;


    public WorkmateListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_workmates_list, container, false);
        recyclerView = v.findViewById(R.id.workmate_list);

        configureRecyclerview();

        return v;
    }

    private void configureRecyclerview() {

        Query allUsers = UserFirebase.getAllUsers();

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(allUsers, User.class)
                .build();

        adapter = new WorkmateListAdapter(options, Glide.with(recyclerView));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new WorkmateListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    String restaurantId;
                    if (user != null) {
                        restaurantId = user.getRestaurantOfTheDay();
                       // Log.e("Test", "WorkmateFRAGMENT user = "+ user + " restaurantId = " + restaurantId);
                        if (restaurantId.length() > 1) {
                            Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
                            intent.putExtra(PLACE_ID, restaurantId);
                          //  Log.e("Test", "WorkMatFragment RestaurantId" + restaurantId);
                            startActivity(intent);
                        } else
                            Toast.makeText(getContext(), R.string.list_workmates_adapter_no_lunch, Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}