package com.example.ande_munch.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.LoginPage;
import com.example.ande_munch.RestaurantCardAdapter;
import com.example.ande_munch.databinding.FragmentHomeBinding;
import com.example.ande_munch.methods.LoginMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HomeFragment extends Fragment {
    private static final String TAG = "ExploreRestaurants";
    private FragmentHomeBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RestaurantCardAdapter adapter;

    private LoginMethods loginMethods = new LoginMethods();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String email = loginMethods.getUserEmail();
        Log.d(TAG, "Email: " + email);
        CheckAndAddUser(email);

        // Initialize RecyclerView and Adapter
        recyclerView = binding.RestaurantCards;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        getRestaurantCardData();

        return root;
    }

    public void CheckAndAddUser(String email) {
        db = FirebaseFirestore.getInstance();
        loginMethods.checkDbForEmail(email).thenAccept(userExists -> {
            if (!userExists) {
                Log.d(TAG, "User email doesn't exist");
                loginMethods.createUser(email);
            } else {
                Log.d(TAG, "User email exists");
            }
        }).exceptionally(e -> {
            Log.e(TAG, "Error checking user in database: " + e);
            return null;
        });
    }

    public void getRestaurantCardData() {
        db = FirebaseFirestore.getInstance();
        db.collection("Restaurants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            documents.add(document);
                        }
                        adapter = new RestaurantCardAdapter(documents);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching documents", e));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
