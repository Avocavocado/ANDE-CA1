package com.example.ande_munch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import android.util.Log;

public class RegisterPage extends AppCompatActivity {
    // Firebase stuff
    private FirebaseAuth mAuth;

    // Declare the EditText attributes here
    private EditText usernameInput, emailInput, passwordInput;
    private Button signUpButton;

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
            String username, email, password;
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
                                Toast.makeText(RegisterPage.this, "Registration Completed: " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Registration successful for user: " + mAuth.getCurrentUser().getEmail());
                            } else {
                                Toast.makeText(RegisterPage.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Registration failure", task.getException());
                            }
                        });
            }
        });
    }
}