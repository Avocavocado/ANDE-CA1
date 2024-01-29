package com.example.ande_munch.methods;

import android.util.Log;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SwipeMethods {
    // Define a new callback
    public interface RestaurantFieldsCallback {
        void onRestaurantFieldsFetched(HashMap<String, Map<String, Object>> restaurantDetails);
    }

    public interface OpeningStatusCallback {
        void onStatusChecked(boolean isOpen);
    }

    // Define new attributes
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference RestaurantRef = db.collection("Restaurants");

    // Method to extract the restaurant details
    public void getRestaurantDetails(HashMap<String, HashMap<String, Double>> eachRestaurantData, RestaurantFieldsCallback callback) {
        ArrayList<String> restaurantNames = new ArrayList<>(eachRestaurantData.keySet());
        HashMap<String, Map<String, Object>> restaurantDetails = new HashMap<>();
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalRestaurants = restaurantNames.size();

        for (String restaurantName : restaurantNames) {
            RestaurantRef.document(restaurantName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            Map<String, Object> combinedData = new HashMap<>(task.getResult().getData());
                            HashMap<String, Double> restaurantData = eachRestaurantData.get(restaurantName);
                            if (restaurantData != null) {
                                combinedData.putAll(restaurantData);
                            }
                            restaurantDetails.put(restaurantName, combinedData);
                        } else {
                            Log.d("SwipeMethods", "Error retrieving data for " + restaurantName);
                        }
                        if (completedRequests.incrementAndGet() == totalRestaurants) {
                            callback.onRestaurantFieldsFetched(restaurantDetails);
                        }
                    });
        }
    }

    public void checkOpeningStatus(List<Map<String, String>> openingHours, OpeningStatusCallback callback) {
        // Declaring other variables
        boolean isOpen = false;

        // Getting the current time and day of the week
        LocalDate currentDate = LocalDate.now();
        DayOfWeek currentDay = currentDate.getDayOfWeek();
        LocalTime localTime = LocalTime.now();

        // Determine the day index (0 = Monday, 6 = Sunday)
        int dayIndex = currentDay.getValue() - 1;

        if (dayIndex < openingHours.size()) {
            Map<String, String> todayOpeningHour = openingHours.get(dayIndex);

            // Extracting the opening and closing times for today
            String openTime = todayOpeningHour.get("Open");
            String closeTime = todayOpeningHour.get("Close");

            // Using DateTimeFormatter to parse the time in H:mm format
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
            LocalTime openingTime = LocalTime.parse(openTime, timeFormatter);
            LocalTime closingTime = LocalTime.parse(closeTime, timeFormatter);

            isOpen = localTime.isAfter(openingTime) && localTime.isBefore(closingTime);
        }

        // Check if callback is not null before using it
        if (callback != null) {
            Log.d("SwipeMethods", "Callback is called");
            callback.onStatusChecked(isOpen);
        } else {
            Log.e("SwipeMethods", "OpeningStatusCallback is null");
        }
    }
}
