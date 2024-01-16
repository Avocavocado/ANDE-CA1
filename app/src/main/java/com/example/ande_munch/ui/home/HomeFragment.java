package com.example.ande_munch.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.ande_munch.Restaurant;
import com.example.ande_munch.RestaurantCardAdapter;
import com.example.ande_munch.databinding.ExploreRestaurantsBinding;
import com.example.ande_munch.methods.LoginMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.recyclerview.widget.LinearLayoutManager;

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
    private List<Restaurant> restaurants = new ArrayList<Restaurant>();;
    private List<String> selectedCuisines = new ArrayList<>();
    private RestaurantCardAdapter rcAdapter;
    private EditText searchText;
    private String searchString = "";
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
                rcAdapter.updateData(filterRestaurants());
                //Log.i(TAG,"Text changed: " + searchString);
            }
        });

        // Initialize RecyclerView and Adapter
        recyclerView = binding.RestaurantCards;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        restaurants = new ArrayList<Restaurant>();
        db.collection("Restaurants")
                .get()
                .addOnCompleteListener(getRestaurantsTask -> {
                            if (getRestaurantsTask.isSuccessful()) {
                                for (DocumentSnapshot document : getRestaurantsTask.getResult()) {
                                    DocumentReference restaurantReference = document.getReference();
                                    CollectionReference menu = restaurantReference.collection("Menu");
                                    CollectionReference reviews = restaurantReference.collection("Reviews");

                                    Callback callback = new Callback() {
                                        @Override
                                        public void onSuccess(double avgPrice, double avgRating) {
                                            restaurants.add(new Restaurant(document, avgPrice, avgRating));
                                            rcAdapter.notifyItemInserted(restaurants.size() - 1);
                                        }
                                    };

                                    getMenuAndReviewData1(menu, reviews, callback);
                                }
                                rcAdapter = new RestaurantCardAdapter(getContext(), restaurants);
                                rcAdapter.setOnItemClickListener(new RestaurantCardAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(String rid) {
                                        Toast.makeText(requireContext(), "Item clicked: " + rid, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                RecyclerView resCards = root.findViewById(R.id.RestaurantCards);
                                LinearLayoutManager rcLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                                resCards.setLayoutManager(rcLayoutManager);
                                resCards.setAdapter(rcAdapter);
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
                rcAdapter.updateData(filterRestaurants());
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

    public List<Restaurant> filterRestaurants() {
        List<Restaurant> filteredDocuments = restaurants;

        //NO FILTERS
        if (selectedCuisines.size() == 0 && searchString.length() == 0 && priceFilter.equals("Any") && ratingFilter.equals("Any") && distanceFilter == 0) {
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
        if (!priceFilter.equals("Any")) {

        }

        //RATING FILTER
        if (!ratingFilter.equals("Any")) {

        }

        //DISTANCE FILTER
        if (distanceFilter != 0) {

        }
        Log.i(TAG, "SELECTED :" + selectedCuisines + " Documents No: " + filteredDocuments.size() + " Res. No: " + restaurants.size());
        return filteredDocuments;
    }

    public List<Restaurant> filterByName(List<Restaurant> restaurants) {
        List<Restaurant> filteredDocuments = new ArrayList<>();
        for (Restaurant document : restaurants) {
            String rName = document.data.getId().toLowerCase();
            String search = searchString.trim().replaceAll("\\s+", " ").toLowerCase();
            if (rName.contains(search)) {
                filteredDocuments.add(document);
            }
        }
        return filteredDocuments;
    }

    public List<Restaurant> filterByCuisine(List<Restaurant> restaurants) {
        List<Restaurant> filteredDocuments = new ArrayList<>();
        for (Restaurant document : restaurants) {
            for (String cuisine : (List<String>) document.data.get("Cuisine")) {
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

    public void getMenuAndReviewData1(CollectionReference menu, CollectionReference reviews, Callback callback) {
        menu.get().addOnCompleteListener(menuSnapshot -> {
            double totalPrice = 0;
            double dishCount = 0;

            for (QueryDocumentSnapshot dish : menuSnapshot.getResult()) {
                try {
                    //Log.i(TAG, "DISH: " + dish.get("Price"));
                    totalPrice += dish.getLong("Price").doubleValue();
                    dishCount++;
                } catch (Exception e) {
                    Log.i(TAG, dish.getId());
                }
            }

            double avgPrice = Math.round(totalPrice / dishCount * 100) / 100.0;
            //Log.i(TAG, "getAvgPrice() worked");
            getMenuAndReviewData2(reviews, avgPrice, callback);
        });
    }

    public void getMenuAndReviewData2(CollectionReference reviews, double avgPrice, Callback callback) {
        reviews.get().addOnCompleteListener(reviewsSnapshot -> {
            double totalScore = 0;
            double reviewCount = 0;
            for (QueryDocumentSnapshot review : reviewsSnapshot.getResult()) {
                try {
                    //Log.i(TAG, "RATING :" + review.get("Rating"));
                    totalScore += review.getLong("Rating").doubleValue();
                } catch (Exception e) {
                    Log.i(TAG, "RATING : [NO REVIEWS]");
                    totalScore = 0;
                    break;
                }
                reviewCount++;
            }
            double avgRating = Math.round(totalScore / reviewCount * 2) / 2.0;
            callback.onSuccess(avgPrice, avgRating);
        });
    }

    public interface Callback {
        void onSuccess(double result1, double result2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
