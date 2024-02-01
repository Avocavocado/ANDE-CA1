package com.example.ande_munch;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ande_munch.methods.SwipeMethods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SwipePage extends AppCompatActivity {
    // Define new callbacks
    public interface CardCreatedCallback {
        void onCardCreated(CardView cardView);
    }

    public interface AllCardsLoadedCallback {
        void onAllCardsLoaded(List<CardView> cardViews);
    }

    // Define new attributes
    private int currentCardIndex = 0;
    private HashMap<String, HashMap<String, Double>> eachRestaurantData = new HashMap<>();
    private ArrayList<HashMap<String, HashMap<String, Double>>> filteredRestaurantNames = new ArrayList<>();
    private ViewGroup parentLayout;
    public Button scuffedBtn;

    // Declaring new classes
    SwipeMethods swipeMethods = new SwipeMethods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_page);

        // Declaring buttons
        scuffedBtn = findViewById(R.id.scuffedBtn);

        // Add the cards to the parent layout
        parentLayout = findViewById(R.id.RestaurantCards);

        // Retrieve the intent that started this activity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("filteredRestaurantNames")) {
            filteredRestaurantNames = (ArrayList<HashMap<String, HashMap<String, Double>>>) intent.getSerializableExtra("filteredRestaurantNames");

            eachRestaurantData = getRestaurantData(filteredRestaurantNames);
            Log.d("SwipePage", "eachRestaurantData: " + eachRestaurantData);

            // Set onClickListener for scuffedBtn
            scuffedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipeMethods.getRestaurantDetails(eachRestaurantData, new SwipeMethods.RestaurantFieldsCallback() {
                        @Override
                        public void onRestaurantFieldsFetched(HashMap<String, Map<String, Object>> restaurantDetails) {
                            Log.d("SwipePage", "restaurantDetails: " + restaurantDetails);

                            // Load all the cards
                            loadAllCards(restaurantDetails, SwipePage.this, new AllCardsLoadedCallback() {
                                @Override
                                public void onAllCardsLoaded(List<CardView> cardViews) {
                                    for (CardView cardView : cardViews) {
                                        Log.d("SwipePage", "Adding cardView to parentLayout " + cardView);
                                        parentLayout.addView(cardView);
                                    }
                                }
                            });
                        }
                    });
                }
            });

            // Taking the keys and calling the get method attribute
            swipeMethods.getRestaurantDetails(eachRestaurantData, new SwipeMethods.RestaurantFieldsCallback() {
                @Override
                public void onRestaurantFieldsFetched(HashMap<String, Map<String, Object>> restaurantDetails) {
                    Log.d("SwipePage", "restaurantDetails: " + restaurantDetails);

                    // Load all the cards
                    loadAllCards(restaurantDetails, SwipePage.this, new AllCardsLoadedCallback() {
                        @Override
                        public void onAllCardsLoaded(List<CardView> cardViews) {
                            for (CardView cardView : cardViews) {
                                Log.d("SwipePage", "Adding cardView to parentLayout " + cardView);
                                parentLayout.addView(cardView);
                            }
                        }
                    });
                }
            });
        } else {
            Log.d("SwipePage", "Intent does not have filteredRestaurantNames");
        }
    }

    private HashMap<String, HashMap<String, Double>> getRestaurantData(ArrayList<HashMap<String, HashMap<String, Double>>> filteredRestaurantNames) {
        HashMap<String, HashMap<String, Double>> eachRestaurantData = new HashMap<>();

        if (filteredRestaurantNames != null) {
            for (HashMap<String, HashMap<String, Double>> restaurant : filteredRestaurantNames) {
                for (Map.Entry<String, HashMap<String, Double>> entry : restaurant.entrySet()) {
                    eachRestaurantData.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return eachRestaurantData;
    }

    private void loadAllCards(HashMap<String, Map<String, Object>> restaurantDetails, Context context, AllCardsLoadedCallback allCardsLoadedCallback) {
        List<CardView> restaurantCardViews = new ArrayList<>();
        AtomicInteger cardsProcessed = new AtomicInteger(0);

        for (Map.Entry<String, Map<String, Object>> entry : restaurantDetails.entrySet()) {
            retrieveInfoAndPopulateCard(entry, context, restaurantCardViews, new CardCreatedCallback() {
                @Override
                public void onCardCreated(CardView cardView) {
                    if (cardsProcessed.incrementAndGet() == restaurantDetails.size()) {
                        allCardsLoadedCallback.onAllCardsLoaded(restaurantCardViews);
                    }
                }
            });
        }
    }

    private void retrieveInfoAndPopulateCard(Map.Entry<String, Map<String, Object>> restaurantDetails, Context context, List<CardView> cardList, CardCreatedCallback callback) {
        String restaurantName = restaurantDetails.getKey();
        Map<String, Object> restaurantData = restaurantDetails.getValue();

        // Inflate cardView here
        CardView restaurantView = (CardView) LayoutInflater.from(context).inflate(R.layout.restaurant_card, null, false);

        // Get the restaurant's details
        String restaurantImage = (String) restaurantData.get("RestaurantImage");
        String description = (String) restaurantData.get("Desc");
        List<String> cuisine = (List<String>) restaurantData.get("Cuisine");
        String address = (String) restaurantData.get("Address");
        Double averageRating = (Double) restaurantData.get("averageRating");
        Double averagePrice = (Double) restaurantData.get("averagePrice");
        List<Map<String, String>> openingHours = (List<Map<String, String>>) restaurantData.get("OpeningHours");

        Log.d("SwipePage", "restaurantName: " + restaurantName);
        Log.d("SwipePage", "restaurantImage: " + restaurantImage);
        Log.d("SwipePage", "description: " + description);
        Log.d("SwipePage", "cuisine: " + cuisine);
        Log.d("SwipePage", "address: " + address);
        Log.d("SwipePage", "averageRating: " + averageRating);
        Log.d("SwipePage", "averagePrice: " + averagePrice);
        Log.d("SwipePage", "openingHours: " + openingHours);

        // Check if the restaurant is open and set up the card
        swipeMethods.checkOpeningStatus(openingHours, new SwipeMethods.OpeningStatusCallback() {
            @Override
            public void onStatusChecked(boolean isOpen) {
                settingUpCard(context, restaurantView, isOpen, restaurantImage, restaurantName, description, cuisine, address, averageRating, averagePrice, new CardCreatedCallback() {
                    @Override
                    public void onCardCreated(CardView cardView) {
                        try {
                            // Add the cardView to the list
                            Log.d("SwipePage", "Adding cardView to cardList " + cardView);
                            cardList.add(cardView);

                            // Notify that a card has been created
                            callback.onCardCreated(cardView);
                        } catch (Exception e) {


                            Log.d("SwipePage", "Error: " + e);
                        }
                    }
                });
            }
        });
    }

    private void settingUpCard(Context context, CardView restaurantView, boolean isOpen, String restaurantImage, String restaurantName,
                               String description, List<String> cuisine, String address, Double averageRating,
                               Double averagePrice, CardCreatedCallback callback) {

        // Find the respective IDs of the CardView
        ImageView restaurantImageView = restaurantView.findViewById(R.id.RestaurantImage);
        TextView restaurantNameView = restaurantView.findViewById(R.id.RestaurantName);
        TextView restaurantRatingView = restaurantView.findViewById(R.id.restaurantRating);
        TextView restaurantOpenStatusView = restaurantView.findViewById(R.id.OpenInfo);
        TextView restaurantDescView = restaurantView.findViewById(R.id.RestaurantDesc);
        LinearLayout cuisinesLayout = restaurantView.findViewById(R.id.Cuisines);

        // Set the respective values of the CardView
        restaurantNameView.setText(restaurantName);
        restaurantDescView.setText(description);
        restaurantOpenStatusView.setText(isOpen ? "Open" : "Closed");
        restaurantRatingView.setText(String.format("%.1f★", averageRating));

        // Load the restaurant image
        Picasso.get().load(restaurantImage).into(restaurantImageView);

        // Use the callback to return the populated CardView
        if (callback != null) {
            callback.onCardCreated(restaurantView);
        } else {
            Log.d("SwipePage", "Callback onCardCreated is null");
        }
    }
}
