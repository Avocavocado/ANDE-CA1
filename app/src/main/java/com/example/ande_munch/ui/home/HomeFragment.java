package com.example.ande_munch.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.Manifest;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.ChatBotActivity;
import com.example.ande_munch.CuisineButtonAdapter;
import com.example.ande_munch.FilterActivity;
import com.example.ande_munch.MainActivity;
import com.example.ande_munch.ProfilePage;
import com.example.ande_munch.R;
import com.example.ande_munch.classes.LocationTracker;
import com.example.ande_munch.classes.Restaurant;
import com.example.ande_munch.RestaurantCardAdapter;
import com.example.ande_munch.RestaurantDetails;
import com.example.ande_munch.databinding.ExploreRestaurantsBinding;
import com.example.ande_munch.methods.Callback;
import com.example.ande_munch.methods.LoginMethods;
import com.example.ande_munch.methods.ProfileMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private ImageView profileImageView;
    private ProfileMethods profileMethods = new ProfileMethods();
    private LoginMethods loginMethods = new LoginMethods();
    private MainActivity mainActivity;
    private List<String> urls =
            Arrays.asList("bbq", "chinese", "fast_food", "hawker", "indian", "japanese", "malay", "mexican", "seafood", "thai", "western");
    private List<String> cuisines =
            Arrays.asList("BBQ", "Chinese", "Fast Food", "Hawker", "Indian", "Japanese", "Malay", "Mexican", "Seafood", "Thai", "Western");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = ExploreRestaurantsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String email = loginMethods.getUserEmail();
        mainActivity = (MainActivity) getActivity();

        profileImageView = binding.getRoot().findViewById(R.id.profileImageView);

        profileMethods.getUserProfileImage(new com.example.ande_munch.methods.Callback() {
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
                                    GeoPoint geopoint = document.getGeoPoint("Location");
                                    Log.i("GPS","HomeFrag: " + mainActivity.getLatitude() + " " + mainActivity.getLongitude() + " " + geopoint.getLatitude() +  " " + geopoint.getLongitude());
                                    Log.i("GPS", "Distance: " + getDistance(mainActivity.getLatitude(), mainActivity.getLongitude(), geopoint.getLatitude(), geopoint.getLongitude()));
                                    double distance = getDistance(mainActivity.getLatitude(), mainActivity.getLongitude(), geopoint.getLatitude(), geopoint.getLongitude());
                                    restaurants.add(new Restaurant(document, avgPrice, avgRating, distance));
                                    rcAdapter.notifyItemInserted(restaurants.size() - 1);
                                }
                            };

                            getMenuAndReviewData1(menu, reviews, callback);
                        }
                        rcAdapter = new RestaurantCardAdapter(getContext(), restaurants);
                        rcAdapter.setOnItemClickListener(new RestaurantCardAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Restaurant restaurant) {
                                Intent intent = new Intent(requireActivity(), RestaurantDetails.class);
                                GeoPoint geopoint = restaurant.data.getGeoPoint("Location");
                                intent.putExtra("Lat", geopoint.getLatitude());
                                intent.putExtra("Lon", geopoint.getLongitude());
                                intent.putExtra("RestaurantId", restaurant.data.getId());
                                intent.putExtra("AvgPrice", restaurant.avgPrice);
                                intent.putExtra("Distance", restaurant.distance);
                                intent.putExtra("AvgRating", restaurant.avgRating);
                                intent.putExtra("Address", restaurant.data.getString("Address"));
                                intent.putExtra("Desc", restaurant.data.getString("Desc"));
                                intent.putStringArrayListExtra("Cuisine",  (ArrayList<String>) restaurant.data.get("Cuisine"));
                                intent.putExtra("OpeningHours", (Serializable) restaurant.data.get("OpeningHours"));
                                intent.putExtra("Image", restaurant.data.getString("RestaurantImage"));
                                startActivity(intent);
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
            public void onItemClick(View view, TextView cuisine) {
                String cuisineName = cuisine.getText().toString();
                Toast.makeText(requireContext(), "Item clicked: " + cuisineName, Toast.LENGTH_SHORT).show();
                if (selectedCuisines.contains(cuisineName)) {
                    selectedCuisines.remove(cuisineName);
                    cuisine.setTextColor(ContextCompat.getColor(requireContext(), R.color.almostBlack));
                    view.setBackgroundResource(R.drawable.cuisine_button_bg);
                } else {
                    selectedCuisines.add(cuisineName);
                    cuisine.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
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

        ImageButton startBot = root.findViewById(R.id.startBot);
        startBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), ChatBotActivity.class);

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(requireContext(), Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(mainActivity.getLatitude(), mainActivity.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) { throw new RuntimeException(e); }

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();

                intent.putExtra("Location", country + ", " + city + ", " + address);
                startActivity(intent);
            }
        });
        return root;
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
            filteredDocuments = filterByPrice(filteredDocuments);
        }

        //RATING FILTER
        if (!ratingFilter.equals("Any")) {
            filteredDocuments = filterByRating(filteredDocuments);
        }

        //DISTANCE FILTER
        if (distanceFilter != 0) {
            filteredDocuments = filterByDistance(filteredDocuments);
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

    public List<Restaurant> filterByRating(List<Restaurant> restaurants) {
        List<Restaurant> filteredDocuments = new ArrayList<>();
        for (Restaurant document : restaurants) {
            double ratingValue = Double.parseDouble(ratingFilter.substring(0, ratingFilter.length() - 1));
            if (document.avgRating >= ratingValue) {
                filteredDocuments.add(document);
            }
        }
        return filteredDocuments;
    }

    public List<Restaurant> filterByPrice(List<Restaurant> restaurants) {
        List<Restaurant> filteredDocuments = new ArrayList<>();
        for (Restaurant document : restaurants) {
            int priceValue = Integer.parseInt(priceFilter.substring(1));
            if (document.avgPrice <= priceValue) {
                filteredDocuments.add(document);
            }
        }
        return filteredDocuments;
    }

    public List<Restaurant> filterByDistance(List<Restaurant> restaurants) {
        List<Restaurant> filteredDocuments = new ArrayList<>();
        for (Restaurant document : restaurants) {
            double maxDistance = (double) distanceFilter / 10;
            if (document.distance <= maxDistance) {
                filteredDocuments.add(document);
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
                    rcAdapter.updateData(filterRestaurants());
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
                    totalPrice += dish.getDouble("Price");
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
                    Log.i(TAG, "RATING :" + review.get("Rating"));
                    totalScore += review.getDouble("Rating");
                    reviewCount++;
                } catch (Exception e) {}
            }
            double avgRating = Math.round(totalScore / reviewCount * 2) / 2.0;
            callback.onSuccess(avgPrice, avgRating);
        });
    }

    public interface Callback {
        void onSuccess(double result1, double result2);
    }

    public double getDistance (double lat1, double lon1, double lat2, double lon2) {
        int r = 6371; // km
        double p = Math.PI / 180;
        double a = 0.5 - Math.cos((lat2 - lat1) * p) / 2
                + Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                (1 - Math.cos((lon2 - lon1) * p)) / 2;
        return (double) Math.round(10 * 2 * r * Math.asin(Math.sqrt(a))) / 10;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }
}
