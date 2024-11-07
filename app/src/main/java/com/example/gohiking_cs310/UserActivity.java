package com.example.gohiking_cs310;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;


import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ArrayList<String> friendsList = new ArrayList<>();
    private ArrayList<String> hikesList = new ArrayList<>();
    private ArrayAdapter<String> hikeAdapter;
    private AutoCompleteTextView autoCompleteHikeSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();

        autoCompleteHikeSearch = findViewById(R.id.editTextSearchHike);
        hikeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, hikesList);
        autoCompleteHikeSearch.setAdapter(hikeAdapter);
        loadHikeSuggestions("");

        autoCompleteHikeSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteHikeSearch.showDropDown();
            }
        });

        autoCompleteHikeSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadHikeSuggestions(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });

        autoCompleteHikeSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedHike = hikeAdapter.getItem(position);
            Toast.makeText(UserActivity.this, "Selected Hike: " + selectedHike, Toast.LENGTH_SHORT).show();
            hikeQuery(selectedHike);
        });

        Button buttonBackToHome = findViewById(R.id.buttonBackHome);
        buttonBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        });

        Button addFriend = findViewById(R.id.buttonAddFriend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailInput = findViewById(R.id.editTextAddFriend);
                String email = emailInput.getText().toString().trim();
                if (!email.isEmpty()) {
                    queryUserByEmail(email);
                } else {
                    Toast.makeText(UserActivity.this, "Please enter an email.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button myFriendsButton = findViewById(R.id.buttonMyFriends);
        myFriendsButton.setOnClickListener(v -> fetchAndShowFriends());

        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            EditText searchInput = findViewById(R.id.editTextSearchHike);
            String searchQuery = searchInput.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                hikeQuery(searchQuery);
            } else {
                Toast.makeText(UserActivity.this, "Please enter a search term.", Toast.LENGTH_SHORT).show();
            }
        });

        Button backToHomeButton = findViewById(R.id.buttonBackHome);
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        Button myHikes = findViewById(R.id.buttonMyHikes);
        myHikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomHikes();
            }
        });
    }

    private void loadHikeSuggestions(String query) {
        hikesList.clear();
        hikesList.add("Griffith Observatory");
        hikesList.add("Hollywood Sign");
        hikesList.add("Skid Row");
        hikesList.add("Sycamore Canyon Trailhead");
        hikesList.add("Trail Canyon Falls");
        hikeAdapter.notifyDataSetChanged();

        /*db.collection("Hikes")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        hikesList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String hikeName = document.getString("name");
                            if (hikeName != null) {
                                hikesList.add(hikeName);
                            }
                        }
                        hikeAdapter.notifyDataSetChanged(); // Update dropdown suggestions
                        Log.d("UserActivity", "Hike suggestions: " + hikesList); // Log the fetched data
                    } else {
                        Toast.makeText(UserActivity.this, "Failed to load hike suggestions.", Toast.LENGTH_SHORT).show();
                    }
                });*/
    }


    private void queryUserByEmail(String email) {
        // Get the current user's ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String friendUserId = document.getId();
                                String foundEmail = document.getString("email");
                                String username = document.getString("username");

                                // Log user info for debugging
                                Log.d("MY ACCT", "Current User ID: " + currentUserId);
                                Log.d("FRIEND ACCT", "Friend Email: " + foundEmail);

                                // Add the friend to the current user's friends array
                                db.collection("Users").document(currentUserId)
                                        .update("friends", FieldValue.arrayUnion(friendUserId))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(UserActivity.this, email + " added as a friend", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UserActivity.this, "Failed to add friend.", Toast.LENGTH_SHORT).show();
                                        });

                                // Add the current user to the friend's friends array
                                db.collection("Users").document(friendUserId)
                                        .update("friends", FieldValue.arrayUnion(currentUserId))
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UserActivity.this, "Failed to add current user as friend.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // No user found with the specified email
                            Toast.makeText(UserActivity.this, "No user found with this email.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAndShowFriends() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ArrayList<String> friends = (ArrayList<String>) documentSnapshot.get("friends");
                        if (friends != null && !friends.isEmpty()) {
                            fetchFriendDetails(friends);
                        } else {
                            Toast.makeText(UserActivity.this, "You have no friends added.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserActivity.this, "Failed to fetch friends.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchFriendDetails(ArrayList<String> friendIds) {
        friendsList.clear();
        for (String friendId : friendIds) {
            db.collection("Users").document(friendId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String friendEmail = documentSnapshot.getString("email");
                            friendsList.add(friendEmail);
                            if (friendsList.size() == friendIds.size()) {
                                showFriendsDialog();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UserActivity.this, "Failed to fetch friend details.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showFriendsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Friends");

        String[] friendsArray = friendsList.toArray(new String[0]);
        builder.setItems(friendsArray, (dialog, which) -> {

        });

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void hikeQuery(String hike) {
        db.collection("Hikes")
                .whereEqualTo("Name", hike)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (!snapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : snapshot) {
                                String hikeID = document.getId();
                                Boolean bathrooms = document.getBoolean("Bathrooms") != null ? document.getBoolean("Bathrooms") : false;
                                Long difficulty = document.getLong("Difficulty") != null ? document.getLong("Difficulty") : 0;
                                Long lat = document.getLong("Lat") != null ? document.getLong("Lat") : 0;
                                Long hikelong = document.getLong("Long") != null ? document.getLong("Long") : 0;
                                String name = document.getString("Name") != null ? document.getString("Name") : "Unknown";
                                Boolean parking = document.getBoolean("Parking") != null ? document.getBoolean("Parking") : false;
                                ArrayList<Double> ratings = document.get("Ratings") != null ? (ArrayList<Double>) document.get("Ratings") : new ArrayList<>();
                                ArrayList<String> reviews = document.get("Reviews") != null ? (ArrayList<String>) document.get("Reviews") : new ArrayList<>();
                                String conditions = document.getString("Trail Conditions") != null ? document.getString("Trail Conditions") : "Not available";
                                Boolean markers = document.getBoolean("Trail Markers") != null ? document.getBoolean("Trail Markers") : false;
                                Boolean trash = document.getBoolean("Trash Cans") != null ? document.getBoolean("Trash Cans") : false;
                                Boolean water = document.getBoolean("Water Fountains") != null ? document.getBoolean("Water Fountains") : false;
                                Boolean wifi = document.getBoolean("WiFi") != null ? document.getBoolean("WiFi") : false;


                                Hike queriedHike = new Hike(
                                        hikeID,
                                        name,
                                        difficulty.intValue(),
                                        lat.doubleValue(),
                                        hikelong.doubleValue(),
                                        bathrooms,
                                        parking,
                                        ratings,
                                        reviews,
                                        conditions,
                                        markers,
                                        trash,
                                        water,
                                        wifi
                                );


                                if (queriedHike != null) {
                                    Log.d("HikeActivity", "Hike details loaded: " + queriedHike.getName());
                                } else {
                                    Log.e("HikeActivity", "Hike object is null!");
                                }


                                Intent intent = new Intent(UserActivity.this, HikeActivity.class);
                                intent.putExtra("hikeObject", queriedHike);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(UserActivity.this, "No hike found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to show the search results in a popup
    private void showSearchResultsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results");

        String[] hikesArray = hikesList.toArray(new String[0]);
        builder.setItems(hikesArray, (dialog, which) -> {

        });

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showCustomHikes() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ArrayList<String> userHikes = (ArrayList<String>) documentSnapshot.get("customHikes");
                        if (userHikes != null && !userHikes.isEmpty()) {
                            StringBuilder hikeList = new StringBuilder("My Hikes: " + "\n");
                            for (int i = 0; i < userHikes.size(); i++) {
                                hikeList.append(userHikes.get(i)).append("\n");
                            }
                            new android.app.AlertDialog.Builder(UserActivity.this)
                                    .setTitle("Custom Hikes: ")
                                    .setMessage(userHikes.toString())
                                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                                    .show();
                        } else {
                            Toast.makeText(UserActivity.this, "No custom hikes added.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(UserActivity.this, "Failed to fetch custom hikes.", Toast.LENGTH_SHORT).show()
                );
    }
}
