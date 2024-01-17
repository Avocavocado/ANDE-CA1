package com.example.ande_munch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ExploreRestaurants extends AppCompatActivity {

    private static final String TAG = "ExploreRestuarants";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explore_restaurants);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Restaurants")
                .get()
                .addOnSuccessListener(result -> {
                    for (DocumentSnapshot document : result.getDocuments()) {
                        Log.i(TAG,document.getId() + " => " + document.getData());
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.i(TAG, "Error getting documents." + exception);
                });
    }
}
