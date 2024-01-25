package com.example.ande_munch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ande_munch.methods.Callback;
import com.example.ande_munch.methods.PartyMethods;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayParty extends AppCompatActivity {
    private String priceFilter = "Any";
    private String ratingFilter = "Any";
    private int distanceFilter = 0;
    ArrayList<String> selectedCuisines;
    PartyMethods partyMethods = new PartyMethods();
    private String loggedInEmail;
    private String partyCode;
    private String ratingValue;
    private String priceValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitvity_display_party);

        loggedInEmail = getIntent().getStringExtra("email");
        partyCode = getIntent().getStringExtra("dialogCode");

        Button buttonFilterPage = findViewById(R.id.partyFilterBtn);
        buttonFilterPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayParty.this, PartyFilter.class);
                getFilters.launch(intent);
            }
        });
    }
    private HashMap<String, Object> generateFilterDetails() {
        HashMap<String, Object> filtersToUpdate = new HashMap<>();
        if(!ratingFilter.equals("Any")){
            ratingValue = ratingFilter.substring(0, ratingFilter.length() - 1);
        }else{
            ratingValue = ratingFilter;
        }
        if(!priceFilter.equals("Any")){
            priceValue = priceFilter.substring(1);
        }else{
            priceValue = priceFilter;
        }
        double maxDistance = (double) distanceFilter / 10;
        String stringDistance  = Double.toString(maxDistance);
        filtersToUpdate.put("price", priceValue);
        filtersToUpdate.put("rating", ratingValue);
        filtersToUpdate.put("Distance", stringDistance);
        filtersToUpdate.put("Cuisine", selectedCuisines);
        return filtersToUpdate;
    }
    private final ActivityResultLauncher<Intent> getFilters = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    priceFilter = data.getStringExtra("Price");
                    ratingFilter = data.getStringExtra("Rating");
                    distanceFilter = data.getIntExtra("Distance", 0);
                    selectedCuisines = data.getStringArrayListExtra("SelectedCuisines");
                    HashMap<String, Object> filterDetails = generateFilterDetails();
                    Log.d("Updating these filters","Details: "+ filterDetails);
                    partyMethods.updatePartyUserFilters(partyCode, loggedInEmail, filterDetails, new Callback() {
                        @Override
                        public void onUserChecked(boolean userExists) {
                            // Handle user check result if needed
                        }

                        @Override
                        public void onUserDataFetched(List<Map<String, Object>> usersList) {
                            // Handle user data fetched result if needed
                        }

                        @Override
                        public void onUserDataFetched(Map<String, Object> userDetails) {
                            // Handle user data fetched result if needed
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Handle failure if needed
                        }

                        @Override
                        public void onSuccess() {

                        }
                    });
                }
            }
    );


}
