package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ande_munch.classes.Restaurant;
import com.example.ande_munch.classes.RestaurantInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class restaurant_card_swipe extends AppCompatActivity {
    // Define new attributes
    Button StartBtn, LikeBtn, DislikeBtn;

    ArrayList<String> restaurantNames = new ArrayList<>();
    ArrayList<Map<String, Object>> restaurantDetailsList = new ArrayList<>();

    // Define new attributes
    private boolean isOpen;
    private ImageView RestaurantImage;
    private TextView RestaurantName;
    private TextView RestaurantDesc;
    private TextView RestaurantRating;
    private TextView openInfo;
    private LinearLayout cuisines;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference RestaurantRef = db.collection("Restaurants");
    private ArrayList<HashMap<String, HashMap<String, Double>>> filteredRestaurantNames = new ArrayList<>();
    private HashMap<String, Map<String, Object>> restaurantDetails = new HashMap<>();
    private ArrayList<RestaurantInfo> restaurantInfoList = new ArrayList<>();
    private int currentIndex = 0, countLike = 0, countDislike = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_page);

        // Initialise the attributes
        RestaurantImage = findViewById(R.id.RestaurantImage);
        RestaurantName = findViewById(R.id.RestaurantName);
        RestaurantDesc = findViewById(R.id.RestaurantDesc);
        RestaurantRating = findViewById(R.id.restaurantRating);
        openInfo = findViewById(R.id.OpenInfo);
        cuisines = findViewById(R.id.Cuisines);

        LikeBtn = findViewById(R.id.like);
        LikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countLike++ ;

                if (countLike%2 == 0 && countLike!= 0){
                    Toast.makeText(restaurant_card_swipe.this, "You have Liked this ;)", Toast.LENGTH_SHORT).show();

                    // Logic for processing liked restaurant
                }

                onNextButtonClick();
            }
        });

        DislikeBtn = findViewById(R.id.dislike);
        DislikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDislike++ ;

                if (countDislike%2 == 0 && countDislike!= 0){
                    Toast.makeText(restaurant_card_swipe.this, "You have Disliked this ;(", Toast.LENGTH_SHORT).show();

                    // Logic for processing disliked restaurant
                }

                onNextButtonClick();
            }
        });

        StartBtn = findViewById(R.id.scuffedBtn);
        StartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                if (intent != null && intent.hasExtra("filteredRestaurantNames")) {
                    LikeBtn.setVisibility(View.VISIBLE);
                    DislikeBtn.setVisibility(View.VISIBLE);
                    StartBtn.setVisibility(View.GONE);
                    filteredRestaurantNames = (ArrayList<HashMap<String, HashMap<String, Double>>>) intent.getSerializableExtra("filteredRestaurantNames");
                    getRestaurantDetails(filteredRestaurantNames);
                }
            }
        });
    }

    private void onNextButtonClick() {
        if (currentIndex < restaurantInfoList.size()) {
            RestaurantInfo currentRestaurantInfo = restaurantInfoList.get(currentIndex);
            updateUI(currentRestaurantInfo);
            currentIndex++;
        } else {
            Log.d("SwipeMethods", "No more data available");
        }
    }

    private void updateUI(RestaurantInfo restaurantInfo) {
        // Update the UI with the new object
        Picasso.get().load(restaurantInfo.getRestaurantImage()).into(RestaurantImage);
        RestaurantName.setText(restaurantInfo.getName());
        RestaurantDesc.setText(restaurantInfo.getDescription());
        RestaurantRating.setText(String.valueOf(restaurantInfo.getAverageRating()));
        openInfo.setText(restaurantInfo.isOpen() ? "Open" : "Closed");
        for (String cuisine : restaurantInfo.getCuisines()) {
            TextView cuisineView = new TextView(this);
            cuisineView.setText(cuisine);
            cuisines.addView(cuisineView);
        }
    }

    // Get the restaurant details from the database
    private void getRestaurantDetails(ArrayList<HashMap<String, HashMap<String, Double>>> filteredRestaurantNames) {
        // Declaring new attributes
        HashMap<String, HashMap<String, Double>> eachRestaurantData = new HashMap<>();

        if (filteredRestaurantNames != null) {
            for (HashMap<String, HashMap<String, Double>> restaurant : filteredRestaurantNames) {
                for (Map.Entry<String, HashMap<String, Double>> entry : restaurant.entrySet()) {
                    String restaurantName = entry.getKey();
                    eachRestaurantData.put(restaurantName, entry.getValue());
                    restaurantNames.add(restaurantName);
                }
            }
        }

        AtomicInteger counter = new AtomicInteger(0);

        for (String restaurantName : restaurantNames) {
            Log.d("SwipeMethods", "restaurantName: " + restaurantName);
            RestaurantRef.document(restaurantName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            Map<String, Object> combinedData = new HashMap<>(task.getResult().getData());
                            HashMap<String, Double> restaurantData = eachRestaurantData.get(restaurantName);
                            if (restaurantData != null) {
                                combinedData.putAll(restaurantData);
                                // Add restaurantName to the combinedData map
                                combinedData.put("restaurantName", restaurantName);
                                Log.d("SwipeMethods", "combinedData: " + combinedData);
                            }
                            restaurantDetailsList.add(combinedData);

                            if (counter.incrementAndGet() == restaurantNames.size()) {
                                populateCard();
                            }
                        }
                    });
        }
    }

    // Clear the previous object and load up the new object
    // Update the UI with the new object
    private void populateCard() {
        if (!restaurantDetailsList.isEmpty()) {
            // Clear the existing restaurantInfoList
            restaurantInfoList.clear();

            // Populate restaurantInfoList with all data
            for (int i = 0; i < restaurantDetailsList.size(); i++) {
                restaurantInfoList.add(createRestaurantInfo(i));
            }

            // Show the first entry
            updateUI(restaurantInfoList.get(0));
        } else {
            Log.d("SwipeMethods", "No restaurant data available");
        }
    }

    // Creating of the object with all the attributes
    private RestaurantInfo createRestaurantInfo(int index) {
        Map<String, Object> restaurantData = restaurantDetailsList.get(index);
        String restaurantName = (String) restaurantData.get("restaurantName");
        String restaurantImage = (String) restaurantData.get("RestaurantImage");
        String description = (String) restaurantData.get("Desc");
        List<String> cuisine = (List<String>) restaurantData.get("Cuisine");
        String address = (String) restaurantData.get("Address");
        Double averageRating = (Double) restaurantData.get("averageRating");
        Double averagePrice = (Double) restaurantData.get("averagePrice");
        List<Map<String, String>> openingHours = (List<Map<String, String>>) restaurantData.get("OpeningHours");
        boolean isOpen = calculateIsOpen(openingHours);

        return new RestaurantInfo(restaurantName, description, restaurantImage, cuisine, openingHours, isOpen, averageRating);
    }


    private boolean calculateIsOpen(List<Map<String, String>> openingHours) {
        LocalDate currentDate = LocalDate.now();
        DayOfWeek currentDay = currentDate.getDayOfWeek();
        LocalTime localTime = LocalTime.now();

        int dayIndex = currentDay.getValue() - 1;

        if (dayIndex >= 0 && dayIndex < openingHours.size()) {
            Map<String, String> todayOpeningHour = openingHours.get(dayIndex);
            String openTime = todayOpeningHour.get("Open");
            String closeTime = todayOpeningHour.get("Close");

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
            LocalTime openingTime = LocalTime.parse(openTime, timeFormatter);
            LocalTime closingTime = LocalTime.parse(closeTime, timeFormatter);

            return localTime.isAfter(openingTime) && localTime.isBefore(closingTime);
        } else {
            Log.d("SwipeMethods", "Invalid day index for calculating opening hours");
            return false;
        }
    }
}
