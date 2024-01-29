package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ande_munch.methods.LoginMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

public class RegisterPage extends AppCompatActivity {
    // Firebase stuff
    private FirebaseAuth mAuth;
    private LoginMethods loginMethods = new LoginMethods();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Declare the EditText attributes here
    private EditText usernameInput, emailInput, passwordInput;
    private Button signUpButton;
    String username, email, password;

    private static final String TAG = "RegisterPage"; // TAG for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize the EditText and Button attributes
        usernameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> {
            username = usernameInput.getText().toString().trim();
            email = emailInput.getText().toString().trim();
            password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(RegisterPage.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                CheckAndAddUser();
                                Toast.makeText(RegisterPage.this, "Registration Completed: " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Registration successful for user: " + mAuth.getCurrentUser().getEmail());
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Sign in success
                                                Log.d("LoginSuccess", "signInWithEmail:success");
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                Toast.makeText(RegisterPage.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                                                goHomePage();
                                            } else {
                                                // Sign in failed
                                                Log.w("LoginFailure", "signInWithEmail:failure", task.getException());
                                                Toast.makeText(RegisterPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(RegisterPage.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Registration failure", task.getException());
                            }
                        });
            }
        });
    }

    public void CheckAndAddUser() {
        db = FirebaseFirestore.getInstance();
        loginMethods.checkDbForEmail(email).thenAccept(userExists -> {
            if (!userExists) {
                Log.d(TAG, "User email doesn't exist");
                loginMethods.createUser(email, password, username);
            } else {
                Log.d(TAG, "User email exists");
            }
        }).exceptionally(e -> {
            Log.e(TAG, "Error checking user in database: " + e);
            return null;
        });
    }

    public void goHomePage() {
        Intent intent = new Intent(RegisterPage.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}