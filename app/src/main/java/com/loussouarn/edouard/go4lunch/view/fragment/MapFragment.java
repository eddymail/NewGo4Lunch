package com.loussouarn.edouard.go4lunch.view.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.api.RestaurantFirebase;
import com.loussouarn.edouard.go4lunch.model.Restaurant;
import com.loussouarn.edouard.go4lunch.utils.DateFormat;
import com.loussouarn.edouard.go4lunch.utils.GpsTracker;
import com.loussouarn.edouard.go4lunch.view.activities.RestaurantDetailsActivity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private View mapView;
    private final static String PLACE_ID_RESTAURANT = "restaurant_place_id";

    private final static String TAG = "MapsFragment";
    private static final float DEFAULT_ZOOM = 16f;

    private FloatingActionButton locationButton;
    private String today;
    private GpsTracker gpsTracker;
    private Marker myMarker;
    private GoogleMap map;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        locationButton = v.findViewById(R.id.fab_gps_location);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initMap();
    }

    private void initMap() {
        MapView mapView;
        mapView = this.mapView.findViewById(R.id.map);

        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), DEFAULT_ZOOM));

        PlacesClient placesClient = Places.createClient(getActivity());

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME, Place.Field.LAT_LNG);

        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
        if (ContextCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResult = placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        for (int i = 0; i < likelyPlaces.getPlaceLikelihoods().size(); i++) {
                            PlaceLikelihood place = likelyPlaces.getPlaceLikelihoods().get(i);
                            String restaurantName = place.getPlace().getName();
                            String restaurantPlaceId = place.getPlace().getId();

                            final MarkerOptions markerOptions = new MarkerOptions();

                            RestaurantFirebase.getRestaurant(place.getPlace().getId()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()){
                                        Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                                        Date dateRestaurantSheet;
                                        if (restaurant != null) {
                                            dateRestaurantSheet = restaurant.getDateCreated();
                                            DateFormat myDate = new DateFormat();
                                            String dateRegistered = myDate.getRegisteredDate(dateRestaurantSheet);

                                            if (dateRegistered.equals(today)) {
                                                int users = restaurant.getClientsTodayList().size();
                                                if (users > 0) {
                                                    markerOptions.position(place.getPlace().getLatLng())
                                                            .title(restaurantName)
                                                            .snippet(restaurantPlaceId)
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                                    myMarker = map.addMarker(markerOptions);
                                                    myMarker.setTag(0);
                                                }
                                            }
                                        }
                                    }
                                }
                            });

                            markerOptions.position(place.getPlace().getLatLng())
                                    .title(restaurantName)
                                    .snippet(restaurantPlaceId)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            myMarker = map.addMarker(markerOptions);
                            myMarker.setTag(0);

                            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                    lunchRestaurantDetailsActivity(marker.getSnippet());
                                }
                            });
                        }

                    } else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        }
        try {
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        init();
    }

    private GpsTracker getLocation(){
        gpsTracker = new GpsTracker(getContext());
        if(gpsTracker.canGetLocation()){
        }else{
            gpsTracker.showSettingsAlert();
        }
        return gpsTracker;
    }

    private void init() {
        // click on location button
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getLocation().getLatitude(), getLocation().getLongitude()), DEFAULT_ZOOM));
            }
        });
    }

    private void lunchRestaurantDetailsActivity(String id) {
        Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
        intent.putExtra(PLACE_ID_RESTAURANT, id);
        startActivity(intent);
    }

}