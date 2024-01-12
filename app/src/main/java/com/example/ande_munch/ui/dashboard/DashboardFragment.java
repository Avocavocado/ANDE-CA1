package com.example.ande_munch.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.widget.Toast;

import java.util.Map;
import java.util.Random;
import java.util.HashMap;

import com.example.ande_munch.CreateParty;
import com.example.ande_munch.JoinParty;
import com.example.ande_munch.databinding.FragmentDashboardBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 4;
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mauth.getCurrentUser();
    FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.createPartyBtn.setOnClickListener(view -> navigateToCreatePartyPage());
        binding.joinPartyBtn.setOnClickListener(view -> navigateToJoinPartyPage());

        // final TextView textView = binding.textDashboard;
        // dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    // Callback interface
    public interface OnUserDataFetchedListener {
        void onUserDataFetched(HashMap<String, HashMap<String, Object>> userDataMap);
    }

    // Navigation methods
    private void navigateToCreatePartyPage() {
        Intent createPartyIntent = new Intent(getActivity(), CreateParty.class);
        initCreateParty();
        startActivity(createPartyIntent);
    }

    private void navigateToJoinPartyPage() {
        Intent joinPartyIntent = new Intent(getActivity(), JoinParty.class);
        startActivity(joinPartyIntent);
    }

    public void initCreateParty() {
        getUserEmail(new OnUserDataFetchedListener() {
            @Override
            public void onUserDataFetched(HashMap<String, HashMap<String, Object>> userDataMap) {
                createParty(userDataMap);
            }
        });
    }

    public void createParty(HashMap<String, HashMap<String, Object>> userDataMap) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Generate the 4-digit party code
        String partyCode = PartyCodeGenerator();

        // Get user emails and details to add to the party
        getUserEmail(new OnUserDataFetchedListener() {
            @Override
            public void onUserDataFetched(HashMap<String, HashMap<String, Object>> allUsersData) {
                // Create a document with the party code as the document ID inside the Parties collection
                DocumentReference partyDocumentRef = db.collection("Parties").document(partyCode);

                // Initialize the party document with an empty HashMap or necessary initial data
                partyDocumentRef.set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                    Log.d("CreateParty", "Party document created with code: " + partyCode);

                    // Iterate over all user data and create a document for each user in the sub-collection
                    for (Map.Entry<String, HashMap<String, Object>> entry : allUsersData.entrySet()) {
                        String userEmail = entry.getKey();
                        HashMap<String, Object> userDetails = entry.getValue();

                        // Add each user as a document to the 'Users' sub-collection of the party
                        partyDocumentRef.collection("Users")
                                .document(userEmail)
                                .set(userDetails)
                                .addOnSuccessListener(aVoidUser ->
                                        Log.d("CreateParty", "User added to party: " + userEmail)
                                )
                                .addOnFailureListener(e ->
                                        Log.w("CreateParty", "Error adding user to party", e)
                                );
                    }
                }).addOnFailureListener(e -> Log.w("CreateParty", "Error creating party document", e));
            }
        });
    }

    public void getUserEmail(OnUserDataFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();

            db.collection("Users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean userFound = false;
                    HashMap<String, HashMap<String, Object>> userDataMap = new HashMap<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String emailAccount = document.getId();

                        if (emailAccount.equals(currentUserEmail)) {
                            userFound = true;

                            Log.d("TAG", emailAccount + " => " + document.getData());

                            HashMap<String, Object> userDetails = new HashMap<>();
                            userDetails.put("Diet", document.getString("Diet"));
                            userDetails.put("ProfileImage", document.getString("ProfileImage"));
                            userDetails.put("Username", document.getString("Username"));

                            userDataMap.put(emailAccount, userDetails);
                            break; // Stop the loop as the user is found
                        }
                    }

                    if (userFound) {
                        listener.onUserDataFetched(userDataMap);
                    } else {
                        Log.e("TAG", "User not found: " + currentUserEmail);
                        Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("TAG", "Error getting documents: ", task.getException());
                    Toast.makeText(getActivity(), "Error fetching data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("TAG", "No current user logged in");
            Toast.makeText(getActivity(), "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    public String PartyCodeGenerator() {
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            codeBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        String partyCode = codeBuilder.toString();
        System.out.println("The party code is: " + partyCode);

        return partyCode;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}