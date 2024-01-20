package com.example.ande_munch.classes;

import com.google.firebase.firestore.DocumentSnapshot;

public class Restaurant {
    public DocumentSnapshot data;
    public double avgPrice;
    public double avgRating;
    public double distance;

    public Restaurant (DocumentSnapshot data, double avgPrice, double avgRating, double distance) {
        this.data = data;
        this.avgPrice = avgPrice;
        this.avgRating = avgRating;
        this.distance = distance;
    }
}