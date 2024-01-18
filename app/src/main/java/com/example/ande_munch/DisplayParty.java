package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DisplayParty extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitvity_display_party);

        Button buttonFilterPage = findViewById(R.id.partyFilterBtn);
        buttonFilterPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayParty.this, PartyFilter.class);
                startActivity(intent);
            }
        });
    }

}
