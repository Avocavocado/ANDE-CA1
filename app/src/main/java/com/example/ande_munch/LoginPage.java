package com.example.ande_munch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.ande_munch.methods.LoginMethods;

public class LoginPage extends AppCompatActivity {

    MaterialCardView googleBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    public String userID;
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonLogin;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    EditText editTextEmailPhone;
    // New Instance of LoginMethods
    LoginMethods loginMethods = new LoginMethods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        editTextEmail = findViewById(R.id.emailEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        buttonLogin = findViewById(R.id.loginButton);

        // Initialize your UI elements here
        editTextEmailPhone = findViewById(R.id.emailEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        buttonLogin = findViewById(R.id.loginButton);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        // GoogleUI Firebase Sign In
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // Google Auth Logic Code
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        gsc = GoogleSignIn.getClient(this, gso);
        googleBtn = findViewById(R.id.googleBtn);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        // Login Button Logic Code
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginPage.this, "Email is required.", Toast.LENGTH_SHORT).show();
                Log.e("LoginError", "Email field is empty.");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginPage.this, "Password is required.", Toast.LENGTH_SHORT).show();
                Log.e("LoginError", "Password field is empty.");
                return;
            }

            // Authenticate the user
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d("LoginSuccess", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginPage.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                            goHomePage();
                        } else {
                            // Sign in failed
                            Log.w("LoginFailure", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Redirect to register page
        TextView signUpText = findViewById(R.id.signUpText);
        signUpText.setOnClickListener(v -> goRegisterPage());
    }

    public void signIn() {
        Intent SignInIntent = gsc.getSignInIntent();
        startActivityForResult(SignInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Error " + e.getStatusCode() + ": " + e.getMessage(), e);
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Firebase authentication is successful
                            goHomePage(); // Go to the homepage activity
                        } else {
                            Toast.makeText(LoginPage.this, "Something went wrong in Firebase Auth", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void goHomePage() {
        Intent intent = new Intent(LoginPage.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goRegisterPage() {
        Intent intent = new Intent(LoginPage.this, RegisterPage.class);
        startActivity(intent);
        finish();
    }
}