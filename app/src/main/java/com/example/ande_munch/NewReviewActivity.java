package com.example.ande_munch;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewReviewActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();
    String userEmail = currentUser.getEmail();
    private TextView rName;
    private String restaurantID;
    private final String TAG = "NewReviewActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_review);

        EditText feedbackInput = findViewById(R.id.feedbackInput);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        Button postBtn = findViewById(R.id.postReviewBtn);
        ImageButton cancelBtn = findViewById(R.id.cancelReviewBtn);
        Log.i(TAG,"USER EMAIL" + userEmail);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float rating = ratingBar.getRating();
                String feedback = String.valueOf(feedbackInput.getText());
                Log.i(TAG, feedback + ": " + rating);
                Map<String, Object> data = new HashMap<>();
                data.put("UserID", userEmail);
                data.put("Text", feedback);
                data.put("Rating", rating);

                db.collection("Restaurants").document(restaurantID).collection("Reviews")
                        .add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                finish();
                            }
                        });
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle info = getIntent().getExtras();
        restaurantID = info.getString("RestaurantId");

        rName = findViewById(R.id.restaurantName);
        rName.setText(restaurantID);
    }
}
