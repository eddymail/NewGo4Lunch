package com.loussouarn.edouard.go4lunch.view.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.api.UserFirebase;
import com.loussouarn.edouard.go4lunch.model.User;
import com.loussouarn.edouard.go4lunch.utils.GpsTracker;
import com.loussouarn.edouard.go4lunch.view.fragment.MapFragment;
import com.loussouarn.edouard.go4lunch.view.fragment.RestaurantListFragment;
import com.loussouarn.edouard.go4lunch.view.fragment.WorkmateListFragment;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 123;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 456;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private final MapFragment mapFragment = new MapFragment();
    private boolean locationPermissionGranted = false;
    private String placeIdRestaurant = "restaurant_place_id";
    private PlacesClient placesClient;
    private GpsTracker gpsTracker;
    private Context context;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureToolBar();
        configureDrawerLayout();
        configureNavigationView();

        updateNavigationHeader();
        initializePlaces();
        getLocationPermission();

        loadFragment(new MapFragment());

        context = this;

    }

    private void configureNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void configureDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    // Configure the click on each item of the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_activity_main_search:

                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.TYPES);
                // Define the region
                RectangularBounds bounds = RectangularBounds.newInstance(
                        new LatLng(getLocation().getLatitude() - 0.01, getLocation().getLongitude() - 0.01),
                        new LatLng(getLocation().getLatitude() + 0.01, getLocation().getLongitude() + 0.01));
                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .setLocationBias(bounds)
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_drawer_lunch:
                startDetailActivity();
                break;
            case R.id.menu_drawer_settings:
                startSettingsActivity();
                break;

            case R.id.menu_drawer_logout:
                logOut();
                break;
            case R.id.mapViewFragment:
                loadFragment(mapFragment);
                return true;
            case R.id.listViewFragment:
                fragment = new RestaurantListFragment();
                loadFragment(fragment);
                return true;
            case R.id.workmatesListFragment:
                fragment = new WorkmateListFragment();
                loadFragment(fragment);
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_main_frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.detach(fragment);
        transaction.attach(fragment);
        transaction.commit();
    }

    @Nullable
    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }


    private void updateNavigationHeader() {
        if (getCurrentUser() != null) {
            final View headerView = navigationView.getHeaderView(0);

            TextView name = headerView.findViewById(R.id.nav_header_name_txt);
            TextView email = headerView.findViewById(R.id.nav_header_email_txt);
            ImageView illustrationUser = headerView.findViewById(R.id.nav_header_image_view);

            email.setText(getCurrentUser().getEmail());
            if (getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(getCurrentUser().getPhotoUrl())
                        .circleCrop()
                        .into(illustrationUser);
            }
            if (getCurrentUser().getDisplayName() != null) {
                name.setText(getCurrentUser().getDisplayName());
            } else {
                name.setText(getString(R.string.info_no_username_found));
            }

            if (getCurrentUser().getEmail() != null) {
                email.setText((getCurrentUser().getEmail()));
            } else {
                email.setText(getString(R.string.info_no_email_found));
            }
        }
    }

    // onNavigationItemSelected methods

    private void startDetailActivity() {
        String userId = UserFirebase.getCurrentUserId();
        UserFirebase.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                String lunch;
                if (user != null) {
                    lunch = user.getRestaurantOfTheDay();
                    if (lunch.equals("")) {
                        Toast.makeText(context, R.string.main_activity_no_choose_restaurant, Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(context, RestaurantDetailsActivity.class);
                        intent.putExtra(placeIdRestaurant, lunch);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivity(intent);
    }

    private void logOut() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(this, aVoid ->
        {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.main_activity_success_log_out), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializePlaces() {
        String apiKey = getString(R.string.api_key);
        //Initialize Places. For simplicity, the API key is hard-coded.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        // Construct a PlacesClient
        placesClient = Places.createClient(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Intent intent = new Intent(this, RestaurantDetailsActivity.class);
                intent.putExtra(placeIdRestaurant, place.getId());
                startActivity(intent);
                Log.e("Test", "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("STATUS", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Permissions

    public void getLocationPermission() {
        FusedLocationProviderClient mFusedLocationProviderClient;
        //getLocationPermission: getting location permissions
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public GpsTracker getLocation() {
        gpsTracker = new GpsTracker(this);
        if (gpsTracker.canGetLocation()) {
        } else {
            gpsTracker.showSettingsAlert();
        }
        return gpsTracker;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // onRequestPermissionsResult: called
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            // onRequestPermissionsResult: permissions failed
                            return;
                        } else {
                            // onRequestPermissionsResult: Permissions granted
                            locationPermissionGranted = true;
                            getLocationPermission();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back click to close menu
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

