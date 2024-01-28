package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ande_munch.databinding.ActivityProfilePageBinding; // Import your generated binding class
import com.example.ande_munch.methods.Callback;
import com.example.ande_munch.methods.ProfileMethods;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfilePage extends AppCompatActivity{
    // Declare the binding variable
    private ActivityProfilePageBinding binding;
    ProfileMethods profileMethods = new ProfileMethods();
    Map<String, Object> userDetails;
    Toast Toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the binding
        binding = ActivityProfilePageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Set the onClickListener for logout button
        binding.logoutBtn.setOnClickListener(v -> signOutAndQuit());
        binding.updateBtn.setOnClickListener(v -> updateProfile());

        profileMethods.getUserProfileDetails(new Callback() {
            @Override
            public void onUserChecked(boolean userExists) {

            }

            @Override
            public void onUserDataFetched(List<Map<String, Object>> usersList) {

            }

            @Override
            public void onUserDataFetched(Map<String, Object> userDetails) {
                ProfilePage.this.userDetails = userDetails;
                setUserProfileDetails(userDetails);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ProfilePage", "Error fetching user details", e);
                // Handle the failure, like showing a toast or a dialog
            }

            @Override
            public void onSuccess() {

            }
        });

        ImageView imageViewTogglePassword = findViewById(R.id.imageViewTogglePassword);
        imageViewTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.editTextPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Show the password
                    binding.editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imageViewTogglePassword.setImageResource(R.drawable.password_show_symbol); // Change the icon to indicate password is visible
                } else {
                    // Hide the password
                    binding.editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imageViewTogglePassword.setImageResource(R.drawable.password_hide_symbol); // Change the icon to indicate password is hidden
                }

                // Move the cursor to the end of the text
                binding.editTextPassword.setSelection(binding.editTextPassword.getText().length());
            }
        });
        ImageView backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void setUserProfileDetails(Map<String, Object> userDetails){
        String username = Objects.requireNonNull(userDetails.get("Username")).toString();
        binding.usernameEditTxt.setText(username);

        String password = Objects.requireNonNull(userDetails.get("Password")).toString();
        binding.editTextPassword.setText(password);

        String dietaryRestriction = Objects.requireNonNull(userDetails.get("Diet")).toString();
        int position = getPositionForDietaryRestriction(dietaryRestriction);
        binding.dietSpinner.setSelection(position);
    }
    private void updateProfile() {
        // Get the updated data from the UI fields
        String newUsername = binding.usernameEditTxt.getText().toString();
        String newPassword = binding.editTextPassword.getText().toString();
        String newDiet = binding.dietSpinner.getSelectedItem().toString();

        // Check if the username and password are not null or empty
        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            // Show an error message if username or password is empty
            android.widget.Toast.makeText(ProfilePage.this, "Username and password cannot be empty", android.widget.Toast.LENGTH_SHORT).show();
        } else {
            // Create a Map with the updated data
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("Username", newUsername);
            updatedData.put("Password", newPassword);
            updatedData.put("Diet", newDiet);

            // Call the updateUserProfile method from ProfileMethods
            profileMethods.updateUserProfile(updatedData, new Callback() {
                @Override
                public void onUserChecked(boolean userExists) {
                    // ...
                }

                @Override
                public void onUserDataFetched(List<Map<String, Object>> usersList) {
                    // ...
                }

                @Override
                public void onUserDataFetched(Map<String, Object> userDetails) {
                    // ...
                }

                @Override
                public void onFailure(Exception e) {
                    // Handle the failure, e.g., show an error message
                    Log.e("ProfilePage", "Error updating profile", e);
                    android.widget.Toast.makeText(ProfilePage.this, "Error updating profile", android.widget.Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    // Handle the success case, e.g., show a success message
                    android.widget.Toast.makeText(ProfilePage.this, "Profile updated successfully", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private int getPositionForDietaryRestriction(String dietaryRestriction) {

        String[] dietaryOptions = {"None","Vegetarian", "Vegan"};
        for (int i = 0; i < dietaryOptions.length; i++) {
            if (dietaryOptions[i].equalsIgnoreCase(dietaryRestriction)) {
                return i;
            }
        }
        return 0; // Default position if not found
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
