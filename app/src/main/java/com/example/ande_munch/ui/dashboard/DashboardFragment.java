package com.example.ande_munch.ui.dashboard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.example.ande_munch.DisplayParty;
import com.example.ande_munch.LoginPage;
import com.example.ande_munch.ProfilePage;
import com.example.ande_munch.R;
import com.example.ande_munch.databinding.FragmentDashboardBinding;
import com.example.ande_munch.methods.Callback;
import com.example.ande_munch.methods.DisplayMethods;
import com.example.ande_munch.methods.LoginMethods;
import com.example.ande_munch.methods.PartyMethods;
import com.example.ande_munch.methods.ProfileMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.ande_munch.DialogCallback;
import com.squareup.picasso.Picasso;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mauth.getCurrentUser();
    LoginMethods loginMethods = new LoginMethods();
    ProfileMethods profileMethods = new ProfileMethods();
    PartyMethods partyMethods = new PartyMethods();
    ImageView profileImageView;
    String userEmail = currentUser.getEmail();
    // Generate the 4-digit party code
    String partyCode = partyMethods.PartyCodeGenerator();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileImageView = binding.getRoot().findViewById(R.id.profileImageView);

        profileMethods.getUserProfileImage(new Callback() {
            @Override
            public void onUserChecked(boolean userExists) {

            }

            @Override
            public void onUserDataFetched(List<Map<String, Object>> usersList) {

            }

            @Override
            public void onUserDataFetched(Map<String, Object> userDetails) {

            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onUserImageFetched(String profileImage) {
                if (profileImage != null && !profileImage.isEmpty()) {
                    Picasso.get()
                            .load(profileImage)
                            .resize(50, 50)
                            .centerCrop()
                            .into(profileImageView);
                } else {
                    Log.d("DashboardFragment", "Profile image URL is null or empty");
                }
            }
        });
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfilePage.class);
                startActivity(intent);
            }
        });

        DialogCallback dialogCallback = new DialogCallback() {
            @Override
            public void onDialogResult(String dialogCode) {
                // Handle the dialog code here
                System.out.println("The dialog code is: " + dialogCode);

                partyMethods.checkPartyCode(getContext(), dialogCode, partyCodeExists -> {
                    // Check if the party code exists and handle it
                    if (partyCodeExists) {
                        // Party code exists
                        System.out.println("Party found!");
                        Log.d("dialog", "onDialogResult: " + dialogCode);

                        // Call the method with the correct parameters
                        partyMethods.addUserToParty(userEmail, dialogCode);
                        navigateToDisplayParty(userEmail, dialogCode);

                    } else {
                        // Party code does not exist
                        System.out.println("Party not found.");
                    }
                }, e -> {
                    // Handle the failure, e.g., show an error message
                    System.out.println("Error: " + e.getMessage());
                });
            }
        };

        binding.joinPartyBtn.setOnClickListener(view -> {
            showJoinPartyDialog(dialogCallback);
        });

        binding.createPartyBtn.setOnClickListener(view -> {
            initCreateParty();
            navigateToDisplayParty(userEmail, partyCode);
        });
        return root;
    }

    // Callback interface
    public interface OnUserDataFetchedListener {
        void onUserDataFetched(HashMap<String, HashMap<String, Object>> userDataMap);
    }

    // Navigation methods
    public void navigateToProfilePage() {
        Intent intent = new Intent(getActivity(), ProfilePage.class);
        startActivity(intent);
    }

    public void navigateToDisplayParty(String email, String dialogCode) {
        Intent intent = new Intent(getActivity(), DisplayParty.class);
        intent.putExtra("email", email);
        Log.d("TAG", "navigateToDisplayParty: " + dialogCode);
        intent.putExtra("DIALOG_CODE", dialogCode);
        startActivity(intent);
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
            // Get the current user's email
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
                            userDetails.put("IsLeader", true);

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

    private void showJoinPartyDialog(DialogCallback callback) {
        // Inflate the custom dialog layout
        View customView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null);

        // Create the Material Component Dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(customView)
                .setPositiveButton("Join", null) // We'll handle this in a custom way
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the negative button action or dismiss the dialog
                        dialog.dismiss();
                    }
                });

        final EditText editText1 = customView.findViewById(R.id.editText1);
        final EditText editText2 = customView.findViewById(R.id.editText2);
        final EditText editText3 = customView.findViewById(R.id.editText3);
        final EditText editText4 = customView.findViewById(R.id.editText4);

        // Set a TextWatcher for each EditText
        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    editText2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    editText3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    editText4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set a TextWatcher for editText4 to disable further input if it contains one character
        editText4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    editText4.setEnabled(false);
                }
            }
        });

        // Handle the positive button click here
        builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input1 = editText1.getText().toString();
                String input2 = editText2.getText().toString();
                String input3 = editText3.getText().toString();
                String input4 = editText4.getText().toString();

                String dialogCode = input1 + input2 + input3 + input4;

                dialog.dismiss();

                System.out.println("The dialog code is: " + dialogCode);

                Intent intent = new Intent(getActivity(), DisplayParty.class);
                intent.putExtra("DIALOG_CODE", dialogCode);
                startActivity(intent);
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Request focus on the first EditText initially
        editText1.requestFocus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}