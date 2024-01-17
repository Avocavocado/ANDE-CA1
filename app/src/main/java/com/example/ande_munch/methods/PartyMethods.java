package com.example.ande_munch.methods;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.Random;

public class PartyMethods {
    // Initialize attributes
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 4;
    boolean partyCodeExists = false;

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

    // Adding user to the party
    public void addUserToParty (String partyCode, String email, Boolean partyExistStatus) {
        // Connect to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();


    }

    // Helper method to show a toast message
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}