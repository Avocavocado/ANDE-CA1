package com.example.ande_munch.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.CuisineButtonAdapter;
import com.example.ande_munch.FilterActivity;
import com.example.ande_munch.R;
import com.example.ande_munch.RestaurantCardAdapter;
import com.example.ande_munch.databinding.ExploreRestaurantsBinding;
import com.example.ande_munch.methods.LoginMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "ExploreRestaurants";
    private @NonNull ExploreRestaurantsBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private List<DocumentSnapshot> restaurants;
    private List<String> selectedCuisines = new ArrayList<>();
    private RestaurantCardAdapter adapter;
    private LoginMethods loginMethods = new LoginMethods();

    private List<String> urls =
            Arrays.asList("bbq", "chinese", "fast_food", "hawker", "indian", "japanese", "mexican", "seafood", "thai", "western");
    private List<String> cuisines =
            Arrays.asList("BBQ", "Chinese", "Fast Food", "Hawker", "Indian", "Japanese", "Mexican", "Seafood", "Thai", "Western");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = ExploreRestaurantsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String email = loginMethods.getUserEmail();
        Log.d(TAG, "Email: " + email);
        CheckAndAddUser(email);

        // Initialize RecyclerView and Adapter
        recyclerView = binding.RestaurantCards;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        restaurants = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        db.collection("Restaurants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            restaurants.add(document);
                            Log.i(TAG, "Document: " + document);
                        }
                        // After data retrieval, set up the adapter
                        adapter = new RestaurantCardAdapter(getContext(), restaurants);
                        adapter.setOnItemClickListener(new RestaurantCardAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(String rid) {
                                Toast.makeText(requireContext(), "Item clicked: " + rid, Toast.LENGTH_SHORT).show();
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching documents", e));

        //Cuisine Buttons Layout
        RecyclerView cuisineBtns = root.findViewById(R.id.cuisineBtns);
        LinearLayoutManager cuisineLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        cuisineBtns.setLayoutManager(cuisineLayoutManager);
        CuisineButtonAdapter cbAdapter = new CuisineButtonAdapter(requireContext(), cuisines, urls);
        cbAdapter.setOnItemClickListener(new CuisineButtonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String cuisineName, View view) {
                Toast.makeText(requireContext(), "Item clicked: " + cuisineName, Toast.LENGTH_SHORT).show();
                if (selectedCuisines.contains(cuisineName)) {
                    selectedCuisines.remove(cuisineName);
                    view.setBackgroundResource(R.drawable.cuisine_button_bg);
                } else {
                    selectedCuisines.add(cuisineName);
                    view.setBackgroundResource(R.drawable.selected_cuisine_button_bg);
                }
                adapter.updateData(filterDocuments(selectedCuisines));
            }
        });
        cuisineBtns.setAdapter(cbAdapter);

        ImageButton filterBtn = root.findViewById(R.id.filterButton);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), FilterActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    public void CheckAndAddUser(String email) {
        db = FirebaseFirestore.getInstance();
        loginMethods.checkDbForEmail(email).thenAccept(userExists -> {
            if (!userExists) {
                Log.d(TAG, "User email doesn't exist");
                loginMethods.createUser(email);
            } else {
                Log.d(TAG, "User email exists");
            }
        }).exceptionally(e -> {
            Log.e(TAG, "Error checking user in database: " + e);
            return null;
        });
    }

    public List<DocumentSnapshot> filterDocuments(List<String> selectedCuisines) {
        List<DocumentSnapshot> filteredDocuments = new ArrayList<>();

        if (selectedCuisines.size() == 0) {
            return restaurants;
        }

        for (DocumentSnapshot document : restaurants) {
            for (String cuisine: (List<String>) document.get("Cuisine")) {
                if (selectedCuisines.contains(cuisine)) {
                    filteredDocuments.add(document);
                    break;
                }
            }
        }
        Log.i(TAG,"SELECTED :" + selectedCuisines + " Documents No: " + filteredDocuments.size() + " Res. No: " + restaurants.size());
        return filteredDocuments;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
