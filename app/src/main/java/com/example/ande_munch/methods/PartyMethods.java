package com.example.ande_munch.methods;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class PartyMethods {
    // Initialize attributes
    boolean partyCodeExists = false;

    // Method to scan through all parties and check if code entered is one of them
    public void checkPartyCode(Context context, String partyCode, OnSuccessListener<Boolean> onSuccessListener, OnFailureListener onFailureListener) {
        // Connect to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Scan through all parties
        db.collection("Parties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Scan through all parties
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if the party code matches
                        if (document.getId().equals(partyCode)) {
                            partyCodeExists = true;
                            onSuccessListener.onSuccess(partyCodeExists);
                            showToast(context, "Party found!");
                            return;
                        }
                    }
                    // If the loop completes without finding the code
                    onSuccessListener.onSuccess(false);
                    showToast(context, "Party not found.");
                })
                .addOnFailureListener(e -> {
                    // Log error message
                    Log.d("PartyMethods", "Error getting documents: " + e.getMessage());
                    onFailureListener.onFailure(e);
                    showToast(context, "Error: " + e.getMessage());
                });
    }

    // Helper method to show a toast message
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}