package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DisplayParty extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitvity_display_party);

        Button buttonFilterPage = findViewById(R.id.partyFilterBtn);
        buttonFilterPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayParty.this, PartyFilter.class);
                startActivity(intent);
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Parties")
                .get()
                .addOnSuccessListener(result -> {
                    for (DocumentSnapshot partyDocument : result.getDocuments()) {
                        // Access a subcollection within the document
                        CollectionReference subCollectionRef = partyDocument.getReference().collection("SubCollectionName");

                        subCollectionRef.get()
                                .addOnSuccessListener(subResult -> {
                                    for (DocumentSnapshot subDocument : subResult.getDocuments()) {
                                        // Process each document in the subcollection
//                                        Log.i(TAG, subDocument.getId() + " => " + subDocument.getData());
                                    }
                                })
                                .addOnFailureListener(subException -> {
//                                    Log.i(TAG, "Error getting subcollection documents." + subException);
                                });
                    }
                })
                .addOnFailureListener(exception -> {
//                    Log.i(TAG, "Error getting documents." + exception);
                });

    }

}
