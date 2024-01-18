package com.example.ande_munch;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RestaurantDetails extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView restaurantName;
    private MenuCardAdapter mcAdapter;
    private ReviewsAdapter rvAdapter;
    private ImageView rImage;
    private TextView desc;
    private LinearLayout cuisines;
    private TextView rating;
    private TextView details;
    //List<DocumentSnapshot> foodItems = new ArrayList<>();
    List<Review> reviewItems;
    private final String TAG = "RestaurantDetails";
    private final String[] weekdays = new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"," Sun"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_details);

        restaurantName = findViewById(R.id.restaurantDetailsName);
        rImage = findViewById(R.id.restaurantDetailsImage);
        desc = findViewById(R.id.restarauntDetailsDesc);
        cuisines = findViewById(R.id.restaurantDetailsCuisines);
        rating = findViewById(R.id.restaurantDetailsRating);
        details = findViewById(R.id.restarauntDetails);
    }

   @Override
    protected void onStart() {
        super.onStart();
       reviewItems = new ArrayList<>(); //Clear existing data
        Bundle info = getIntent().getExtras();
        String restaurantID = info.getString("RestaurantId");
        double avgPrice = info.getDouble("AvgPrice");
        double avgRating = info.getDouble("AvgRating");
        String imageUrl = info.getString("Image");
        String descText = info.getString("Desc");
        ArrayList<String> cuisineArray = info.getStringArrayList("Cuisine");
        ArrayList<Object> openingHoursArray = (ArrayList<Object>) info.get("OpeningHours");

        restaurantName.setText(restaurantID);
        desc.setText(descText);
        rating.setText(avgRating + "★");
        Picasso.get().load(imageUrl).into(rImage);

        String detailsText = "";
        int index = 0;
        for (Object object : openingHoursArray) {
           HashMap<String, String> openingHours = (HashMap<String, String>) object;
           if (openingHours != null) {
               String closeTime = openingHours.get("Close");
               String openTime = openingHours.get("Open");
               if (closeTime != null && openTime != null) {
                   detailsText += weekdays[index] + " " + openTime + "-" + closeTime + "\n";
               }
           }
           index++;
       }
       details.setText(detailsText);

       cuisines.removeAllViews();
       for (String cuisine: cuisineArray) {
           TextView textView = new TextView(this);
           textView.setText(cuisine);

               LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                   LinearLayout.LayoutParams.WRAP_CONTENT,
                   LinearLayout.LayoutParams.WRAP_CONTENT);

           int dp = (int) (0.5f * this.getResources().getDisplayMetrics().density);
           layoutParams.setMargins(0, 0, 24*dp, 0);
           textView.setPadding(26*dp, 8*dp, 26*dp, 8*dp);
           textView.setBackgroundColor(Color.parseColor("#BBBBBB"));
           textView.setLayoutParams(layoutParams);
           cuisines.addView(textView);
       }

       //GET MENU ITEMS
        db.collection("Restaurants").document(restaurantID).collection("Menu").get()
            .addOnCompleteListener(menuSnapshot -> {
                if (menuSnapshot.isSuccessful()) {
                    mcAdapter = new MenuCardAdapter(menuSnapshot.getResult().getDocuments());
                    RecyclerView foodCards = findViewById(R.id.menuItems);
                    LinearLayoutManager mcLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                    foodCards.setLayoutManager(mcLayoutManager);
                    foodCards.setAdapter(mcAdapter);
                }
            });

       //GET USER REVIEWS

       db.collection("Restaurants").document(restaurantID).collection("Reviews").get()
               .addOnCompleteListener(reviewSnapshot -> {
                   if (reviewSnapshot.isSuccessful()) {

                       //ReviewAdapter Setup
                       rvAdapter = new ReviewsAdapter(reviewItems);
                       RecyclerView reviews = findViewById(R.id.reviews);
                       LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                       reviews.setLayoutManager(rvLayoutManager);
                       reviews.setAdapter(rvAdapter);

                       //Get additional user info
                       for (DocumentSnapshot review : reviewSnapshot.getResult()) {
                           db.collection("Users").document(review.getString("UserID")).get()
                                   .addOnCompleteListener(userSnapshot -> {
                                       DocumentSnapshot user = userSnapshot.getResult();
                                       String username = user.getString("Username");
                                       String url = user.getString("ProfileImage");
                                       String feedback = review.getString("Text");
                                       Double rating = review.getDouble("Rating");
                                       reviewItems.add(new Review(username, feedback, url, rating));
                                       rvAdapter.notifyItemInserted(reviewItems.size() - 1);
                                   });

                       }
                   }
               });
   }
}
