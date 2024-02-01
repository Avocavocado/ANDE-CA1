package com.example.ande_munch.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.DishAdapter;
import com.example.ande_munch.ProfilePage;
import com.example.ande_munch.R;
import com.example.ande_munch.classes.Dish;
import com.example.ande_munch.databinding.FragmentNotificationsBinding;
import com.example.ande_munch.methods.Callback;
import com.example.ande_munch.methods.LoginMethods;
import com.example.ande_munch.methods.ProfileMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewCuisines;

    private final Map<String, List<Dish>> cuisineDishesMap = new HashMap<>();
    private RecyclerView recyclerViewDishes1;
    private RecyclerView recyclerViewDishes2;
    private RecyclerView recyclerViewDishes3;
    ImageView profileImageView;
    LoginMethods loginMethods = new LoginMethods();
    ProfileMethods profileMethods = new ProfileMethods();
    String loggedInEmail = loginMethods.getUserEmail();
    Object DishesUnlocked;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileImageView = binding.getRoot().findViewById(R.id.profileImageView);

        profileMethods.getUserProfileImage(new Callback() {
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
                    Log.d("NotificationsFragment", "Profile image URL is null or empty");
                }
            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfilePage.class);
                startActivity(intent);
            }
        });

        recyclerViewDishes1 = binding.recyclerViewDishes1;
        recyclerViewDishes2 = binding.recyclerViewDishes2;
        recyclerViewDishes3 = binding.recyclerViewDishes3;

        // Set up the RecyclerViews with GridLayoutManager but don't set the adapters yet
        recyclerViewDishes1.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerViewDishes2.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerViewDishes3.setLayoutManager(new GridLayoutManager(getContext(), 3));

        getAllDishes();
        getUserProfileDishesUnlocked();

        return root;
    }

    private void getAllDishes() {
        CollectionReference dishesRef = db.collection("Dishes");

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
                                    Log.i("DishesMap", "Cuisine: " + cuisine + ", Dishes: " + dishesList);
                                    switch (cuisine) {
                                        case "Western":
                                            updateRecyclerView(recyclerViewDishes1, dishesList, cuisine);
                                            break;
                                        case "Mexican":
                                            updateRecyclerView(recyclerViewDishes2, dishesList, cuisine);
                                            break;
                                        case "Chinese":
                                            updateRecyclerView(recyclerViewDishes3, dishesList, cuisine);
                                            break;
                                    }
                                } else {
                                    Log.i("Firestore", "Error getting documents: ", task.getException());
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

    private void updateRecyclerView(RecyclerView recyclerView, List<Dish> dishes, String cuisine) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                getActivity().runOnUiThread(() -> {
                    if (recyclerView == recyclerViewDishes1) {
                        binding.CuisineTxt1.setText(cuisine);
                    } else if (recyclerView == recyclerViewDishes2) {
                        binding.CuisineTxt2.setText(cuisine);
                    } else if (recyclerView == recyclerViewDishes3) {
                        binding.CuisineTxt3.setText(cuisine);
                    }
                    DishAdapter adapter = new DishAdapter(dishes, DishesUnlocked);
                    recyclerView.setAdapter(adapter);
                });
            });
        }
    }

    public void getUserProfileDishesUnlocked() {
        db.collection("Users").document(loggedInEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DishesUnlocked = documentSnapshot.get("Dishes");
                        if (DishesUnlocked != null) {
                            Log.d("getUserDishesDetails", "Got Dishes Unlocked: " + DishesUnlocked);
                        } else {
                            Log.d("getUserDishesDetails", "Field does not exist");
                        }
                    } else {
                        Log.d("getUserDishesDetails", "No such document");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("getUserDishesDetails", "Error getting user details dishes: " + e.getMessage());
                });
    }
    @Override
    public void onResume() {
        super.onResume();
        getAllDishes();
        getUserProfileDishesUnlocked();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}