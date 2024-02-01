package com.example.ande_munch.methods;

import android.content.Context;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ande_munch.classes.LocationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.ande_munch.methods.Callback;

public class PartyMethods {
    // Initialize attributes
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 4;
    boolean partyCodeExists = false;
    String globalUsername;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface UserFilterDetailsCallback {
        void onUserFilterDetailsResult(HashMap<String, Object> resultMap);
    }

    public interface GeoPointCallback {
        void onGeoPointReceived(GeoPoint geoPoint);
    }

    public interface FilterResultsCallback {
        void onFilterResults(ArrayList<HashMap<String, HashMap<String, Double>>> tfilteredRestaurantNames);
    }

    // Method to scan through all parties and check if code entered is one of them
    public void checkPartyCode(Context context, String partyCode, OnSuccessListener<Boolean> onSuccessListener, OnFailureListener onFailureListener) {
        // Connect to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Scan through all parties
        db.collection("Parties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("PartyMethods", "The party code is: " + partyCode);
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d("PartyMethods", "The document is: " + document.getId());
                        if (document.getId().equals(partyCode)) {
                            boolean partyCodeExists = true;
                            onSuccessListener.onSuccess(partyCodeExists);
                            showToast(context, "Party found!");
                            return;
                        }
                    }
                    onSuccessListener.onSuccess(false);
                    showToast(context, "The Party was not found.");
                })
                .addOnFailureListener(e -> {
                    Log.d("PartyMethods", "Error getting documents: " + e.getMessage());
                    onFailureListener.onFailure(e);
                    showToast(context, "Error: " + e.getMessage());
                });
    }

    public String PartyCodeGenerator() {
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            codeBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        String partyCode = codeBuilder.toString();
        System.out.println("The party code is: " + partyCode);

        return partyCode;
    }

    // Check if the current user exists in the party
    public void checkUserInParty(String email, String PartyCode, Callback callback) {
        // Connect to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Parties").document(PartyCode).collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean userExists = false;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.getId().equals(email)) {
                            userExists = true;
                            break;
                        }
                    }
                    callback.onUserChecked(userExists);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    callback.onUserChecked(false);
                });
    }

    // Method to get each party member's dietary restrictions
    public void getDietaryRestrictions(String email, DietaryRestrictionsCallback callback) {
        // Connect to Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the dietary restrictions of the user
        db.collection("Users").document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String dietaryRestrictions = documentSnapshot.getString("Dietary Restrictions");
                    if (dietaryRestrictions == null || dietaryRestrictions.isEmpty()) {
                        callback.onCallback("No restrictions");
                    } else {
                        callback.onCallback(dietaryRestrictions);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("PartyMethods", "Error getting dietary restrictions: " + e.getMessage());
                    callback.onCallback("No restrictions"); // Handle failure case
                });
    }

    // Adding user to the party
    public void addUserToParty(String email, String PartyCode) {
        Log.i("THAD", "HERE2 WITH EMAIL: " + email + " AND PARTYCODE: " + PartyCode);

        // If the user exist
        if (email != null) {
            // Retrieve user details
            db.collection("Users")
                    .document(email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Accessing username field
                                    globalUsername = document.getString("Username");
                                    Log.d("PartyMethods", "Username is " + globalUsername);
                                } else {
                                    Log.d("PartyMethods", "No such document");
                                }
                            } else {
                                Log.d("PartyMethods", "get failed with ", task.getException());
                            }
                        }
                    });
        } else {
            Log.d("PartyMethods", "Email is null");
        }

        // Call method to check if user exists in the party
        checkUserInParty(email, PartyCode, new Callback() {
            @Override
            public void onUserChecked(boolean userExists) {
                Log.d("PartyMethods", "User exists: " + userExists);

                if (!userExists) {
                    Log.d("PartyMethods", "User does not exist in party");
                    // Fetch dietary restrictions
                    getDietaryRestrictions(email, dietaryRestrictions -> {
                        // Prepare user data as a Map
                        Map<String, Object> userData = new HashMap<>();

                        if (globalUsername != null) {
                            Log.d("PartyMethods", "Username is " + globalUsername);
                            userData.put("Username", globalUsername);
                        } else {
                            Log.d("PartyMethods", "Username is null");
                            userData.put("Username", "Example User");
                        }

                        userData.put("price", 0);
                        userData.put("rating", 0);
                        userData.put("Distance", 0);
                        userData.put("Dietary Restrictions", dietaryRestrictions);
                        userData.put("Cuisine", Arrays.asList(new String[]{}));
                        userData.put("IsLeader", false);

                        // Add user to the party
                        db.collection("Parties").document(PartyCode).collection("Users").document(email)
                                .set(userData) // Use the Map here
                                .addOnSuccessListener(aVoid -> Log.d("PartyMethods", "User added to party"))
                                .addOnFailureListener(e -> Log.d("PartyMethods", "Error adding user to party: " + e.getMessage()));
                    });
                } else {
                    Log.d("PartyMethods", "User already exists in party");
                }
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

            }
        });
    }

//    public void getUserFilterDetails(String PartyCode, Callback callback) {
//        db.collection("Parties").document(PartyCode).collection("Users")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<Map<String, Object>> usersList = new ArrayList<>();
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        Map<String, Object> userDetails = document.getData();
//                        userDetails.put("email", document.getId()); // Include the email in the userDetails map
//                        usersList.add(userDetails);
//                    }
//                    Log.d("getUserFilter", "Got User Details: " + usersList);
//                    callback.onUserDataFetched(usersList);
//                })
//                .addOnFailureListener(e -> {
//                    Log.d("PartyMethods", "Error getting user details: " + e.getMessage());
//                    callback.onFailure(e); // Handle the failure case
//                });
//    }

    public void updatePartyUserFilters(String partyCode, String email, Map<String, Object> filtersToUpdate, Callback callback) {
        // Reference to the user's document in the "Users" collection of the specified party
        DocumentReference userDocRef = db.collection("Parties").document(partyCode)
                .collection("Users").document(email);
        userDocRef.update(filtersToUpdate)
                .addOnSuccessListener(aVoid -> {
                    // After updating, retrieve the user details
                    userDocRef.get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Map<String, Object> userDetails = documentSnapshot.getData();
                                    userDetails.put("email", email);

                                    List<Map<String, Object>> filtersList = new ArrayList<>();
                                    filtersList.add(userDetails);

                                    Log.d("updatePartyUserFilters", "Filters updated and User Details fetched: " + filtersList);
                                    callback.onUserDataFetched(filtersList);
                                } else {
                                    Log.d("updatePartyUserFilters", "User document doesn't exist for email: " + email);
                                    callback.onFailure(new Exception("User document not found."));
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.d("updatePartyUserFilters", "Error getting user details: " + e.getMessage());
                                callback.onFailure(e); // Handle the failure case
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d("updatePartyUserFilters", "Error updating filters: " + e.getMessage());
                    callback.onFailure(e); // Handle the failure case
                });
    }

    public void getUserFilterDetails(String partyCode, UserFilterDetailsCallback callback) {
        // Declaring variables
        ArrayList<HashMap<String, Integer>> cuisineList = new ArrayList<>();
        ArrayList<Double> priceList = new ArrayList<>();
        ArrayList<Double> ratingList = new ArrayList<>();
        ArrayList<Double> distanceList = new ArrayList<>();

        // One hashmap to store all attributes
        HashMap<String, Object> resultMap = new HashMap<>();

        db.collection("Parties")
                .document(partyCode)
                .collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Initialize Hashmap
                            HashMap<String, Integer> cuisineCountMap = new HashMap<>();

                            // Getting the current document
                            QuerySnapshot queryData = task.getResult();

                            // Retrieving the data from the document
                            for (QueryDocumentSnapshot document : queryData) {
                                ArrayList<String> cuisineNames = (ArrayList<String>) document.get("Cuisine");
                                Double price = document.getDouble("price");
                                Double rating = document.getDouble("rating");
                                Double distance = document.getDouble("Distance");

                                // Update cuisine count hashmap
                                if (cuisineNames != null) {
                                    for (String cuisine : cuisineNames) {
                                        cuisineCountMap.put(cuisine, cuisineCountMap.getOrDefault(cuisine, 0) + 1);
                                    }
                                }

                                // Retrieving price, rating, and distance
                                if (price != null) {
                                    priceList.add(price);
                                }

                                if (rating != null) {
                                    ratingList.add(rating);
                                }

                                if (distance != null) {
                                    distanceList.add(distance);
                                }
                            }

                            // Calculate averages
                            double averagePrice = priceList.isEmpty() ? 0 : priceList.stream().mapToDouble(a -> a).average().getAsDouble();
                            double averageRating = ratingList.isEmpty() ? 0 : ratingList.stream().mapToDouble(a -> a).average().getAsDouble();
                            double averageDistance = distanceList.isEmpty() ? 0 : distanceList.stream().mapToDouble(a -> a).average().getAsDouble();

                            cuisineList.add(cuisineCountMap);
                            resultMap.put("cuisineList", cuisineList);
                            resultMap.put("averagePrice", averagePrice);
                            resultMap.put("averageRating", averageRating);
                            resultMap.put("averageDistance", Math.round(averageDistance * 10.0) / 10.0);

                            // Invoke the callback with the resultMap when data retrieval is complete
                            callback.onUserFilterDetailsResult(resultMap);
                        }
                    }
                });
    }

    public static ArrayList<String> filterUserCuisinePreferences(HashMap<String, Integer> userCuisinePreferences) {
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(userCuisinePreferences.entrySet());
        entryList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        int count = Math.min(3, entryList.size());

        ArrayList<String> topCuisines = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            topCuisines.add(entryList.get(i).getKey());
        }

        return topCuisines;
    }

    public void filterRestaurants(ArrayList<Map<String, Object>> allRestaurantsData,
                                  HashMap<String, Object> avgUserAttributes,
                                  Context context,
                                  FilterResultsCallback resultsCallback) {


        getLocation(context, new GeoPointCallback() {
            @Override
            public void onGeoPointReceived(GeoPoint userGeoPoint) {
                ArrayList<HashMap<String, HashMap<String, Double>>> results = new ArrayList<>();
                String[] TopCuisines = new String[3];

                double avgPrice = (double) avgUserAttributes.get("averagePrice");
                double avgRating = (double) avgUserAttributes.get("averageRating");
                double avgDistance = (double) avgUserAttributes.get("averageDistance");
                ArrayList<HashMap<String, Integer>> userCuisinePreferences = (ArrayList<HashMap<String, Integer>>) avgUserAttributes.get("cuisineList");

                ArrayList<String> userCuisinePreferenceList = filterUserCuisinePreferences(userCuisinePreferences.get(0));
                System.out.println("User's Cuisine Preferences: " + userCuisinePreferenceList + " " + allRestaurantsData.size());

                for (Map<String, Object> restaurant : allRestaurantsData) {
                    String restaurantName = (String) restaurant.get("Restaurant Name");
                    double restaurantAvgPrice = (double) restaurant.get("Average Price");
                    double restaurantAvgRating = (double) restaurant.get("Average Rating");
                    GeoPoint restaurantGeoLocation = (GeoPoint) restaurant.get("GeoLocation");
                    String restaurantCuisine = (String) restaurant.get("Cuisine");


                    if ((avgPrice == 0 || restaurantAvgPrice <= avgPrice) && (restaurantAvgRating >= avgRating || avgRating == 0)) {
                        HashMap<String, Double> restaurantMap = new HashMap<>();
                        double restaurantDistance = calculateGeoLocationDiff(userGeoPoint, restaurantGeoLocation);
                        restaurantMap.put("distance", restaurantDistance);
                        restaurantMap.put("averageRating", restaurantAvgRating);
                        restaurantMap.put("averagePrice", restaurantAvgPrice);
                        System.out.println("Restaurant distance: " + restaurantDistance + " " + avgDistance);

                        if (userCuisinePreferenceList.contains(restaurantCuisine)) {
                            System.out.println("Adding restaurant: " + restaurantName + " to results");
                            HashMap<String, HashMap<String, Double>> resultMap = new HashMap<String, HashMap<String, Double>>();
                            resultMap.put(restaurantName, restaurantMap);
                            results.add(resultMap);
                        } else {
                            System.out.println("Restaurant " + restaurantName + " does not match cuisine preferences");
                        }
                    }
                }

                results = sortRestaurantResults(results);

                System.out.println("Filtered Restaurants: " + results);
                resultsCallback.onFilterResults(results);
            }
        });
    }

    private ArrayList<HashMap<String, HashMap<String, Double>>> sortRestaurantResults(ArrayList<HashMap<String, HashMap<String, Double>>> results) {
        results.sort((o1, o2) -> {
            HashMap<String, Double> restaurant1 = o1.get(o1.keySet().toArray()[0]);
            HashMap<String, Double> restaurant2 = o2.get(o2.keySet().toArray()[0]);

            double restaurant1Distance = restaurant1.get("distance");
            double restaurant2Distance = restaurant2.get("distance");

            if (restaurant1Distance < restaurant2Distance) {
                return -1;
            } else if (restaurant1Distance > restaurant2Distance) {
                return 1;
            } else {
                return 0;
            }
        });

        return results;
    }

    private double calculateGeoLocationDiff(GeoPoint userLocation, GeoPoint restaurantLocation) {
        double userLatitude = userLocation.getLatitude();
        double userLongitude = userLocation.getLongitude();
        double restaurantLatitude = restaurantLocation.getLatitude();
        double restaurantLongitude = restaurantLocation.getLongitude();

        final int R = 6371;

        double lat1 = Math.toRadians(userLatitude);
        double lon1 = Math.toRadians(userLongitude);
        double lat2 = Math.toRadians(restaurantLatitude);
        double lon2 = Math.toRadians(restaurantLongitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate the distance in kilometers
        double distance = R * c;

        return distance;
    }

    public void getLocation(Context context, final GeoPointCallback geoPointCallback) {
        LocationHelper locationHelper = new LocationHelper(context);
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double[] formattedLocation) {
                if (formattedLocation != null && formattedLocation.length == 2) {
                    // Use the first element as latitude and the second as longitude
                    double latitude = formattedLocation[0];
                    double longitude = formattedLocation[1];

                    // Create a GeoPoint using the obtained latitude and longitude
                    GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                    // Invoke the callback with the GeoPoint
                    geoPointCallback.onGeoPointReceived(geoPoint);
                } else {
                    // Handle the case where formattedLocation is null or not as expected
                    Log.e("getLocation", "Invalid location format");
                }
            }
        });
    }

//    public interface GeoPointCallback {
//        void onGeoPointReceived(GeoPoint geoPoint);
//    }
//
//    // Check distance between user and restaurant
//    public double calculateDistanceBetweenGeohashes(String geohash1, String geohash2) {
//        // Decode the geohashes to obtain latitude and longitude coordinates
//        double[] coordinates1 = decodeGeohash(geohash1);
//        double[] coordinates2 = decodeGeohash(geohash2);
//
//        // Calculate the distance between the two coordinates using the Haversine formula
//        double lat1 = coordinates1[0];
//        double lon1 = coordinates1[1];
//        double lat2 = coordinates2[0];
//        double lon2 = coordinates2[1];
//
//        final double R = 6371;
//
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//
//        // Calculate the distance
//        double distance = R * c;
//        Log.d("calculateDistance", "The distance between the two coordinates is: " + distance + " km");
//
//        return distance;
//    }
//
//    public static double[] decodeGeohash(String geohash) {
//        double minLat = -90.0; // Minimum latitude
//        double maxLat = 90.0;  // Maximum latitude
//        double minLon = -180.0; // Minimum longitude
//        double maxLon = 180.0;  // Maximum longitude
//        boolean isEvenBit = true; // Used to alternate between latitude and longitude bits
//
//        double[] coordinates = new double[2]; // [latitude, longitude]
//
//        for (int i = 0; i < geohash.length(); i++) {
//            char c = geohash.charAt(i);
//            int cd = BASE32.indexOf(c);
//
//            for (int j = 4; j >= 0; j--) {
//                int mask = 1 << j;
//                if (isEvenBit) {
//                    refineInterval(coordinates, cd, mask, minLon, maxLon, 0);
//                } else {
//                    refineInterval(coordinates, cd, mask, minLat, maxLat, 1);
//                }
//                isEvenBit = !isEvenBit;
//            }
//        }
//
//        // Calculate the latitude and longitude averages
//        double latitude = (minLat + maxLat) / 2;
//        double longitude = (minLon + maxLon) / 2;
//
//        coordinates[0] = latitude;
//        coordinates[1] = longitude;
//
//        return coordinates;
//    }
//
//    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
//
//    private static void refineInterval(double[] coordinates, int cd, int mask, double min, double max, int index) {
//        if ((cd & mask) != 0) {
//            min = (min + max) / 2;
//        } else {
//            max = (min + max) / 2;
//        }
//
//        if (index == 0) {
//            coordinates[1] = (min + max) / 2;
//        } else {
//            coordinates[0] = (min + max) / 2;
//        }
//    }

    // Helper method to show a toast message
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}