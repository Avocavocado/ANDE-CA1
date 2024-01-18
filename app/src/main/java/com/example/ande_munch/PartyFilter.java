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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;


public class PartyFilter extends AppCompatActivity {
    private static final String TAG = "FilterActivity";
    private RadioGroup priceBtns;
    private MaterialButtonToggleGroup toggleGroupCuisine;
    private RadioGroup ratingBtns;
    private static SeekBar distanceSlider;
    private TextView distanceText;
    private Button saveFilterBtn;
    private List<String> cuisines =
            Arrays.asList("BBQ", "Chinese", "Fast Food", "Hawker", "Indian", "Japanese", "Mexican", "Seafood", "Thai", "Western", "Malay");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_filter);

        priceBtns = findViewById(R.id.priceBtns);
        ratingBtns = findViewById(R.id.ratingBtns);
        distanceSlider = findViewById(R.id.distanceSlider);
        distanceText = findViewById(R.id.distanceText);
        saveFilterBtn = findViewById(R.id.saveFilterBtn);
        saveFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("Filters", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                int price = priceBtns.getCheckedRadioButtonId();
                int rating = ratingBtns.getCheckedRadioButtonId();
                int distance = distanceSlider.getProgress();

                // Save filter inputs
                editor.putInt("Price", price);
                editor.putInt("Rating", rating);
                editor.putInt("Distance", distance);
                editor.apply();

                //Send filter data back to explore restaurants page
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Price", ((RadioButton) findViewById(price)).getText());
                resultIntent.putExtra("Rating", ((RadioButton) findViewById(rating)).getText());
                resultIntent.putExtra("Distance", distance);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
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
        toggleGroupCuisine = findViewById(R.id.btnGroupCuisine);
        createCuisineButtons();
    }

    private void createCuisineButtons() {
        // Calculate the number of rows needed
        int numRows = (int) Math.ceil(cuisines.size() / 3.0);
        Log.i("numRows", "Number of rows" + numRows);
        int buttonIndex = 0;

        for (int row = 0; row < numRows; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 3; col++) {
                if (buttonIndex < cuisines.size()) {
                    Log.i("Test", "Test: " + col);
                    MaterialButton button = new MaterialButton(this, null);
                    button.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                    button.setText(cuisines.get(buttonIndex));
                    button.setId(View.generateViewId()); // Generate a unique ID for the button
                    button.setOnClickListener(view -> {
                        // Handle the click event
                        button.setChecked(!button.isChecked());
                    });
                    rowLayout.addView(button);
                    buttonIndex++;
                }
            }

            // Add the row to the ToggleGroup
            toggleGroupCuisine.addView(rowLayout);
        }

    // ... existing code ...
}
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "HERE");
        SharedPreferences preferences = getSharedPreferences("Filters", MODE_PRIVATE);

        int savedPrice = preferences.getInt("Price", R.id.Price1);
        int savedRating = preferences.getInt("Rating", R.id.Rating1);
        int savedDistance = preferences.getInt("Distance", 0);

        priceBtns.check(savedPrice);
        ratingBtns.check(savedRating);
        distanceSlider.setProgress(savedDistance);

        updateDistance();
    }

    public void updateDistance() {
        float finalValue = distanceSlider.getProgress()/10.0f;
        Log.d(TAG, "Final Value: " + finalValue);
        distanceText.setText(finalValue == 0.0f ? "Any" : finalValue+"km");
    }



}