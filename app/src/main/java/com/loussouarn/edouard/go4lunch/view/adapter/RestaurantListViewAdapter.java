package com.loussouarn.edouard.go4lunch.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.loussouarn.edouard.go4lunch.R;
import com.loussouarn.edouard.go4lunch.api.RestaurantFirebase;
import com.loussouarn.edouard.go4lunch.model.Restaurant;
import com.loussouarn.edouard.go4lunch.utils.DateFormat;
import com.loussouarn.edouard.go4lunch.utils.GpsTracker;
import com.loussouarn.edouard.go4lunch.utils.Rate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public abstract class RestaurantListViewAdapter extends RecyclerView.Adapter<RestaurantListViewAdapter.ViewHolder> {

    private static final String TAG = "OPENINGHOURS";
    private String today;
    private Context context;
    private List<PlaceLikelihood> list;
    private RequestManager glide;
    private boolean textOK;
    private GpsTracker gpsTracker;
    private OnRestaurantItemClickListener mListener;

    public RestaurantListViewAdapter(Context context, List<PlaceLikelihood> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        PlaceLikelihood place = list.get(position);

        holder.name.setText(place.getPlace().getName());
        holder.address.setText(place.getPlace().getAddress());

        // Opening hours
        Log.e(TAG, "place.getPlace().getOpeningHours()");

        holder.openHours.setTextColor(holder.openHours.getResources().getColor(R.color.colorGrey));
        if (place.getPlace().getOpeningHours() != null) {
            // default value that will be overwritten with today's schedules if the restaurant is open today
            isRestaurantOpen(place.getPlace().getOpeningHours(), holder);
            textOK = false;
        } else {
            holder.openHours.setText(R.string.list_restaurants_no_hours);
        }

        // //Distance
        float distance;
        float results[] = new float[10];
        double restaurantLat = Objects.requireNonNull(place.getPlace().getLatLng()).latitude;
        double restaurantLng = Objects.requireNonNull(place.getPlace().getLatLng()).longitude;
        double myLatitude = getLocation().getLatitude();
        double myLongitude = getLocation().getLongitude();
        Location.distanceBetween(myLatitude, myLongitude, restaurantLat, restaurantLng, results);
        distance = results[0];
        String dist = Math.round(distance) + "m";
        holder.distance.setText(dist);

        // Number of interested colleagues
        // Set to 0 by default
        holder.ratingNumbers.setText("0");
        DateFormat forToday = new DateFormat();
        today = forToday.getTodayDate();
        RestaurantFirebase.getRestaurant(place.getPlace().getId()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                    // Date check
                    Date dateRestaurantSheet;
                    if (restaurant != null) {
                        dateRestaurantSheet = restaurant.getDateCreated();
                        DateFormat myDate = new DateFormat();
                        String dateRegistered = myDate.getRegisteredDate(dateRestaurantSheet);
                        if (dateRegistered.equals(today)) {
                            // Number of interested workmates
                            List<String> listUsers = restaurant.getClientsTodayList();
                            String numberOfInterestedWorkmates = String.valueOf(listUsers.size());
                            holder.ratingNumbers.setText(numberOfInterestedWorkmates);
                        }
                    }
                }
            }
        });

        // Assign the number of stars
        if (place.getPlace().getRating() != null) {
            Double rate = place.getPlace().getRating();
            Rate myRate = new Rate(rate, holder.star1, holder.star2, holder.star3);
        } else {
            Rate myRate = new Rate(0, holder.star1, holder.star2, holder.star3);
        }

        // Images
        PlacesClient placesClient = Places.createClient(context);
        final List<PhotoMetadata> metadata = place.getPlace().getPhotoMetadatas();
        if (metadata == null || metadata.isEmpty()) {
            Log.w(TAG, "No photo metadata.");
            holder.picture.setImageResource(R.drawable.meal_picture);
            return;
        }
        final PhotoMetadata photoMetadata = metadata.get(0);
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            if (bitmap != null) {
                holder.picture.setImageBitmap(bitmap);
            } else
                holder.picture.setImageResource(R.drawable.meal_picture);

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                holder.picture.setImageResource(R.drawable.meal_picture);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public abstract void onItemClick(int position);

    public void setOnItemClickListener(OnRestaurantItemClickListener listener) {
        this.mListener = listener;
    }

    private GpsTracker getLocation() {
        gpsTracker = new GpsTracker(context);
        if (gpsTracker.canGetLocation()) {
        } else {
            gpsTracker.showSettingsAlert();
        }
        return gpsTracker;
    }

    private void isRestaurantOpen(OpeningHours restaurantDetail, ViewHolder holder) {
        holder.openHours.setTextColor(holder.openHours.getResources().getColor(R.color.black));
        holder.openHours.setText(holder.openHours.getResources().getString(R.string.list_restaurants_close_now));

        for (Period period : restaurantDetail.getPeriods()) {
            if (period.getClose() == null) {
                holder.openHours.setText(holder.openHours.getResources().getString(R.string.list_restaurants_open_now));
            } else {
                String text;
                String textTime;
                // text OK allows you to manage cases where there are several opening hours for the same day
                DateFormat hour = new DateFormat();
                switch (getOpeningHour(period)) {
                    case 1:
                        holder.openHours.setTextColor(holder.openHours.getResources().getColor(R.color.colorPrimary));
                        text = holder.openHours.getResources().getString(R.string.list_restaurants_open_at);
                        textTime = hour.getHoursFormat(String.valueOf(period.getOpen().getTime()));
                        text += textTime;
                        holder.openHours.setText(text);

                        break;
                    case 2:
                        holder.openHours.setTextColor(holder.openHours.getResources().getColor(R.color.colorGreen));
                        text = holder.openHours.getResources().getString(R.string.list_restaurants_open_until);

                        textTime = hour.getHoursFormat(String.valueOf(period.getClose().getTime()));
                        text += textTime;
                        holder.openHours.setText(text);

                        break;
                    case 3:
                        holder.openHours.setTextColor(holder.openHours.getResources().getColor(R.color.colorGrey));
                        holder.openHours.setText(holder.openHours.getResources().getString(R.string.list_restaurants_closed));
                }
            }
        }
    }

    // Method that get opening hours from GooglePlaces
    private int getOpeningHour(Period period) {

        Calendar calendar = Calendar.getInstance();
        int currentHour;
        if (calendar.get(Calendar.MINUTE) < 10) {
            currentHour = Integer.parseInt("" + calendar.get(Calendar.HOUR_OF_DAY) + "0" + calendar.get(Calendar.MINUTE));
        } else {
            currentHour = Integer.parseInt("" + calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE));
        }
        int closureHour = Integer.parseInt(String.valueOf(period.getClose().getTime()));
        int openHour = Integer.parseInt(String.valueOf(period.getOpen().getTime()));

        Log.d(TAG, "getOpeningHour: currenthour " + currentHour);
        if (currentHour < openHour) {
            textOK = true; // We are earlier than the first schedule so do not go compare with the second
            return 1;
        } else if (currentHour > openHour && currentHour < closureHour) {
            textOK = true; // We are in the first time slot so do not go compare with the second
            return 2;
        } else return 3;
    }

    public interface OnRestaurantItemClickListener {
        void onItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, openHours, distance, ratingNumbers;
        ImageView picture, star1, star2, star3;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.item_restaurant_name_txt);
            address = itemView.findViewById(R.id.item_restaurant_address_txt);
            openHours = itemView.findViewById(R.id.item_restaurant_hours_txt);
            distance = itemView.findViewById(R.id.item_restaurant_distance_txt);

            ratingNumbers = itemView.findViewById(R.id.item_restaurant_number_rating_txt);
            picture = itemView.findViewById(R.id.item_restaurant_picture);
            star1 = itemView.findViewById(R.id.item_restaurant_star_1);
            star2 = itemView.findViewById(R.id.item_restaurant_star_2);
            star3 = itemView.findViewById(R.id.item_restaurant_star_3);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (mListener != null) {
                        mListener.onItemClick(position);
                    }
                }
            });
        }
    }
}
