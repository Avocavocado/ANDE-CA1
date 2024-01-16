package com.example.ande_munch;

import com.google.firebase.firestore.DocumentSnapshot;

public class Restaurant {
    public DocumentSnapshot data;
    public double avgPrice;
    public double avgRating;

    public Restaurant (DocumentSnapshot data, double avgPrice, double avgRating) {
        this.data = data;
        this.avgPrice = avgPrice;
        this.avgRating = avgRating;
    }
}