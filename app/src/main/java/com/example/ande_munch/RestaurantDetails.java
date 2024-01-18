package com.example.ande_munch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RestaurantDetails extends AppCompatActivity {

    private TextView restaurantName;
    private String restaurantID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_details);
        restaurantName = findViewById(R.id.restaurantDetailsName);
    }

   @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        restaurantID = intent.getExtras().getString("RestaurantId");
        double avgPrice = intent.getExtras().getDouble("AvgPrice");
        restaurantName.setText(restaurantID + " " + avgPrice);
   }
}
