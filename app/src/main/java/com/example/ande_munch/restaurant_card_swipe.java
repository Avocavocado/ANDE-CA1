package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
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
    List<Map<String, String>> openingHours;
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
        // Original average rating
        double averageRating = restaurantInfo.getAverageRating();
        double roundedRating = Math.round(averageRating * 2) / 2.0;

        // Update the UI with the new object
        Picasso.get().load(restaurantInfo.getRestaurantImage()).into(RestaurantImage);
        RestaurantName.setText(restaurantInfo.getName());
        RestaurantDesc.setText(restaurantInfo.getDescription());
        RestaurantRating.setText(String.valueOf(roundedRating));
        HashMap<String, String> openText = getOpenText();
        openInfo.setText(openText.get("text"));
        openInfo.setBackgroundColor(Color.parseColor(openText.get("color")));
        cuisines.removeAllViews();
        for (String cuisine: (List<String>) restaurantInfo.getCuisines()) {
            TextView textView = new TextView(this);
            textView.setText(cuisine);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            int dp = (int) (0.5f * getResources().getDisplayMetrics().density);
            layoutParams.setMargins(0, 0, 24*dp, 0);
            textView.setPadding(26*dp, 8*dp, 26*dp, 8*dp);
            textView.setBackgroundColor(Color.parseColor("#D7DAFF"));
            textView.setTextColor(Color.parseColor("#53555C"));
            textView.setLayoutParams(layoutParams);

            cuisines.addView(textView);

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
        openingHours = (List<Map<String, String>>) restaurantData.get("OpeningHours");

        return new RestaurantInfo(restaurantName, description, restaurantImage, cuisine, openingHours, isOpen, averageRating);
    }


    private HashMap<String, String> getOpenText() {
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue() - 1;
        Map<String,String> todayOH = (Map<String, String>) openingHours.get(dayOfWeek);
        LocalTime now = LocalTime.now();
        LocalTime openingTime = LocalTime.parse(todayOH.get("Open"), DateTimeFormatter.ofPattern("H:mm"));
        LocalTime closingTime = LocalTime.parse(todayOH.get("Close"), DateTimeFormatter.ofPattern("H:mm"));

        boolean hasOpened = !now.isBefore(openingTime);
        boolean hasClosed = !now.isBefore(closingTime);

        if (hasOpened && !hasClosed) {
            return new HashMap<String, String>(){
                {{
                    put("color", "#00FF66");
                    put("text", "Open til " + closingTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
                }};
            };
        }
        else if (!hasOpened && !hasClosed){
            return new HashMap<String, String>(){
                {{
                    put("color", "#30303A");
                    put("text", "Opens at " + openingTime.format(DateTimeFormatter.ofPattern("hh:mm a")));;
                }};
            };
        }
        else {
            Map<String,String> tomorrowOH = (Map<String, String>) openingHours.get(dayOfWeek != 6 ? dayOfWeek+1 : 0);
            LocalTime tomorrowOpeningTime = LocalTime.parse(tomorrowOH.get("Open"), DateTimeFormatter.ofPattern("H:mm"));
            return new HashMap<String, String>(){
                {{
                    put("color", "#30303A");
                    put("text", "Closed til " + tomorrowOpeningTime.format(DateTimeFormatter.ofPattern("hh:mm a")));;
                }};
            };
        }
    }
}
