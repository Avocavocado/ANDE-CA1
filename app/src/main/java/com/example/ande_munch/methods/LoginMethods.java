package com.example.ande_munch.methods;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.CompletableFuture;

public class LoginMethods {
    private FirebaseFirestore db;

    public LoginMethods() {
        db = FirebaseFirestore.getInstance(); // Initialize FirebaseFirestore here
    }

    // Method to check if the user exists in the database
    public CompletableFuture<Boolean> checkDbForEmail(String email) {
        CompletableFuture<Boolean> userExistsFuture = new CompletableFuture<>();

        db.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean found = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.getId().equals(email)) {
                                found = true;
                                break; // Exit the loop if user is found
                            }
                        }
                        userExistsFuture.complete(found);
                    } else {
                        userExistsFuture.completeExceptionally(task.getException());
                    }
                });

        return userExistsFuture;
    }

    public CompletableFuture<Boolean> checkDbForPassword(String email, String password) {
        CompletableFuture<Boolean> passwordMatchFuture = new CompletableFuture<>();

        db.collection("Users")
                .document(email) // Directly access the document with the provided email
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Check if the password matches
                            String storedPassword = document.getString("Password");
                            if (storedPassword != null && storedPassword.equals(password)) {
                                passwordMatchFuture.complete(true); // Password matches
                                System.out.println("Password matches");
                            } else {
                                passwordMatchFuture.complete(false); // Password does not match
                                System.out.println("Password does not match");
                            }
                        } else {
                            passwordMatchFuture.complete(false); // Document does not exist
                            System.out.println("Document does not exist");
                        }
                    } else {
                        passwordMatchFuture.completeExceptionally(task.getException());
                        System.out.println("Something went wrong" + task.getException());
                    }
                });

        return passwordMatchFuture;
    }
}