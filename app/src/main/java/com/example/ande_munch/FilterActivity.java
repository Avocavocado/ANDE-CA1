package com.example.ande_munch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class FilterActivity extends AppCompatActivity {
    private static final String TAG = "FilterActivity";
    private RadioGroup priceBtns;
    private RadioGroup ratingBtns;
    private static SeekBar distanceSlider;
    private TextView distanceText;
    private Button saveFilterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        priceBtns = findViewById(R.id.priceBtns);
        ratingBtns = findViewById(R.id.ratingBtns);
        distanceSlider = findViewById(R.id.distanceSlider);
        distanceText = findViewById(R.id.distanceText);
        saveFilterBtn = findViewById(R.id.saveFilterBtn);

        priceBtns.check(R.id.Price1);
        ratingBtns.check(R.id.Rating1);
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
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateDistance();
            }
        });

        ImageButton closeFilter = findViewById(R.id.cancelFilterBtn);
        closeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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
