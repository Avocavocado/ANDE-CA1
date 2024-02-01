package com.example.ande_munch;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.methods.PartyMethods;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PartyFilter extends AppCompatActivity {
    private static final String TAG = "FilterActivity";
    private RadioGroup priceBtns;
    private MaterialButtonToggleGroup toggleGroupCuisine;
    private RadioGroup ratingBtns;
    private static SeekBar distanceSlider;
    private TextView distanceText;
    private Button saveFilterBtn;
    PartyMethods partyMethods = new PartyMethods();
    private List<String> cuisines =
            Arrays.asList("BBQ", "Chinese", "Fast Food", "Hawker", "Indian", "Japanese", "Mexican", "Seafood", "Thai", "Western", "Malay", "Korean");
    private MaterialButtonToggleGroup toggleGroup1;
    private MaterialButtonToggleGroup toggleGroup2;
    private MaterialButtonToggleGroup toggleGroup3;
    private MaterialButtonToggleGroup toggleGroup4;
    private MaterialButtonToggleGroup toggleGroup;
    List<MaterialButton> checkedButtons = new ArrayList<>();
    List<String> selectedCuisines = new ArrayList<>();
    String partyCode;
    String userId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_filter);

        Intent intent = getIntent();
        partyCode = intent.getStringExtra("PartyCode");
        userId = intent.getStringExtra("User");
        priceBtns = findViewById(R.id.priceBtns);
        ratingBtns = findViewById(R.id.ratingBtns);
        distanceSlider = findViewById(R.id.distanceSlider);
        distanceText = findViewById(R.id.distanceText);
        saveFilterBtn = findViewById(R.id.saveFilterBtn);
        toggleGroup1 = findViewById(R.id.toggle_button_group1);
        toggleGroup2 = findViewById(R.id.toggle_button_group2);
        toggleGroup3 = findViewById(R.id.toggle_button_group3);
        toggleGroup4 = findViewById(R.id.toggle_button_group4);
        saveFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("Filters", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                int price = priceBtns.getCheckedRadioButtonId();
                int rating = ratingBtns.getCheckedRadioButtonId();
                int distance = distanceSlider.getProgress();
                getCuisineSelected();
                // Save filter inputs
                editor.putInt("Price", price);
                editor.putInt("Rating", rating);
                editor.putInt("Distance", distance);
                Set<String> previousCuisines = preferences.getStringSet("SelectedCuisines", new HashSet<>());
                selectedCuisines.clear(); // Clear the list
                selectedCuisines.addAll(previousCuisines); // Add the previously selected cuisines
                getCuisineSelected(); // Add the newly selected cuisines

                editor.putStringSet("SelectedCuisines", new HashSet<>(selectedCuisines));
                editor.apply();

                //Send filter data back to explore restaurants page
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("Price", ((RadioButton) findViewById(price)).getText());
//                resultIntent.putExtra("Rating", ((RadioButton) findViewById(rating)).getText());
//                resultIntent.putExtra("Distance", distance);
//                resultIntent.putStringArrayListExtra("SelectedCuisines", new ArrayList<>(selectedCuisines));
//                setResult(Activity.RESULT_OK, resultIntent);
                String priceText = ((RadioButton) findViewById(price)).getText().toString();
                int priceValue = 0;
                if (!priceText.equals("Any")) {
                    priceValue = Integer.parseInt(priceText.substring(1));
                }
                String ratingText = ((RadioButton) findViewById(rating)).getText().toString();
                double ratingValue = 0;
                if (!ratingText.equals("Any")) {
                    ratingValue = Double.parseDouble(ratingText.substring(0,ratingText.length()-1));
                }
                Log.i("PARTYFILTER", priceValue + " <--PRICE");
                Log.i("PARTYFILTER", ratingValue + " <--RATING");
                Log.i("PARTYFILTER", distance + " <--DISTANCE");
                List<String> cuisines = new ArrayList<>(new HashSet<>(selectedCuisines));

                Map<String, Object> updates = new HashMap<>();
                updates.put("Distance", distance / 10.0);
                updates.put("rating", ratingValue);
                updates.put("price", priceValue);
                updates.put("Cuisine", cuisines);
                db.collection("Parties").document(partyCode).collection("Users").document(userId)
                        .set(updates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("PARTYFILTER", "FAILED");
                            }
                        });

            }
        });

        distanceSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateDistance();
            }
        });
    }

    private void createCuisineButtons() {
        for (int num = 0; num < cuisines.size(); num++) {
            int buttonId = getResources().getIdentifier("toggleCuisineBtn" + (num + 1), "id", getPackageName());
            if (buttonId != 0) {
                MaterialButton materialButton = findViewById(buttonId);
                materialButton.setText(cuisines.get(num));

                // Check if the cuisine was previously selected and update the button's state

                if (selectedCuisines.contains(cuisines.get(num))) {
                    materialButton.setChecked(true);
                }
            }
        }
    }

    private void getCuisineSelected() {
        checkedButtons.clear(); // Clear the list before adding selected buttons
        selectedCuisines.clear();
        for (int num = 0; num < cuisines.size(); num++) {
            int buttonId = getResources().getIdentifier("toggleCuisineBtn" + (num + 1), "id", getPackageName());
            if (buttonId != 0) {
                MaterialButton materialButton = findViewById(buttonId);
                // Check if the cuisine was previously selected and update the button's state
                if (selectedCuisines.contains(cuisines.get(num))){
                    materialButton.setChecked(true);
                }
            }
        }
        // Add checked buttons to the 'checkedButtons' list for each toggle group
        addCheckedButtonsToList(toggleGroup1);
        addCheckedButtonsToList(toggleGroup2);
        addCheckedButtonsToList(toggleGroup3);
        addCheckedButtonsToList(toggleGroup4);

        for (MaterialButton materialButton : checkedButtons) {
            String buttonText = materialButton.getText().toString();
            selectedCuisines.add(buttonText);
        }
        Log.d("Selected Cuisines","Selected Cuisines: "+selectedCuisines);
    }

    private void addCheckedButtonsToList(MaterialButtonToggleGroup toggleGroup) {
        for (int i = 0; i < toggleGroup.getChildCount(); i++) {
            View childView = toggleGroup.getChildAt(i);
            if (childView instanceof MaterialButton) {
                MaterialButton materialButton = (MaterialButton) childView;
                if (materialButton.isChecked()) {
                    checkedButtons.add(materialButton);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "HERE");
        SharedPreferences preferences = getSharedPreferences("Filters", MODE_PRIVATE);

        int savedPrice = preferences.getInt("Price", R.id.Price1);
        int savedRating = preferences.getInt("Rating", R.id.Rating1);
        int savedDistance = preferences.getInt("Distance", 0);
        Set<String> savedCuisines = preferences.getStringSet("SelectedCuisines", new HashSet<>());

        priceBtns.check(savedPrice);
        ratingBtns.check(savedRating);
        distanceSlider.setProgress(savedDistance);
        selectedCuisines.addAll(savedCuisines);

        updateDistance();
        createCuisineButtons();
    }

    public void updateDistance() {
        float finalValue = distanceSlider.getProgress()/10.0f;
        Log.d(TAG, "Final Value: " + finalValue);
        distanceText.setText(finalValue == 0.0f ? "Any" : finalValue+"km");
    }
}
