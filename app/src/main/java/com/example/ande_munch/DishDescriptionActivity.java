package com.example.ande_munch;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ande_munch.methods.Callback;
import com.example.ande_munch.methods.LoginMethods;
import com.example.ande_munch.methods.ProfileMethods;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DishDescriptionActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    LoginMethods loginMethods = new LoginMethods();
    ProfileMethods profileMethods = new ProfileMethods();
    String loggedInEmail = loginMethods.getUserEmail();
    String dishName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dish_description);
        // Retrieve data from Intent
        dishName = getIntent().getStringExtra("dishName");
        String dishImageUrl = getIntent().getStringExtra("dishImageUrl");
        String dishDescription = getIntent().getStringExtra("dishDescription");

        // Find views and set the data
        TextView nameTextView = findViewById(R.id.dishNameTxt);
        ShapeableImageView dishImageView = findViewById(R.id.descriptionImage);
        TextView descriptionTextView = findViewById(R.id.descriptionDetailsTxt);

        nameTextView.setText(dishName);
        descriptionTextView.setText(dishDescription);
        Picasso.get().load(dishImageUrl).into(dishImageView);


        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the result to indicate that the Explore page should reload
                Intent data = new Intent();
                finish();
            }
        });

        ImageView profileImageView = findViewById(R.id.profileImageView);

        profileMethods.getUserProfileImage(new com.example.ande_munch.methods.Callback() {
            @Override
            public void onUserChecked(boolean userExists) {

            }

            @Override
            public void onUserDataFetched(List<Map<String, Object>> usersList) {

            }

            @Override
            public void onUserDataFetched(Map<String, Object> userDetails) {

            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onUserImageFetched(String profileImage) {
                if (profileImage != null && !profileImage.isEmpty()) {
                    Picasso.get()
                            .load(profileImage)
                            .resize(50,50)
                            .centerCrop()
                            .into(profileImageView);
                } else {
                    Log.d("DishDescriptionActivity", "Profile image URL is null or empty");
                }
            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DishDescriptionActivity.this, ProfilePage.class);
                startActivity(intent);
            }
        });

        Button unlockBtn = findViewById(R.id.unlockBtn);
        Button lockBtn = findViewById(R.id.lockBtn);
        checkDishUnlockStatus(dishName, unlocked -> {
            if (unlocked) {
                unlockBtn.setVisibility(View.GONE);
                lockBtn.setVisibility(View.VISIBLE);
            } else {
                lockBtn.setVisibility(View.GONE);
                unlockBtn.setVisibility(View.VISIBLE);
            }
        });
        lockBtn.setOnClickListener(v -> {
            lockDishStatus(dishName, false);
        });

        unlockBtn.setOnClickListener(v -> {
            unlockDishStatus(dishName, true);
        });
    }
    public void unlockDishStatus(String dishName, boolean status) {
        // The path to the nested field
        String fieldPath = "Dishes." + dishName;

        // Prepare the data to update
        Map<String, Object> dataToUpdate = new HashMap<>();
        dataToUpdate.put(fieldPath, status);

        // Update the specific nested field
        db.collection("Users").document(loggedInEmail)
                .update(dataToUpdate)
                .addOnSuccessListener(aVoid -> {
                    // Log success and show a toast message
                    Log.d("Firestore", "DocumentSnapshot successfully updated!");
                    Toast.makeText(DishDescriptionActivity.this, "Successfully unlocked!", Toast.LENGTH_SHORT).show();

                    Button unlockButton = findViewById(R.id.unlockBtn);
                    Button lockButton = findViewById(R.id.lockBtn);
                    unlockButton.setVisibility(View.GONE);
                    lockButton.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    // Log the error and show a toast message
                    Log.e("Firestore", "Error updating document", e);
                    Toast.makeText(DishDescriptionActivity.this, "Failed to unlock. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    public void lockDishStatus(String dishName, boolean status) {
        // The path to the nested field
        String fieldPath = "Dishes." + dishName;

        // Prepare the data to update
        Map<String, Object> dataToUpdate = new HashMap<>();
        dataToUpdate.put(fieldPath, status);

        // Update the specific nested field
        db.collection("Users").document(loggedInEmail)
                .update(dataToUpdate)
                .addOnSuccessListener(aVoid -> {
                    // Log success and show a toast message
                    Log.d("Firestore", "DocumentSnapshot successfully updated!");
                    Toast.makeText(DishDescriptionActivity.this, "Successfully locked!", Toast.LENGTH_SHORT).show();

                    Button lockButton = findViewById(R.id.lockBtn);
                    Button unlockButton = findViewById(R.id.unlockBtn);
                    lockButton.setVisibility(View.GONE);
                    unlockButton.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    // Log the error and show a toast message
                    Log.e("Firestore", "Error updating document", e);
                    Toast.makeText(DishDescriptionActivity.this, "Failed to lock. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkDishUnlockStatus(String dishName, Callback<Boolean> callback) {
        db.collection("Users").document(loggedInEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                        Map<String, Boolean> dishes = (Map<String, Boolean>) documentSnapshot.getData().get("Dishes");
                        boolean unlocked = dishes != null && Boolean.TRUE.equals(dishes.get(dishName));
                        callback.onComplete(unlocked);

                        // Get references to the buttons
                        Button unlockButton = findViewById(R.id.unlockBtn);
                        Button lockButton = findViewById(R.id.lockBtn);

                        if (unlocked) {
                            // If the dish is unlocked, hide the unlock button and show the lock button
                            unlockButton.setVisibility(View.GONE);
                            lockButton.setVisibility(View.VISIBLE);
                        } else {
                            // If the dish is not unlocked, show the unlock button and hide the lock button
                            unlockButton.setVisibility(View.VISIBLE);
                            lockButton.setVisibility(View.GONE);
                        }
                    } else {
                        // Document does not exist, treat as not unlocked
                        callback.onComplete(false);
                    }
                })
                .addOnFailureListener(e -> {
                    // Error getting document, treat as not unlocked
                    callback.onComplete(false);
                });
    }


    // Define the Callback interface
    interface Callback<T> {
        void onComplete(T result);
    }

}
