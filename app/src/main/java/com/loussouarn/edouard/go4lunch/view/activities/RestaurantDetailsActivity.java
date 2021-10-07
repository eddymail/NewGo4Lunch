package com.loussouarn.edouard.go4lunch.view.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.api.RestaurantFirebase;
import com.loussouarn.edouard.go4lunch.api.UserFirebase;
import com.loussouarn.edouard.go4lunch.model.Restaurant;
import com.loussouarn.edouard.go4lunch.model.User;
import com.loussouarn.edouard.go4lunch.utils.DateFormat;
import com.loussouarn.edouard.go4lunch.utils.Rate;
import com.loussouarn.edouard.go4lunch.view.adapter.ClientListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;

public class RestaurantDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ClientListAdapter adapter;

    private String PLACE_ID_RESTAURANT = "restaurant_place_id";

    private TextView name;
    private TextView address;
    private ImageView picture;
    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private ImageButton callButton;
    private ImageButton webSiteButton;
    private ImageButton likeButton;
    private FloatingActionButton choiceButton;
    private RecyclerView recyclerView;

    private String restoToday;
    private List<String> listRestaurantsLike = new ArrayList<>();
    private List<String> listId;
    private String restaurantAddress;
    private String phoneNumber;
    private String webSite;
    private String restaurantId;
    private double restaurantRate;
    private String userId;
    private String restaurantName;
    private String lastRestaurantId;
    private String lastRestaurantDate;
    private String lastRestaurantName;
    private String today;

    private Place restaurant;

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        initActivity();
        configureRecyclerview();
        displayRecyclerView();

        getCurrentUser();
        recoverRestaurantId();

        getRestaurantDetails();

        updateLikeRestaurantView(restaurantId);
        updateTheRestaurantOfTheDayView(restaurantId);

    }



    private void initActivity() {
        name = findViewById(R.id.details_name_txt);
        address = findViewById(R.id.details_address_txt);
        picture = findViewById(R.id.restaurant_details_picture);
        star1 = findViewById(R.id.details_star_1_image);
        star2 = findViewById(R.id.details_star_2_image);
        star3 = findViewById(R.id.details_star_3_image);
        callButton = findViewById(R.id.details_call_button);
        webSiteButton = findViewById(R.id.details_website_button);
        likeButton = findViewById(R.id.details_like_button);
        choiceButton = findViewById(R.id.detail_choice_fab);
        recyclerView = findViewById(R.id.details_restaurant_recyclerview);

        callButton.setOnClickListener(this);
        webSiteButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);
        choiceButton.setOnClickListener(this);

    }

    // RecyclerView
    private void configureRecyclerview() {
        RestaurantFirebase.getRestaurant(restaurantId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);

                    Date dateRestaurantSheet;
                    if (usersToday != null) {

                        dateRestaurantSheet = usersToday.getDateCreated();
                        DateFormat myDate = new DateFormat();
                        String dateRegistered = myDate.getRegisteredDate(dateRestaurantSheet);

                        if (dateRegistered.equals(today)) {

                            listId = usersToday.getClientsTodayList();

                            if (listId != null) {
                                adapter = new ClientListAdapter(listId, Glide.with(recyclerView));
                                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                }
            }
        });
    }

    private void displayRecyclerView() {

        if (listId == null) {
            recyclerView.setVisibility(View.GONE);
        } else {
            configureRecyclerview();
        }
    }

    private void getCurrentUser() {
        userId = UserFirebase.getCurrentUserId();
    }

    private void recoverRestaurantId() {
        restaurantId = getIntent().getStringExtra(PLACE_ID_RESTAURANT);
    }

    // Get and display restaurant details
    private void getRestaurantDetails() {
        PlacesClient placesClient = Places.createClient(this);

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER);

        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(restaurantId, placeFields);

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {

                restaurant = response.getPlace();
                restaurantName = restaurant.getName();
                restaurantAddress = restaurant.getAddress();
                phoneNumber = restaurant.getPhoneNumber();
                webSite = String.valueOf(restaurant.getWebsiteUri());

                address.setText(restaurantAddress);
                name.setText(restaurantName);

                if (restaurant.getRating() != null) {
                    restaurantRate = restaurant.getRating();
                    Rate rate = new Rate(restaurantRate, star1, star2, star3);
                } else {
                    Rate rate = new Rate(0, star1, star2, star3);
                }

                final List<PhotoMetadata> metadata = restaurant.getPhotoMetadatas();
                if (metadata == null || metadata.isEmpty()) {
                    Log.w(TAG, "No photo metadata.");
                    picture.setImageResource(R.drawable.meal_picture);
                    return;
                }

                final PhotoMetadata photoMetadata = metadata.get(0);
                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    if (bitmap != null) {
                        picture.setImageBitmap(bitmap);
                    } else
                        picture.setImageResource(R.drawable.meal_picture);

                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        final ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + exception.getMessage());
                        final int statusCode = apiException.getStatusCode();
                        picture.setImageResource(R.drawable.meal_picture);
                    }
                });

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();
                }
            });
        }
    }

    // Firebase actions


    private void addUserInRestaurant(final String id, final String name) {
        RestaurantFirebase.getRestaurant(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);

                    Date dateRestoSheet;
                    if (usersToday != null) {
                        dateRestoSheet = usersToday.getDateCreated();
                        DateFormat myDate = new DateFormat();
                        String dateRegistered = myDate.getRegisteredDate(dateRestoSheet);
                        if (dateRegistered.equals(today)) {
                            List<String> listUsersToday = usersToday.getClientsTodayList();
                            listUsersToday.add(userId);
                            RestaurantFirebase.updateClientsTodayList(listUsersToday, id);
                        } else {
                            RestaurantFirebase.createRestaurant(id, name, restaurantAddress);
                            updateUserTodayInFirebase(userId, id);
                        }
                    }
                } else {
                    RestaurantFirebase.createRestaurant(id, name, restaurantAddress);
                    updateUserTodayInFirebase(userId, id);
                }
            }
        });
    }

    private void removeUserInRestaurant(final String id, final String name) {
        RestaurantFirebase.getRestaurant(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant usersToday = documentSnapshot.toObject(Restaurant.class);
                    Date dateRestoSheet;
                    if (usersToday != null) {
                        dateRestoSheet = usersToday.getDateCreated();

                        DateFormat myDate = new DateFormat();
                        String dateRegistered = myDate.getRegisteredDate(dateRestoSheet);

                        if (dateRegistered.equals(today)) {
                            List<String> listUsersToday;
                            listUsersToday = usersToday.getClientsTodayList();
                            listUsersToday.remove(userId);
                            RestaurantFirebase.updateClientsTodayList(listUsersToday, id);
                        } else {
                            RestaurantFirebase.createRestaurant(id, name, restaurantAddress);
                        }
                    }
                }
            }
        });
    }

    private void updateUserTodayInFirebase(String myId, String myRestoId) {
        List<String> listUsersToday = new ArrayList<>();
        listUsersToday.add(myId);
        RestaurantFirebase.updateClientsTodayList(listUsersToday, myRestoId);
    }

    private void updateLikeRestaurantInFirebase(final String idResto) {
        Log.d(TAG, "updateLikeInFirebase: idresto " + idResto);
        UserFirebase.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "onSuccess: documentSnapshot exists");
                    listRestaurantsLike = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestaurantLike();
                    if (listRestaurantsLike != null) {
                        if (listRestaurantsLike.contains(idResto)) {
                            Log.d(TAG, "onSuccess: retirer le resto");
                            listRestaurantsLike.remove(idResto);
                            likeButton.setImageResource(R.drawable.ic_baseline_star_border_24);
                        } else {
                            Log.d(TAG, "onSuccess: ajouter le resto");
                            listRestaurantsLike.add(idResto);
                            likeButton.setImageResource(R.drawable.ic_baseline_star_border_24);
                        }
                    }
                    UserFirebase.updateLikedRestaurant(listRestaurantsLike, userId);
                }
            }
        });
    }

    private void updateRestaurantOfTheDayInFirebase(final String restaurantOfTheDayId, final String restaurantOfTheDayName) {
        UserFirebase.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    User myRestaurantOfTheDay = documentSnapshot.toObject(User.class);
                    if (myRestaurantOfTheDay != null) {
                        lastRestaurantId = myRestaurantOfTheDay.getRestaurantOfTheDay();
                        lastRestaurantDate = myRestaurantOfTheDay.getRestaurantChoiceDate();
                        lastRestaurantName = myRestaurantOfTheDay.getRestaurantOfTheDayName();

                        if (lastRestaurantId != null && lastRestaurantId.length() > 0 && lastRestaurantDate.equals(today)) {
                            if (lastRestaurantId.equals(restaurantOfTheDayId)) {
                                choiceButton.setImageResource(R.drawable.ic_not_go_restaurant);
                                updateRestaurantInUser("", "", today);
                                // This user is also removed from Restaurant from the guest list
                                removeUserInRestaurant(restaurantOfTheDayId, restaurantOfTheDayName);
                            } else {
                                // It was not this one so we replace it with the new choice in User
                                choiceButton.setImageResource(R.drawable.ic_go_restaurant);
                                updateRestaurantInUser(restaurantOfTheDayId, restaurantOfTheDayName, today);
                                // We delete the user from the list of guests of his former restaurant chosen
                                removeUserInRestaurant(lastRestaurantId, lastRestaurantName);
                                // and we add the user in the list of guests of the new restaurant
                                addUserInRestaurant(restaurantOfTheDayId, restaurantOfTheDayName);
                            }
                        } else {
                            // No restaurant was registered, so we save this one in User
                            updateRestaurantInUser(restaurantOfTheDayId, restaurantOfTheDayName, today);
                            choiceButton.setImageResource(R.drawable.ic_go_restaurant);
                            // and we add this guest to the restaurant list
                            addUserInRestaurant(restaurantOfTheDayId, restaurantOfTheDayName);
                        }
                    }
                }
            }
        });
    }

    private void updateRestaurantInUser(String id, String name, String date) {
        UserFirebase.updateRestaurantOfTheDay(id, userId);
        UserFirebase.updateRestaurantOfTheDayName(name, userId);
        UserFirebase.updateRestaurantChoiceDate(date, userId);
    }


    // Update display

    private void updateLikeRestaurantView(String id) {
        final String idLike = id;
        UserFirebase.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                listRestaurantsLike = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestaurantLike();
                if (listRestaurantsLike != null) {
                    if (listRestaurantsLike.contains(idLike)) {
                        likeButton.setImageResource(R.drawable.ic_star_orange_24dp);
                    } else {
                        likeButton.setImageResource(R.drawable.ic_baseline_star_border_24);
                    }
                } else {
                    likeButton.setImageResource(R.drawable.ic_baseline_star_border_24);
                }
            }
        });
    }

    private void updateTheRestaurantOfTheDayView(String id) {
        final String idToday = id;

        // Default values
        choiceButton.setImageResource(R.drawable.ic_not_go_restaurant);

        UserFirebase.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                restoToday = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestaurantOfTheDay();
                lastRestaurantDate = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getRestaurantChoiceDate();

                if (restoToday != null && restoToday.length() > 0 && lastRestaurantDate.equals(today)) { // We check that there is a restaurant registered and that it was registered today
                    if (restoToday.equals(idToday)) {
                        choiceButton.setImageResource(R.drawable.ic_go_restaurant);
                    }
                }
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.details_call_button:
                if (phoneNumber != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.details_restaurant_no_phone), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.details_website_button:
                if (webSite != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webSite)));
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.details_restaurant_no_website), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.details_like_button:
                if (restaurantId != null) {
                    updateLikeRestaurantInFirebase(restaurantId);
                }
                break;

            case R.id.detail_choice_fab:
                if (restaurantId != null) {
                    updateRestaurantOfTheDayInFirebase(restaurantId, restaurantName);
                }
                break;

        }
    }
}