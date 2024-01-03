package com.example.ande_munch.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import com.example.ande_munch.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // final TextView textView = binding.textDashboard;
        final TextView textView = binding.startPartyBtn;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Code begins here
        retrieveFromFirebase();

        return root;
    }

    public void retrieveFromFirebase() {
        db = FirebaseFirestore.getInstance();

        db.collection("Parties").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> userList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                        // Assuming you want to get the document ID or a specific field as a string
                        String userData = documentSnapshot.getId();
                        userList.add(userData);
                    }

                    // Converting list to array
                    String[] userArray = userList.toArray(new String[0]);

                    // Printing out the array
                    for (String user : userArray) {
                        System.out.println(user);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    System.out.println("Error getting documents: " + e.getMessage());
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}