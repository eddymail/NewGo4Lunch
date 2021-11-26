package com.loussouarn.edouard.go4lunch.view.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.view.activities.RestaurantDetailsActivity;
import com.loussouarn.edouard.go4lunch.view.adapter.RestaurantListViewAdapter;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;

public class RestaurantListFragment extends Fragment {


    private String placeIdRestaurant = "restaurant_place_id";
    private RecyclerView recyclerView;
    private RestaurantListViewAdapter adapter;

    public RestaurantListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    //TODO Organiser le code

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_restaurant_list, container, false);


        PlacesClient placesClient = Places.createClient(getActivity());

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.ID, Place.Field.TYPES);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ContextCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResult = placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse   likelyPlaces = task.getResult();

                        recyclerView = getView().findViewById(R.id.listRestaurant);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


                        adapter = new RestaurantListViewAdapter(getContext(), likelyPlaces.getPlaceLikelihoods()) {
                            @Override
                            public void onItemClick(int position) {
                            }
                        };

                        recyclerView.setAdapter(adapter);
                        adapter.setOnItemClickListener(new RestaurantListViewAdapter.OnRestaurantItemClickListener(){
                            @Override
                            public void onItemClick(int position) {
                                launchRestaurantsDetailActivity(likelyPlaces.getPlaceLikelihoods().get(position).getPlace().getId());
                            }
                        });
                    } else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        }

        return v;
    }




    private void launchRestaurantsDetailActivity(String id) {
        Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
        intent.putExtra(placeIdRestaurant, id);
        Log.e("Test", "FRAGMENT resto id: " + id);
        startActivity(intent);
    }
}