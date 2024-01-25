package com.example.ande_munch.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ande_munch.classes.Dish;
import com.example.ande_munch.databinding.FragmentNotificationsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference dishesRef;

    private final Map<String, List<Dish>> cuisineDishesMap = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getAllDishes();

        return root;
    }

    private void getAllDishes() {
        dishesRef = db.collection("Dishes");

        // Get all the documents under "Dishes"
        dishesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String cuisine = document.getId();
                        Log.d("Firestore", "Cuisine: " + cuisine);
                        List<Dish> dishesList = new ArrayList<>();

                        // Get the sub-collection under each cuisine
                        CollectionReference dishesInCuisineRef = db.collection("Dishes").document(cuisine).collection("Dishes");

                        // Get all the documents under the sub-collection
                        dishesInCuisineRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@android.support.annotation.NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot dishDoc : task.getResult()) {
                                        String dishImage = dishDoc.getString("DishImage");
                                        String dishDescription = dishDoc.getString("Description");
                                        String dishName = dishDoc.getId();

                                        Dish dish = new Dish(dishName, cuisine, dishImage, dishDescription);

                                        // Add the dish to the list
                                        dishesList.add(dish);
                                    }
                                    cuisineDishesMap.put(cuisine, dishesList);
                                    Log.d("DishesMap", "Cuisine: " + cuisine + ", Dishes: " + dishesList);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}