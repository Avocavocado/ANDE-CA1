package com.example.ande_munch.ui.home;


import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ande_munch.LoginPage;
import com.example.ande_munch.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    FirebaseFirestore db;
    CollectionReference usersRef;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        String email = getUserEmail();
        System.out.println("Email: " + email);
        printAllUserEmails(email);

        // Implement button click listener for logging out
        binding.logoutBtn.setOnClickListener(v -> signOutAndQuit());

        return root;
    }

    private void signOutAndQuit() {
        // Logout of application
        FirebaseAuth.getInstance().signOut();

        // Navigate back to login page
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    // Method to get the current user email
    public String getUserEmail() {
        return user.getEmail();
    }

    // Method to check if email exist, if not, then create a new user
    public void printAllUserEmails(String email) {
        db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Loop through all the users
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Check if the user email exist
                            if (document.getId().equals(email)) {
                                System.out.println("User email exist");
                                return;
                            } else {
                                // If user doesn't exist then create a new user
                                System.out.println("User email doesn't exist");
                            }
                        }
                    } else {
                        System.out.println("Error getting documents: " + task.getException());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}