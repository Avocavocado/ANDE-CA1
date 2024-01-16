package com.example.ande_munch.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "ExploreRestaurants";
    private @NonNull ExploreRestaurantsBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private List<DocumentSnapshot> restaurants;
    private List<String> selectedCuisines = new ArrayList<>();
    private RestaurantCardAdapter adapter;
    private EditText searchText;
    private String searchString;
    private String priceFilter = "Any";
    private String ratingFilter = "Any";
    private int distanceFilter = 0;
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

        searchText = (EditText) root.findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchString = editable.toString();
                adapter.updateData(filterRestaurants());
                //Log.i(TAG,"Text changed: " + searchString);
            }
        });

        // Initialize RecyclerView and Adapter
        recyclerView = binding.RestaurantCards;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        restaurants = new ArrayList<>();
        db.collection("Restaurants")
                .get()
                .addOnCompleteListener(getRestaurantsTask -> {
                    if (getRestaurantsTask.isSuccessful()) {
                        for (DocumentSnapshot document : getRestaurantsTask.getResult()) {
                            DocumentReference restaurantReference = document.getReference();
                            restaurantReference.collection("Menu")
                                    .get()
                                    .addOnCompleteListener(getMenuTask -> {
                                        if (getMenuTask.isSuccessful()) {
                                            double totalPrice = 0;
                                            double dishCount = 0;
                                            for (DocumentSnapshot dish : getMenuTask.getResult()) {
                                                totalPrice += dish.getLong("Price");
                                                dishCount++;
                                            }
                                            document.getData().put("avgPrice", new DecimalFormat("#.##").format(totalPrice/dishCount));
                                        }
                                    });
                            restaurantReference.collection("Reviews")
                                    .get()
                                    .addOnCompleteListener(getReviewsTask -> {
                                        if (getReviewsTask.isSuccessful()) {
                                            double totalScore = 0;
                                            double reviewCount = 0;
                                            for (DocumentSnapshot review : getReviewsTask.getResult()) {
                                                totalScore += review.getLong("Rating");
                                                reviewCount++;
                                            }
                                            document.getData().put("avgRating", Math.round(totalScore / reviewCount * 2.0) / 2.0);
                                        }
                                    });
                            restaurants.add(document);
                            Log.i(TAG, "Document: " + document + " " + document.getDouble("avgPrice") + " " + document.getDouble("avgRating"));
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
                        Log.w(TAG, "Error getting documents: ", getRestaurantsTask.getException());
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
                adapter.updateData(filterRestaurants());
            }
        });
        cuisineBtns.setAdapter(cbAdapter);

        ImageButton filterBtn = root.findViewById(R.id.filterButton);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), FilterActivity.class);
                getFilters.launch(intent);
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

    public List<DocumentSnapshot> filterRestaurants() {
        List<DocumentSnapshot> filteredDocuments = restaurants;

        //NO FILTERS
        if (selectedCuisines.size() == 0 && searchString.length() == 0 && priceFilter == "Any" && ratingFilter == "Any" && distanceFilter == 0) {
            return restaurants;
        }

        //NAME FILTER
        if (searchString.length() > 0) {
            filteredDocuments = filterByName(filteredDocuments);
        }

        //CUISINE FILTER
        if (selectedCuisines.size() > 0) {
            filteredDocuments = filterByCuisine(filteredDocuments);
        }

        //PRICE FILTER
        if (priceFilter != "Any") {

        }

        //RATING FILTER
        if (ratingFilter != "Any") {

        }

        //DISTANCE FILTER
        if (distanceFilter != 0) {

        }
        Log.i(TAG, "SELECTED :" + selectedCuisines + " Documents No: " + filteredDocuments.size() + " Res. No: " + restaurants.size());
        return filteredDocuments;
    }

    public List<DocumentSnapshot> filterByName(List<DocumentSnapshot> restaurants) {
        List<DocumentSnapshot> filteredDocuments = new ArrayList<>();
        for (DocumentSnapshot document : restaurants) {
            String rName = document.getId().toLowerCase();
            String search = searchString.trim().replaceAll("\\s+", " ").toLowerCase();
            if (rName.contains(search)) {
                filteredDocuments.add(document);
            }
        }
        return filteredDocuments;
    }

    public List<DocumentSnapshot> filterByCuisine(List<DocumentSnapshot> restaurants) {
        List<DocumentSnapshot> filteredDocuments = new ArrayList<>();
        for (DocumentSnapshot document : restaurants) {
            for (String cuisine : (List<String>) document.get("Cuisine")) {
                if (selectedCuisines.contains(cuisine)) {
                    filteredDocuments.add(document);
                    break;
                }
            }
        }
        return filteredDocuments;
    }

    private final ActivityResultLauncher<Intent> getFilters = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    priceFilter = data.getStringExtra("Price");
                    ratingFilter = data.getStringExtra("Rating");
                    distanceFilter = data.getIntExtra("Distance", 0);
                    Log.i(TAG, "FILTERS RECEIVED: " + priceFilter + " " + ratingFilter + " " + distanceFilter);
                }
            }
    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
