package com.example.ande_munch.methods;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class DishMethods {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getAllDishes(Callback callback) {
        db.collection("Dishes").document()
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userDetails = documentSnapshot.getData();
                        Log.d("getUserDetails", "Got User Details: " + userDetails);
                        callback.onUserDataFetched(userDetails);
                    } else {
                        Log.d("getUserDetails", "No such document");
                        callback.onFailure(new Exception("No such document"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("getUserDetails", "Error getting user details: " + e.getMessage());
                    callback.onFailure(e); // Handle the failure case
                });
    }
}
