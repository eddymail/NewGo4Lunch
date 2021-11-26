package com.loussouarn.edouard.go4lunch.api;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.loussouarn.edouard.go4lunch.model.User;

import java.util.List;
import java.util.Objects;

public class UserFirebase {
    private static final String COLLECTION_NAME = "users";

    // --- COLLECTION REFERENCE ---
    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }


    // --- CREATE ---
    public static Task<Void> createUser(String uid, String username, String userEmail, String urlPicture) {
        User userToCreate = new User(uid, username, userEmail, urlPicture);
        return UserFirebase.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---
    public static Task<DocumentSnapshot> getUser(String uid){
        return UserFirebase.getUsersCollection().document(uid).get();
    }

    // --- GET CURRENT USER ID ---
    public static String getCurrentUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    // --- GET CURRENT USER NAME ---
    public static String getCurrentUserName() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
    }

    // --- GET CURRENT USER EMAIL ---
    public static String getCurrentUserEmail() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
    }
    // --- GET CURRENT USER URL PICTURE ---
    public static String getCurrentUserUrlPicture() {
        return FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
    }

    // --- UPDATE NAME---
    public static Task<Void> updateUsername(String username, String uid) {
        return UserFirebase.getUsersCollection().document(uid).update("username", username);
    }

    // --- UPDATE TODAY'S RESTAURANT ---
    public static Task<Void> updateRestaurantOfTheDay(String restaurantOfTheDay, String uid) {
        return UserFirebase.getUsersCollection().document(uid).update("restaurantOfTheDay", restaurantOfTheDay);
    }

    // --- UPDATE TODAY'S RESTAURANT---
    public static Task<Void> updateRestaurantOfTheDayName(String restaurantOfTheDayName, String uid) {
        return UserFirebase.getUsersCollection().document(uid).update("restaurantOfTheDayName", restaurantOfTheDayName);
    }

    // --- UPDATE DATE'S RESTAURANT---
    public static Task<Void> updateRestaurantChoiceDate(String restaurantChoiceDate, String uid) {
        return UserFirebase.getUsersCollection().document(uid).update("restaurantChoiceDate", restaurantChoiceDate);
    }

    // --- UPDATE LIKED RESTAURANT---
    public static Task<Void> updateLikedRestaurant(List<String> restaurantsLike, String uid) {
        return UserFirebase.getUsersCollection().document(uid).update("restaurantsLike", restaurantsLike);
    }

    // --- DELETE ---
    public static Task<Void> deleteUser(String uid) {
        return UserFirebase.getUsersCollection().document(uid).delete();
    }

    // -- GET ALL USERS --
    public static Query getAllUsers(){
        return UserFirebase.getUsersCollection();
    }

}
