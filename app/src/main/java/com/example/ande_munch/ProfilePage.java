package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.ande_munch.databinding.ActivityProfilePageBinding; // Import your generated binding class
import com.google.firebase.auth.FirebaseAuth;

public class ProfilePage extends AppCompatActivity{
    // Declare the binding variable
    private ActivityProfilePageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the binding
        binding = ActivityProfilePageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Set the onClickListener for logout button
        binding.logoutBtn.setOnClickListener(v -> signOutAndQuit());
    }

    // Implement firebase auth logout functionality
    private void signOutAndQuit() {
        // Logout of application
        FirebaseAuth.getInstance().signOut();

        // Navigate back to login page
        Intent intent = new Intent(this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
