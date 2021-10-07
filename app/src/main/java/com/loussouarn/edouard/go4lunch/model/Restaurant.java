package com.loussouarn.edouard.go4lunch.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Restaurant {
    private String restaurantName;
    private Date dateCreated;
    private String address;
    private List<String> clientsTodayList;

    public Restaurant() {
    }

    public Restaurant(String restaurantName, String address) {
        this.restaurantName = restaurantName;
        this.clientsTodayList = new ArrayList<>();
        this.address = address;
    }

    // --- GETTERS ---
    public String getRestaurantName() {
        return restaurantName;
    }

    // --- SETTERS ---
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    @ServerTimestamp
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<String> getClientsTodayList() {
        return clientsTodayList;
    }

    public void setClientsTodayList(List<String> clientsTodayList) {
        this.clientsTodayList = clientsTodayList;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
