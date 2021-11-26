package com.loussouarn.edouard.go4lunch.api;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loussouarn.edouard.go4lunch.model.Restaurant;

import java.util.List;

public class RestaurantFirebase {
    private static final String COLLECTION_NAME = "restaurants";

    // Collection reference
    public static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // Create
    public static Task<Void> createRestaurant(String restaurantId, String restaurantName, String address) {
        Restaurant restaurantToCreate = new Restaurant(restaurantName, address);
        return RestaurantFirebase.getRestaurantsCollection().document(restaurantId).set(restaurantToCreate);
    }

    // Get
    public static Task<DocumentSnapshot> getRestaurant(String restaurantId){
        return RestaurantFirebase.getRestaurantsCollection().document(restaurantId).get();
    }

    // Update
    public static Task<Void> updateClientsTodayList(List<String> clientsTodayList, String restaurantId) {
        return RestaurantFirebase.getRestaurantsCollection().document(restaurantId).update("clientsTodayList", clientsTodayList);
    }
}
