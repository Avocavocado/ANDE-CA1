package com.example.ande_munch.methods;

import android.app.admin.SecurityLog;
import android.util.Log;

import com.example.ande_munch.databinding.ActivityProfilePageBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProfileMethods {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LoginMethods loginMethods = new LoginMethods();
    String loggedInEmail = loginMethods.getUserEmail();
    ActivityProfilePageBinding binding;


    public void getUserProfileDetails(Callback callback) {
        db.collection("Users").document(loggedInEmail)
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

    public void updateUserProfile(Map<String, Object> updatedData, Callback callback) {
        db.collection("Users").document(loggedInEmail)
                .set(updatedData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("updateUserProfile", "User profile updated successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.d("updateUserProfile", "Error updating user profile: " + e.getMessage());
                    callback.onFailure(e);
                });
    }



}
