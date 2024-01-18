package com.example.ande_munch.methods;

import android.content.Context;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.ande_munch.methods.Callback;

public class PartyMethods {
    // Initialize attributes
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 4;
    boolean partyCodeExists = false;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Method to scan through all parties and check if code entered is one of them
    public void checkPartyCode(Context context, String partyCode, OnSuccessListener<Boolean> onSuccessListener, OnFailureListener onFailureListener) {
        // Connect to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Scan through all parties
        db.collection("Parties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("PartyMethods", "The party code is: " + partyCode);
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d("PartyMethods", "The document is: " + document.getId());
                        if (document.getId().equals(partyCode)) {
                            boolean partyCodeExists = true;
                            onSuccessListener.onSuccess(partyCodeExists);
                            showToast(context, "Party found!");
                            return;
                        }
                    }
                    onSuccessListener.onSuccess(false);
                    showToast(context, "The Party was not found.");
                })
                .addOnFailureListener(e -> {
                    Log.d("PartyMethods", "Error getting documents: " + e.getMessage());
                    onFailureListener.onFailure(e);
                    showToast(context, "Error: " + e.getMessage());
                });
    }

    public String PartyCodeGenerator() {
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            codeBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        String partyCode = codeBuilder.toString();
        System.out.println("The party code is: " + partyCode);

        return partyCode;
    }

    // Check if the current user exists in the party
    public void checkUserInParty(String email, String PartyCode, Callback callback) {
        // Connect to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Parties").document(PartyCode).collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean userExists = false;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.getId().equals(email)) {
                            userExists = true;
                            break;
                        }
                    }
                    callback.onUserChecked(userExists);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    callback.onUserChecked(false);
                });
    }

    // Method to get each party member's dietary restrictions
    public void getDietaryRestrictions(String email, DietaryRestrictionsCallback callback) {
        // Connect to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the dietary restrictions of the user
        db.collection("Users").document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String dietaryRestrictions = documentSnapshot.getString("Dietary Restrictions");
                    if (dietaryRestrictions == null || dietaryRestrictions.isEmpty()) {
                        callback.onCallback("No restrictions");
                    } else {
                        callback.onCallback(dietaryRestrictions);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("PartyMethods", "Error getting dietary restrictions: " + e.getMessage());
                    callback.onCallback("No restrictions"); // Handle failure case
                });
    }

    // Adding user to the party
    public void addUserToParty(String email, String PartyCode) {
        // Call method to check if user exists in the party
        checkUserInParty(email, PartyCode, new Callback() {
            @Override
            public void onUserChecked(boolean userExists) {
                if (!userExists) {
                    // Fetch dietary restrictions
                    getDietaryRestrictions(email, dietaryRestrictions -> {
                        // Prepare user data as a Map
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("price", 0);
                        userData.put("rating", 0);
                        userData.put("Distance", 0);
                        userData.put("Dietary Restrictions", dietaryRestrictions);
                        userData.put("Cuisine", Arrays.asList(new String[]{}));

                        // Add user to the party
                        db.collection("Parties").document(PartyCode).collection("Users").document(email)
                                .set(userData) // Use the Map here
                                .addOnSuccessListener(aVoid -> Log.d("PartyMethods", "User added to party"))
                                .addOnFailureListener(e -> Log.d("PartyMethods", "Error adding user to party: " + e.getMessage()));
                    });
                }
            }
        });
    }

    // Helper method to show a toast message
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}