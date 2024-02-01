package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ande_munch.methods.DisplayMethods;
import com.example.ande_munch.methods.PartyMethods;
import com.example.ande_munch.ui.dashboard.DashboardFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class DisplayParty extends AppCompatActivity {
    // Declaring UI variables
    private RecyclerView recyclerView;
    private PartyRecyclerAdapter partyAdapter;
    TextView numberOfPartyMembers;

    // Declaring attributes
    PartyMethods partyMethods = new PartyMethods();
    DisplayMethods displayMethods = new DisplayMethods();
    HashMap<String, Object> avgUserAttributes = new HashMap<>();
    ArrayList<HashMap<String, HashMap<String, Double>>> swipePageData = new ArrayList<>();
    String dialogCode;
    String email;
    Boolean isLeader = false;
    String username;

    int userCounter = 0;

    // Initialize intent
    ArrayList<Map<String, Object>> localRestaurantData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitvity_display_party);

        // Initialize RecyclerView and LayoutManager
        recyclerView = findViewById(R.id.partyListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(DisplayParty.this));

//        partyAdapter = new PartyRecyclerAdapter(memberList);
//        recyclerView.setAdapter(partyAdapter);

        // Retrieve the intent that started this activity
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        // Initialize the buttons
        Button buttonFilterPage = findViewById(R.id.partyFilterBtn);
        Button buttonSwipePage = findViewById(R.id.buttonSwipe);
        Button buttonLeavePage = findViewById(R.id.buttonLeave);

        // Logic for the leave button
        buttonLeavePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Check if intent is not null and has the extra "DIALOG_CODE"
        if (intent != null && intent.hasExtra("DIALOG_CODE")) {
            dialogCode = intent.getStringExtra("DIALOG_CODE");
            TextView partyName = findViewById(R.id.partyListHeader);

            SpannableString spannableString = new SpannableString("Party List " + dialogCode);
            ForegroundColorSpan backgroundSpan = new ForegroundColorSpan(Color.parseColor("#00FF66"));
            spannableString.setSpan(backgroundSpan, 10, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            partyName.setText(spannableString);

            if (dialogCode != null && !dialogCode.isEmpty()) {
                Log.d("DisplayParty", "Dialog Code: " + dialogCode);

                // Add party listener
                displayMethods.addPartyListener(dialogCode, new DisplayMethods.PartyListenerCallback() {
                    @Override
                    public void onPartyDataReceived(List<DocumentSnapshot> documents) {
                        ArrayList<HashMap<String, Object>> memberList = new ArrayList<>();
                        for (DocumentSnapshot document : documents) {
                            Log.d("PartyData", "Document: " + document.getData());

                            // Number of party members
                            userCounter = documents.size();

                            numberOfPartyMembers = findViewById(R.id.numberOfMembers);
                            numberOfPartyMembers.setText(String.valueOf(userCounter));

                            // Getting data from document snapshot
                            Map<String, Object> data = document.getData();

                            if (data != null) {
                                // Extract the username and isLeader from the data
                                username = (String) data.get("Username");
                                try {
                                    if (data.get("IsLeader") == null) {
                                        isLeader = false;
                                    } else {
                                        isLeader = (boolean) data.get("IsLeader");
                                    }
                                } catch (ClassCastException e) {
                                    e.printStackTrace();
                                    Log.d("DisplayParty", "IsLeader is not a boolean" + e.getMessage());
                                    isLeader = false;
                                }

                                // Check if username and isLeader are not null
                                if (username != null && isLeader != null) {
                                    // Use the extracted username and isLeader values here
                                    Log.d("PartyData", "Username: " + username);
                                    Log.d("PartyData", "IsLeader: " + isLeader);

                                    // Loading of all party members into the hashmap
                                    HashMap<String, Object> memberInfo = new HashMap<>();
                                    memberInfo.put("Username", username);
                                    memberInfo.put("IsLeader", isLeader);

                                    // Adding each member into the memberList
                                    memberList.add(memberInfo);
                                    Log.d("PartyData", "Member List: " + memberList);

                                    // partyAdapter.updateData(memberList);
                                } else {
                                    Log.d("PartyData", "Username or isLeader is null");
                                }
                            }
                        }

                        // Create a new adapter and set it to the RecyclerView
                        PartyRecyclerAdapter newAdapter = new PartyRecyclerAdapter(memberList);
                        recyclerView.setAdapter(newAdapter);
                    }
                });
            } else {
                Log.d("DisplayParty", "Dialog Code is null or empty");
            }
        } else {
            Log.d("DisplayParty", "Intent is null or doesn't have DIALOG_CODE");
        }

        // Get all restaurants data
        displayMethods.getRestaurantsDetails(new DisplayMethods.FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<Map<String, Object>> allRestaurantsData) {
                // Handle the allRestaurantsData
                Log.d("AllRestaurants", "All Restaurants: " + allRestaurantsData);

                for (Map<String, Object> restaurant : allRestaurantsData) {
                    localRestaurantData.add(restaurant);

                    Log.d("RestaurantDetails", "Restaurant: " + restaurant);
                    for (Map.Entry<String, Object> entry : restaurant.entrySet()) {
                        Log.d("RestaurantDetail", entry.getKey() + ": " + entry.getValue());
                    }
                }
            }
        });

        buttonFilterPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayParty.this, PartyFilter.class);
                intent.putExtra("PartyCode", dialogCode);
                intent.putExtra("User", email);
                startActivity(intent);
            }
        });

        buttonSwipePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                partyMethods.getUserFilterDetails(dialogCode, new PartyMethods.UserFilterDetailsCallback() {
                    @Override
                    public void onUserFilterDetailsResult(HashMap<String, Object> resultMap) {
                        // Process the resultMap and update avgUserAttributes
                        ArrayList<HashMap<String, Integer>> cuisineList = (ArrayList<HashMap<String, Integer>>) resultMap.get("cuisineList");
                        double averagePrice = (double) resultMap.get("averagePrice");
                        double averageRating = (double) resultMap.get("averageRating");
                        double averageDistance = (double) resultMap.get("averageDistance");

                        avgUserAttributes.put("cuisineList", cuisineList);
                        avgUserAttributes.put("averagePrice", averagePrice);
                        avgUserAttributes.put("averageRating", averageRating);
                        avgUserAttributes.put("averageDistance", averageDistance);

                        Log.d("UserAttr", "Cuisine List: " + cuisineList);
                        Log.d("UserAttr", "Average Price: " + averagePrice);
                        Log.d("UserAttr", "Average Rating: " + averageRating);
                        Log.d("UserAttr", "Average Distance: " + averageDistance);

                        // Now that avgUserAttributes is updated, call filterRestaurants
                        partyMethods.filterRestaurants(localRestaurantData, avgUserAttributes, DisplayParty.this, new PartyMethods.FilterResultsCallback() {
                            @Override
                            public void onFilterResults(ArrayList<HashMap<String, HashMap<String, Double>>> filteredRestaurantNames) {
                                // Handle the filtered restaurant names
                                Log.d("FilteredRestaurants", "Filtered Restaurants: " + filteredRestaurantNames);
                                swipePageData = filteredRestaurantNames;

                                // Start SwipePage Activity inside this callback
                                Intent swipeIntent = new Intent(DisplayParty.this, restaurant_card_swipe.class);
                                Log.d("FilteredRestaurants", "Filtered Restaurant Names: " + swipePageData);
                                swipeIntent.putExtra("filteredRestaurantNames", swipePageData);
                                startActivity(swipeIntent);
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateUI() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        ;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
