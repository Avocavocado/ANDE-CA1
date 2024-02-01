package com.example.ande_munch.methods;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.example.ande_munch.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class LocationMethods {
    MainActivity mainActivity = new MainActivity();
    private LocationManager locationManager;
    private Location currentLocation;
    private double userLatitude;
    private double userLongitude;
    private double restrauntLatitude;
    private double restrauntLongitude;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "LocationMethods";

    public void showDistance (Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        calculateDistances(getCurrentLocation(), getRestaurantLocations());
    }

    public HashMap<String, Double> getCurrentLocation() {
        // Assigning latitude and longitude of the user
        userLatitude = mainActivity.getLatitude();
        userLongitude = mainActivity.getLongitude();

        HashMap<String, Double> coordinates = new HashMap<>();
        coordinates.put("latitude", userLatitude);
        coordinates.put("longitude", userLongitude);

        return coordinates;
    }

    public HashMap<String, HashMap<String, Double>> getRestaurantLocations() {
        HashMap<String, HashMap<String, Double>> restaurantLocations = new HashMap<>();

        Task<QuerySnapshot> task = db.collection("Restaurants").get();
        if (task.isSuccessful()) {
            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                String restaurantName = document.getId(); // Get the restaurant name
                GeoPoint geoPoint = document.getGeoPoint("Location");

                if (geoPoint != null) {
                    HashMap<String, Double> restaurantLocation = new HashMap<>();
                    restaurantLocation.put("latitude", geoPoint.getLatitude());
                    restaurantLocation.put("longitude", geoPoint.getLongitude());
                    restaurantLocations.put(restaurantName, restaurantLocation);
                }
            }
        } else {
            Log.w(TAG, "Error getting restaurant locations", task.getException());
        }

        return restaurantLocations;
    }

    public HashMap<String, Double> calculateDistances(HashMap<String, Double> currentLocation, HashMap<String, HashMap<String, Double>> restaurantLocations) {
        HashMap<String, Double> distances = new HashMap<>();

        if (currentLocation != null && currentLocation.containsKey("latitude") && currentLocation.containsKey("longitude")) {
            double currentLat = currentLocation.get("latitude");
            double currentLon = currentLocation.get("longitude");

            for (Map.Entry<String, HashMap<String, Double>> entry : restaurantLocations.entrySet()) {
                String restaurantName = entry.getKey();
                HashMap<String, Double> restaurantLocation = entry.getValue();

                if (restaurantLocation != null && restaurantLocation.containsKey("latitude") && restaurantLocation.containsKey("longitude")) {
                    double restaurantLat = restaurantLocation.get("latitude");
                    double restaurantLon = restaurantLocation.get("longitude");

                    final int R = 6371; // Radius of the earth in kilometers
                    double latDistance = Math.toRadians(restaurantLat - currentLat);
                    double lonDistance = Math.toRadians(restaurantLon - currentLon);
                    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                            + Math.cos(Math.toRadians(currentLat)) * Math.cos(Math.toRadians(restaurantLat))
                            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    double distance = R * c * 1000; // Convert to meters

                    distances.put(restaurantName, distance);
                }
            }
        }

        return distances;
    }
}
