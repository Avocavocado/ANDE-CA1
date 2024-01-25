package com.example.ande_munch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ande_munch.methods.Callback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DishPage extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference dishesRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dishes);

        getAllDishes();
    }

    private void getAllDishes() {
        dishesRef = db.collection("Dishes");

        // Get all the documents under "Dishes"
        dishesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String cuisine = document.getId();
                        Log.d("Firestore", "Cuisine: " + cuisine);

                        // Get the sub-collection under each cuisine
                        CollectionReference dishesInCuisineRef = db.collection("Dishes").document(cuisine).collection("Dishes");

                        // Get all the documents under the sub-collection
                        dishesInCuisineRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot dishDoc : task.getResult()) {
                                        String dishImage = dishDoc.getString("DishImage");
                                        String dishName = document.getId();
                                        Log.d("Firestore", "Dish Image: " + dishImage);
                                        Log.d("Firestore", "Dish Name: " + dishName);
                                    }
                                } else {
                                    Log.d("Firestore", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    }
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });
    }
}
