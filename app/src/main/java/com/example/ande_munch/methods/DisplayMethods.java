package com.example.ande_munch.methods;

import com.example.ande_munch.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import android.support.annotation.NonNull;
import android.util.Log;

import androidx.annotation.Nullable;

public class DisplayMethods {
    private static final String TAG = "DisplayMethods";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Map<String, Object>> restaurantData = new ArrayList<>();
    private Map<String, Object> restaurantDetails = new HashMap<String, Object>();

    public interface AveragePriceCallback {
        void onAveragePriceCalculated(double averagePrice);
    }

    public interface AverageRatingCallback {
        void onAverageRatingCalculated(double averageRating);
    }

    public interface GeoLocationCallback {
        void onGeoLocationReceived(GeoPoint GeoLocation);
    }

    public interface PartyListenerCallback {
        void onPartyDataReceived(List<DocumentSnapshot> documents);
    }

    public interface FirestoreCallback {
        void onCallback(ArrayList<Map<String, Object>> restaurantData);
    }

    public void getRestaurantsDetails(final FirestoreCallback callback) {
        db.collection("Restaurants")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final int[] restaurantsProcessed = {0}; // Keep track of how many restaurants have been processed
                            int totalRestaurants = task.getResult().size();

                            for (QueryDocumentSnapshot restaurantDocument : task.getResult()) {
                                // Getting restaurant name
                                String restaurantName = restaurantDocument.getId();
                                // Getting cuisine field
                                ArrayList <String> cuisineList = (ArrayList<String>) restaurantDocument.get("Cuisine");
                                String cuisine = cuisineList.get(0);

                                // Call the method to fetch menu and calculate average price for each restaurant
                                calcAveragePrices(restaurantName, new AveragePriceCallback() {
                                    @Override
                                    public void onAveragePriceCalculated(double avgPrice) {
                                        // Calculate average rating for the same restaurant
                                        calcAverageRating(restaurantName, new AverageRatingCallback() {
                                            @Override
                                            public void onAverageRatingCalculated(double avgRating) {
                                                // Retrieve the GeoPoint for this restaurant
                                                getGeoLocation(restaurantName, new GeoLocationCallback() {
                                                    @Override
                                                    public void onGeoLocationReceived(GeoPoint GeoLocation) {
                                                        // Both calculations and geolocation retrieval are complete for this restaurant
                                                        Map<String, Object> restaurantDetails = new HashMap<>();
                                                        restaurantDetails.put("Restaurant Name", restaurantName);
                                                        restaurantDetails.put("Average Price", avgPrice);
                                                        restaurantDetails.put("Average Rating", avgRating);
                                                        restaurantDetails.put("GeoLocation", GeoLocation);
                                                        restaurantDetails.put("Cuisine", cuisine);

                                                        // Add the restaurant details to the list
                                                        restaurantData.add(restaurantDetails);

                                                        // Check if all restaurants have been processed
                                                        restaurantsProcessed[0]++;
                                                        if (restaurantsProcessed[0] == totalRestaurants) {
                                                            // Notify the callback that all operations are complete
                                                            callback.onCallback(restaurantData);
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        } else {
                            Log.d("DisplayParty", "Error getting restaurant documents: ", task.getException());
                        }
                    }
                });
    }

    private void calcAveragePrices(String restaurantName, AveragePriceCallback callback) {
        // Create an ArrayList to store dish prices for this restaurant
        ArrayList<Double> restaurantPriceArray = new ArrayList<>();

        // Retrieve the "Menu" subcollection for each restaurant
        db.collection("Restaurants")
                .document(restaurantName)
                .collection("Menu")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> menuTask) {
                        if (menuTask.isSuccessful()) {
                            for (QueryDocumentSnapshot dishDocument : menuTask.getResult()) {
                                // Getting the price attribute
                                Double dishPrice = dishDocument.getDouble("Price");

                                // Adding each dish price to the priceArray
                                restaurantPriceArray.add(dishPrice);
                                Log.d("DisplayParty", "Dish price: " + dishPrice);
                            }

                            // Calculate the average price for this restaurant after fetching all dish prices
                            double averagePriceForRestaurant = 0.0;
                            if (!restaurantPriceArray.isEmpty()) {
                                averagePriceForRestaurant = restaurantPriceArray.stream().mapToDouble(val -> val).average().orElse(0.0);
                            }

                            Log.d("DisplayParty", "Restaurant: " + restaurantName);
                            Log.d("DisplayParty", "Average price: " + averagePriceForRestaurant);

                            // Invoke the callback with the calculated average price
                            callback.onAveragePriceCalculated(averagePriceForRestaurant);
                        } else {
                            Log.d("DisplayParty", "Error getting menu documents: ", menuTask.getException());
                        }
                    }
                });
    }

    private void calcAverageRating(String restaurantName, AverageRatingCallback callback) {
        // Create an ArrayList to store dish ratings for this restaurant
        ArrayList<Double> restaurantRatingArray = new ArrayList<>();

        // Retrieve the "Reviews" subcollection for each restaurant
        db.collection("Restaurants")
                .document(restaurantName)
                .collection("Reviews")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> menuTask) {
                        if (menuTask.isSuccessful()) {
                            for (QueryDocumentSnapshot dishDocument : menuTask.getResult()) {
                                // Getting the rating attribute
                                Double perRating = dishDocument.getDouble("Rating");

                                // Check if the rating is not null before adding it to the array
                                if (perRating != null) {
                                    restaurantRatingArray.add(perRating);
                                    Log.d("DisplayParty", "Rating: " + perRating);
                                }
                            }

                            // Calculate the average rating for this restaurant after fetching all ratings
                            double averageRatingForRestaurant = 0.0;
                            if (!restaurantRatingArray.isEmpty()) {
                                averageRatingForRestaurant = restaurantRatingArray.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                            }

                            Log.d("DisplayParty", "Restaurant: " + restaurantName);
                            Log.d("DisplayParty", "Average rating: " + averageRatingForRestaurant);

                            // Invoke the callback with the calculated average rating
                            callback.onAverageRatingCalculated(averageRatingForRestaurant);
                        } else {
                            Log.d("DisplayParty", "Error getting menu documents: ", menuTask.getException());
                        }
                    }
                });
    }

    private void getGeoLocation(String restaurantName, GeoLocationCallback callback) {
        db.collection("Restaurants")
                .document(restaurantName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve the GeoPoint field "Location"
                                GeoPoint GeoLocation = document.getGeoPoint("Location");

                                if (GeoLocation != null) {
                                    // Invoke the callback with the retrieved GeoPoint
                                    callback.onGeoLocationReceived(GeoLocation);
                                } else {
                                    // Handle the case where the GeoPoint is null
                                    callback.onGeoLocationReceived(null);
                                }
                            } else {
                                // Handle the case where the document does not exist
                                callback.onGeoLocationReceived(null);
                            }
                        } else {
                            // Handle any errors that occurred during the retrieval
                            Log.d("DisplayParty", "Error getting restaurant document: ", task.getException());
                            callback.onGeoLocationReceived(null);
                        }
                    }
                });
    }

    // Adding a firebase listener to the party
    public void addPartyListener(String partyCode, final PartyListenerCallback callback) {
        CollectionReference collectionRef = db.collection("Parties")
                .document(partyCode)
                .collection("Users");

        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (snapshot != null && !snapshot.isEmpty()) {
                    List<DocumentSnapshot> documents = snapshot.getDocuments();
                    callback.onPartyDataReceived(documents);
                    Log.d("DisplayParty", "Listener updated with " + documents.size() + " documents");
                } else {
                    Log.d("DisplayParty", "No data found in the snapshot");
                }
            }
        });
    }

//    public void getMenu(DocumentReference restaurantReference, String restaurantName, HomeFragment.Callback callback) {
//        restaurantReference.collection("Menu").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                double totalPrice = 0;
//                int itemCount = 0;
//                for (QueryDocumentSnapshot menuDocument : task.getResult()) {
//                    Double price = menuDocument.getDouble("Price");
//                    if (price != null) {
//                        totalPrice += price;
//                        itemCount++;
//                    }
//                }
//                double averagePrice = itemCount > 0 ? totalPrice / itemCount : 0;
//                averagePrice = Math.round(averagePrice);
//                getReviews(restaurantReference, averagePrice, restaurantName, callback);
//                Log.d("test", "getMenu: " + averagePrice + " " + restaurantName);
//            } else {
//                Log.w(TAG, "Error getting menu items", task.getException());
//            }
//        });
//    }

    public void getReviews(DocumentReference restaurantReference, double avgPrice, String restaurantName, HomeFragment.Callback callback) {
        restaurantReference.collection("Reviews").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                double totalRating = 0;
                int reviewCount = 0;
                for (QueryDocumentSnapshot reviewDocument : task.getResult()) {
                    Number rating = reviewDocument.getDouble("Rating");
                    if (rating != null) {
                        totalRating += rating.doubleValue();
                        reviewCount++;
                    }
                }
                double averageRating = reviewCount > 0 ? Math.round((totalRating / reviewCount) * 10.0) / 10.0 : 0;
                callback.onSuccess(avgPrice, averageRating);
            } else {
                Log.w(TAG, "Error getting reviews", task.getException());
            }
        });
    }

//    private Map<String, Double> finalizeRestaurantDetails(double avgPrice, double avgRating, String restaurantName) {
//
//        Map<String, Double> map = new HashMap<>();
//        map.put("avgPrice", avgPrice);
//        map.put("avgRating", avgRating);
//        return map;
//        //Log.d(TAG, "Finalized details for " + restaurantName + ": " + details);
//    }
//
}
