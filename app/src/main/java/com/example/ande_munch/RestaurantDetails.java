package com.example.ande_munch;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.classes.Review;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantDetails extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView restaurantName;
    private MenuCardAdapter mcAdapter;
    private GoogleMap mMap;
    private ReviewsAdapter rvAdapter;
    private ImageView rImage;
    private TextView desc;
    private TextView address;
    private LinearLayout cuisines;
    private TextView rating;
    private TextView openInfo;
    private List<Review> reviewItems;
    private TableLayout openingHoursTable;
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
        rating = findViewById(R.id.Rating);
        openInfo = findViewById(R.id.OpenInfo);
        address = findViewById(R.id.restaurantAddress);
        openingHoursTable = findViewById(R.id.OpeningHours);

        ImageButton closeDetails = findViewById(R.id.closeRestaurantDetails);
        closeDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync((OnMapReadyCallback) this);
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
        String addressText = info.getString("Address");
        ArrayList<String> cuisineArray = info.getStringArrayList("Cuisine");
        List<Object> openingHours = (List<Object>) info.get("OpeningHours");

        restaurantName.setText(restaurantID);
        desc.setText(descText);
        rating.setText(avgRating + "★");
        address.setText(addressText);
        Picasso.get().load(imageUrl).into(rImage);

        //SET NEW REVIEW BUTTON LISTENER
       ImageButton newReviewBtn = findViewById(R.id.newReview);
       newReviewBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(RestaurantDetails.this, NewReviewActivity.class);
               intent.putExtra("RestaurantId", restaurantID);
               startActivity(intent);
           }
       });

       //OPENING HOURS
       int dayOfWeek = LocalDate.now().getDayOfWeek().getValue() - 1;
       Map<String,String> todayOH = (Map<String, String>) openingHours.get(dayOfWeek);
       LocalTime now = LocalTime.now();
       LocalTime openingTime = LocalTime.parse(todayOH.get("Open"), DateTimeFormatter.ofPattern("H:mm"));
       LocalTime closingTime = LocalTime.parse(todayOH.get("Close"), DateTimeFormatter.ofPattern("H:mm"));

       boolean hasOpened = !now.isBefore(openingTime);
       boolean hasClosed = !now.isBefore(closingTime);

       if (hasOpened && !hasClosed) {
           openInfo.setBackgroundColor(Color.parseColor("#00FF66"));
           openInfo.setText("Open til " + closingTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
       }
       else if (!hasOpened && !hasClosed){
           openInfo.setBackgroundColor(Color.parseColor("#30303A"));
           openInfo.setText("Opens at " + openingTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
       }
       else {
           openInfo.setBackgroundColor(Color.parseColor("#30303A"));
           Map<String,String> tomorrowOH = (Map<String, String>) openingHours.get(dayOfWeek != 6 ? dayOfWeek+1 : 0);
           LocalTime tomorrowOpeningTime = LocalTime.parse(tomorrowOH.get("Open"), DateTimeFormatter.ofPattern("H:mm"));
           openInfo.setText("Closed til " + tomorrowOpeningTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
       }

       openingHoursTable.removeAllViews();
       int index = 0;
       for (Object object : openingHours) {
           HashMap<String, String> dayOpeningHours = (HashMap<String, String>) object;
           if (dayOpeningHours != null) {
               String closeTime = LocalTime.parse(dayOpeningHours.get("Close"), DateTimeFormatter.ofPattern("H:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
               String openTime = LocalTime.parse(dayOpeningHours.get("Open"), DateTimeFormatter.ofPattern("H:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
               if (closeTime != null && openTime != null) {
                   TableRow dataRow = new TableRow(this);

                   // Data column 1 (Day)
                   TextView dayText = new TextView(this);
                   dayText.setText(weekdays[index]);
                   dayText.setGravity(Gravity.CENTER);
                   dayText.setLayoutParams(new TableRow.LayoutParams(
                           0,
                           TableRow.LayoutParams.WRAP_CONTENT,
                           2f));
                   dataRow.addView(dayText);

                    // Data column 2 (Opening Hours)
                   TextView openingHoursText = new TextView(this);
                   openingHoursText.setText(openTime + "-" + closeTime + "\n");
                   openingHoursText.setGravity(Gravity.CENTER);
                   openingHoursText.setLayoutParams(new TableRow.LayoutParams(
                           0,
                           TableRow.LayoutParams.WRAP_CONTENT,
                           3f));
                   dataRow.addView(openingHoursText);

                   // Add data row to the table
                   openingHoursTable.addView(dataRow, new TableLayout.LayoutParams(
                           TableLayout.LayoutParams.MATCH_PARENT,
                           TableLayout.LayoutParams.WRAP_CONTENT));
               }
           }
           index++;
       }

        //DISPLAY CUISINES
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
           textView.setBackgroundColor(Color.parseColor("#D7DAFF"));
           textView.setTextColor(Color.parseColor("#53555C"));
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle info = getIntent().getExtras();
        double lat = info.getDouble("Lat");
        double lon = info.getDouble("Lon");
        Log.i(TAG, "LATLONG: " + lat + "," + lon);
        // Add a marker in Singapore and move the camera
        LatLng position = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(position).title(info.getString("RestaurantId")));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 16);
        mMap.animateCamera(cameraUpdate);
    }
}
