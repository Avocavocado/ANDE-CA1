package com.example.ande_munch.ui.home;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ande_munch.CuisineButtonAdapter;
import com.example.ande_munch.FilterActivity;
import com.example.ande_munch.LoginPage;
import com.example.ande_munch.R;
import com.example.ande_munch.RestaurantCardAdapter;
import com.example.ande_munch.databinding.ExploreRestaurantsBinding;
import com.example.ande_munch.methods.LoginMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private @NonNull ExploreRestaurantsBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    // FirebaseUser user = auth.getCurrentUser();
    FirebaseFirestore db;

    // New Instances
    private LoginMethods loginMethods = new LoginMethods();
    CollectionReference usersRef;
    private static final String TAG = "ExploreRestuarants";
    private List<String> urls =
            Arrays.asList("bbq","chinese","fast_food","hawker","indian","japanese","mexican","seafood","thai","western");
    private List<String> cuisines =
            Arrays.asList("BBQ","Chinese","Fast Food","Hawker","Indian","Japanese","Mexican","Seafood","Thai","Western");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = ExploreRestaurantsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //String email = getUserEmail();
        //System.out.println("Email: " + email);
        //CheckAndAddUser(email);

        // Implement button click listener for logging out
        //binding.logoutBtn.setOnClickListener(v -> signOutAndQuit());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Restaurants")
                .get()
                .addOnSuccessListener(result -> {
                    for (DocumentSnapshot document : result.getDocuments()) {
                        Log.i(TAG,document.getId() + " => " + document.getData());
                    }

                    //Restuarant Card Layout
                    RecyclerView recyclerView = root.findViewById(R.id.RestaurantCards);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                    recyclerView.setLayoutManager(layoutManager);
                    RestaurantCardAdapter rcAdapter = new RestaurantCardAdapter(requireContext(), result.getDocuments());
                    rcAdapter.setOnItemClickListener(new RestaurantCardAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(String rid) {
                            Toast.makeText(requireContext(), "Item clicked: " + rid, Toast.LENGTH_SHORT).show();
                        }
                    });
                    recyclerView.setAdapter(rcAdapter);

                    //Cuisine Buttons Layout
                    RecyclerView cuisineBtns = root.findViewById(R.id.cuisineBtns);
                    LinearLayoutManager cuisineLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                    cuisineBtns.setLayoutManager(cuisineLayoutManager);
                    CuisineButtonAdapter cbAdapter = new CuisineButtonAdapter(requireContext(),cuisines, urls);
                    cbAdapter.setOnItemClickListener(new CuisineButtonAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(String cuisineName) {
                            Toast.makeText(requireContext(), "Item clicked: " + cuisineName, Toast.LENGTH_SHORT).show();
                        }
                    });
                    cuisineBtns.setAdapter(cbAdapter);


                })
                .addOnFailureListener(exception -> {
                    Log.i(TAG, "Error getting documents." + exception);
                })
                .addOnFailureListener(result -> {
                    Log.e(TAG,"Firestore trollin: "+ result);
                });

        ImageButton filterBtn = root.findViewById(R.id.filterButton);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), FilterActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

//    private void signOutAndQuit() {
//        // Logout of application
//        FirebaseAuth.getInstance().signOut();
//
//        // Navigate back to login page
//        if (getActivity() != null) {
//            Intent intent = new Intent(getActivity(), LoginPage.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            getActivity().finish();
//        }
//    }
//
//    // Method to get the current user email
////    public String getUserEmail() {
////        return user.getEmail();
////    }
//
//    // Method to check if email exist, if not, then create a new user
//    public void printAllUserEmails(String email) {
//        db = FirebaseFirestore.getInstance();
//
//        db.collection("Users")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Loop through all the users
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            // Check if the user email exist
//                            if (document.getId().equals(email)) {
//                                System.out.println("User email exist");
//                                return;
//                            } else {
//                                // If user doesn't exist then create a new user
//                                System.out.println("User email doesn't exist");
//                            }
//                        }
//                    } else {
//                        System.out.println("Error getting documents: " + task.getException());
//                    }
//                });
//    }
//
//    public void createUser(String email) {
//        db = FirebaseFirestore.getInstance();
//        DocumentReference newUserRef = db.collection("Users").document(email);
//
//        // Adding user fields to the datastore
//        Map<String, Object> newUserMap = new HashMap<>();
//        newUserMap.put("Diet", "");
//        newUserMap.put("Password", "");
//        newUserMap.put("ProfileImage", "");
//        newUserMap.put("Username", "");
//
//        newUserRef.set(newUserMap)
//                .addOnSuccessListener(aVoid -> System.out.println("User successfully created!"))
//                .addOnFailureListener(e -> System.out.println("Error creating user: " + e));
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}