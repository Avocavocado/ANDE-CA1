package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ande_munch.databinding.ActivityProfilePageBinding; // Import your generated binding class
import com.example.ande_munch.methods.Callback;
import com.example.ande_munch.methods.LoginMethods;
import com.example.ande_munch.methods.ProfileMethods;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProfilePage extends AppCompatActivity{
    // Declare the binding variable
    private ActivityProfilePageBinding binding;
    ProfileMethods profileMethods = new ProfileMethods();
    Map<String, Object> userDetails;
    Toast Toast;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LoginMethods loginMethods = new LoginMethods();
    String loggedInEmail = loginMethods.getUserEmail();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfilePageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.logoutBtn.setOnClickListener(v -> signOutAndQuit());
        binding.updateBtn.setOnClickListener(v -> updateProfile());

        loadUserProfile();

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

        String profileImageUrl = userDetails.get("ProfileImage").toString();
        int size = 310;
        Picasso.get()
                .load(profileImageUrl)
                .resize(size, size)
                .centerCrop()
                .into(binding.profileImageView);
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

                @Override
                public void onUserImageFetched(String profileImage) {

                }
            });
        }
    }

    private int getPositionForDietaryRestriction(String dietaryRestriction) {

        String[] dietaryOptions = {"None","Vegetarian", "Vegan"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, dietaryOptions);
        binding.dietSpinner.setAdapter(adapter);
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

    public void onEditProfileClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("UserImages/" + UUID.randomUUID().toString());
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(this::updateUserProfileImage))
                .addOnFailureListener(e -> Toast.makeText(ProfilePage.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUserProfileImage(Uri downloadUri) {
        // Create a Map to hold the updated data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("ProfileImage", downloadUri.toString());

        // Update the document in Firestore
        db.collection("Users").document(loggedInEmail)
                .set(updatedData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("updateUserProfile", "User profile updated successfully");
                     Toast.makeText(ProfilePage.this, "Profile image updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.d("updateUserProfile", "Error updating user profile: " + e.getMessage());
                     Toast.makeText(ProfilePage.this, "Failed to update profile image.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProfile(){
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

            @Override
            public void onUserImageFetched(String profileImage) {

            }
        });
    }
}
