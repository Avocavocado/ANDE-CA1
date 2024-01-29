package com.example.ande_munch.methods;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class RestaurantFilter {
    public Map<String, Object> filterRestaurants(Map<String, Map<String, Object>> restaurantMap, Map<String, Object> userMap) {
        Map<String, Object> filteredRestaurants = new HashMap<>();
        Log.d("USERMAP", "USERMAP" + userMap.toString());
        double userMaxPrice = new Double(userMap.get("averagePrice").toString());
        double userMinRating = new Double(userMap.get("averageRating").toString());
        Log.d("test", "test" + userMaxPrice + " " + userMinRating);
        for (Map.Entry<String, Map<String, Object>> entry : restaurantMap.entrySet()) {
            String restaurantName = entry.getKey();
            Map<String, Object> restaurantDetails = (Map<String, Object>) entry.getValue();

            double restaurantPrice = new Double(restaurantDetails.get("avgPrice").toString());
            double restaurantRating = new Double(restaurantDetails.get("avgRating").toString());
            Log.d("test", "test" + restaurantName + " " + restaurantPrice + " " + restaurantRating);

            if (restaurantPrice <= userMaxPrice) {
                // Exclude the "distance" attribute from restaurantDetails
                restaurantDetails.remove("distance");

                filteredRestaurants.put(restaurantName, restaurantDetails);
            }
        }

        return filteredRestaurants;
    }
}