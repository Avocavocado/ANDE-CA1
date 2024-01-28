package com.example.ande_munch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

public class DishDescriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dish_description);
        // Retrieve data from Intent
        String dishName = getIntent().getStringExtra("dishName");
        String dishImageUrl = getIntent().getStringExtra("dishImageUrl");
        String dishDescription = getIntent().getStringExtra("dishDescription");

        // Find views and set the data
        TextView nameTextView = findViewById(R.id.dishNameTxt);
        ShapeableImageView dishImageView = findViewById(R.id.descriptionImage);
        TextView descriptionTextView = findViewById(R.id.descriptionDetailsTxt);

        nameTextView.setText(dishName);
        descriptionTextView.setText(dishDescription);
        Picasso.get().load(dishImageUrl).into(dishImageView);


        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView profileImageView = findViewById(R.id.profileImageView);

        // Set the click listener
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DishDescriptionActivity.this, ProfilePage.class);
                startActivity(intent);
            }
        });
    }
}
