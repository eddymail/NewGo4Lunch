package com.loussouarn.edouard.go4lunch.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String userName;
    private String userEmail;
    private String restaurantOfTheDay;
    private String restaurantOfTheDayName;
    private String restaurantChoiceDate;
    @Nullable
    private String urlPicture;
    private List<String> restaurantLike;


    public User() { }

    public User(String uid, String userName, String userEmail, String urlPicture) {
        this.uid = uid;
        this.userName = userName;
        this.userEmail = userEmail;
        this.restaurantOfTheDay = "";
        this.restaurantOfTheDayName = "";
        this.restaurantChoiceDate = "";
        this.urlPicture = urlPicture;
        this.restaurantLike = new ArrayList<>();

    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return  userEmail;}
    public String getRestaurantOfTheDay() { return restaurantOfTheDay;}
    public String getRestaurantOfTheDayName() {return restaurantOfTheDayName;}
    public String getRestaurantChoiceDate() {return restaurantChoiceDate;}
    public String getUrlPicture() { return urlPicture; }
    public List<String> getRestaurantLike() { return restaurantLike; }

    // --- SETTERS ---
    public void setUid(String uid) { this.uid = uid; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail;}
    public void setRestaurantOfTheDay(String restaurantOfTheDay) {this.restaurantOfTheDay = restaurantOfTheDay;}
    public void setRestaurantOfTheDayName(String restaurantOfTheDayName) {this.restaurantOfTheDayName = restaurantOfTheDayName;}
    public void setRestaurantChoiceDate(String restaurantChoiceDate) {this.restaurantChoiceDate = restaurantChoiceDate;}
    public void setUrlPicture(String urlPicture) { this.urlPicture = urlPicture; }
    public void setRestaurantLike(List<String> restaurantLike) {this.restaurantLike = restaurantLike;}
}
